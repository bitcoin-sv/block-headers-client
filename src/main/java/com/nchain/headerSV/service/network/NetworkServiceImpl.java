package com.nchain.headerSV.service.network;

import com.nchain.headerSV.config.P2PConfig;
import com.nchain.headerSV.domain.PeerInfo;
import com.nchain.headerSV.service.consumer.MessageConsumer;
import com.nchain.headerSV.service.propagation.buffer.MessageBufferService;
import com.nchain.jcl.net.network.PeerAddress;
import com.nchain.jcl.net.network.events.PeerDisconnectedEvent;
import com.nchain.jcl.net.protocol.config.ProtocolConfig;
import com.nchain.jcl.net.protocol.events.MsgReceivedEvent;
import com.nchain.jcl.net.protocol.events.PeerHandshakedEvent;
import com.nchain.jcl.net.protocol.handlers.block.BlockDownloaderHandler;
import com.nchain.jcl.net.protocol.handlers.handshake.HandshakeHandlerConfig;
import com.nchain.jcl.net.protocol.messages.SendHeadersMsg;
import com.nchain.jcl.net.protocol.messages.common.BitcoinMsgBuilder;
import com.nchain.jcl.net.protocol.messages.common.Message;
import com.nchain.jcl.net.protocol.wrapper.P2P;
import com.nchain.jcl.net.protocol.wrapper.P2PBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 03/06/2020
 */
@Service
@Slf4j
public class NetworkServiceImpl implements NetworkService {

    private ProtocolConfig protocolConfig;

    private P2PConfig p2pConfig;

    // Protocol Handlers: This objects will carry out the Bitcoin Protocol and perform the
    // Serialization of messages.
    private P2P p2p;

    // Service to store the info from the Network into the Repository as they come along
    private final MessageBufferService messageBufferService;

    // A Collection to keep track of the Peers handshaked:
    private final Map<PeerAddress, PeerInfo> peersInfo = new ConcurrentHashMap<>();

    private ScheduledExecutorService executor;

    private Map<Class<? extends Message>, Set<MessageConsumer>> messageConsumers = new ConcurrentHashMap<>();


    @Autowired
    protected NetworkServiceImpl(ProtocolConfig protocolConfig,
                                 P2PConfig p2pConfig,
                                 MessageBufferService messageBufferService) {
        this.p2pConfig = p2pConfig;
        this.protocolConfig = protocolConfig;
        this.messageBufferService = messageBufferService;

        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    private void init() {
        log.info("Initalizing Network Service" );

        HandshakeHandlerConfig handshakeHandlerConfig = HandshakeHandlerConfig.builder()
                .relayTxs(p2pConfig.isRelayTxs())
                .build();

        p2p = new P2PBuilder("headersv")
                .config(protocolConfig)
                .minPeers(p2pConfig.getMinPeers())
                .maxPeers(p2pConfig.getMaxPeers())
                .config(handshakeHandlerConfig)
                .excludeHandler(BlockDownloaderHandler.HANDLER_ID)
                .build();

        p2p.EVENTS.PEERS.DISCONNECTED.forEach(this::onPeerDisconnected);
        p2p.EVENTS.PEERS.HANDSHAKED.forEach(this::onPeerHandshaked);
        p2p.EVENTS.MSGS.ALL.forEach(this::onMessage);

    }


    @Override
    public void start() {
        init();
        p2p.start();
    }

    @Override
    public void stop() {
        p2p.stop();
        executor.shutdown();
    }

    @Override
    public void broadcast(Message message){
       BitcoinMsgBuilder bitcoinMsgBuilder = new BitcoinMsgBuilder<>(protocolConfig.getBasicConfig(), message);
       p2p.REQUESTS.MSGS.broadcast(bitcoinMsgBuilder.build()).submit();
    }

    @Override
    public void send(Message message, PeerAddress peerAddress) {
        BitcoinMsgBuilder bitcoinMsgBuilder = new BitcoinMsgBuilder<>(protocolConfig.getBasicConfig(), message);
        p2p.REQUESTS.MSGS.send(peerAddress, bitcoinMsgBuilder.build()).submit();
    }

    @Override
    public void subscribe(Class<? extends Message> eventClass, MessageConsumer messageConsumer) {
        Set<MessageConsumer> consumers = new HashSet<>();
        consumers.add(messageConsumer);
        messageConsumers.merge(eventClass, consumers, (w, prev) -> {prev.addAll(w); return prev;});
    }

    @Override
    public void unsubscribe(Class<? extends Message> eventClass, MessageConsumer messageConsumer) {
        Set<MessageConsumer> consumers = new HashSet<>();
        consumers.remove(messageConsumer);
        messageConsumers.merge(eventClass, consumers, (w, prev) -> {prev.addAll(w); return prev;});
    }


    private void onMessage(MsgReceivedEvent msgReceivedEvent) {
        log.debug("Incoming Message coming from:" + msgReceivedEvent.getPeerAddress() + "type: " + msgReceivedEvent.getBtcMsg().getHeader().getCommand());
        Set<MessageConsumer> handlers = messageConsumers.get(msgReceivedEvent.getBtcMsg().getBody().getClass());

        if(handlers == null) {
            return;
        }

        handlers.forEach(handler -> handler.consume(msgReceivedEvent.getBtcMsg(), msgReceivedEvent.getPeerAddress()));
    }

    private void onPeerDisconnected(PeerDisconnectedEvent event) {
        log.debug("onPeerDisconnected: IP:" + event.getPeerAddress().toString()+":Reason:" + event.getReason().toString());
        PeerInfo peerInfo = peersInfo.get(event.getPeerAddress());

        if(peerInfo!= null) {
            peerInfo.setPeerConnectedStatus(false);
        }
    }

    private void onPeerHandshaked(PeerHandshakedEvent event) {
        log.debug("onPeerHandshaked: IP:" + event.getPeerAddress().toString()+":User Agent:"+ event.getVersionMsg().getUser_agent() +": Version :" + event.getVersionMsg().getVersion());

        SendHeadersMsg sendHeadersMsg = SendHeadersMsg.builder().build();
        BitcoinMsgBuilder bitcoinMsgBuilder = new BitcoinMsgBuilder<>(protocolConfig.getBasicConfig(), sendHeadersMsg);

        p2p.REQUESTS.MSGS.send(event.getPeerAddress(), bitcoinMsgBuilder.build()).submit();

        PeerInfo peerInfo = peersInfo.get(event.getPeerAddress());
        if (peerInfo == null) {
            peerInfo = new PeerInfo(event.getPeerAddress(), event.getVersionMsg(), Optional.empty(), true);
            peersInfo.put(event.getPeerAddress(), peerInfo);
        }
     }


    public int getConnectedPeersCount(){
        return peersInfo.values().stream().filter(PeerInfo::isPeerConnectedStatus).collect(Collectors.toList()).size();
    }

}
