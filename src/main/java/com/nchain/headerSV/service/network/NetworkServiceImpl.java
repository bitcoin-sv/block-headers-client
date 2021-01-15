package com.nchain.headerSV.service.network;

import com.nchain.headerSV.service.consumer.MessageConsumer;
import com.nchain.jcl.net.network.PeerAddress;
import com.nchain.jcl.net.network.events.PeerDisconnectedEvent;
import com.nchain.jcl.net.protocol.config.ProtocolConfig;
import com.nchain.jcl.net.protocol.events.MsgReceivedEvent;
import com.nchain.jcl.net.protocol.events.PeerHandshakedEvent;
import com.nchain.jcl.net.protocol.handlers.block.BlockDownloaderHandler;
import com.nchain.jcl.net.protocol.messages.common.BitcoinMsgBuilder;
import com.nchain.jcl.net.protocol.messages.common.Message;
import com.nchain.jcl.net.protocol.wrapper.P2P;
import com.nchain.jcl.net.protocol.wrapper.P2PBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 03/06/2020
 */
@Service
@Slf4j
public class NetworkServiceImpl implements NetworkService {

    private ProtocolConfig protocolConfig;

    // Protocol Handlers: This objects will carry out the Bitcoin Protocol and perform the
    // Serialization of messages.
    private P2P p2p;

    // A Collection to keep track of the Peers handshaked:
    private List<PeerAddress> connectedPeers = Collections.synchronizedList(new ArrayList<>());

    private Map<Class<? extends Message>, Set<MessageConsumer>> messageConsumers = new ConcurrentHashMap<>();


    @Autowired
    protected NetworkServiceImpl(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
    }

    private void init() {
        log.info("Initalizing Network Service");

        p2p = new P2PBuilder("headersv") //TODO const
                .config(protocolConfig)
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
    }

    @Override
    public void broadcast(Message message) {
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
        messageConsumers.merge(eventClass, consumers, (w, prev) -> {
            prev.addAll(w);
            return prev;
        });
    }

    @Override
    public List<PeerAddress> getConnectedPeers() {
        return new ArrayList<>(connectedPeers);
    }

    private void onMessage(MsgReceivedEvent msgReceivedEvent) {
        log.debug("Incoming Message coming from:" + msgReceivedEvent.getPeerAddress() + "type: " + msgReceivedEvent.getBtcMsg().getHeader().getCommand());
        Set<MessageConsumer> handlers = messageConsumers.get(msgReceivedEvent.getBtcMsg().getBody().getClass());

        //TODO need to handle version?
        if (handlers == null) {
            return;
        }

        handlers.forEach(handler -> handler.consume(msgReceivedEvent.getBtcMsg(), msgReceivedEvent.getPeerAddress()));
    }

    private void onPeerDisconnected(PeerDisconnectedEvent event) {
        log.debug("onPeerDisconnected: IP:" + event.getPeerAddress().toString() + ":Reason:" + event.getReason().toString());

        connectedPeers.remove(event.getPeerAddress());
    }

    private void onPeerHandshaked(PeerHandshakedEvent event) {
        log.debug("onPeerHandshaked: IP:" + event.getPeerAddress().toString() + ":User Agent:" + event.getVersionMsg().getUser_agent() + ": Version :" + event.getVersionMsg().getVersion());

        connectedPeers.add(event.getPeerAddress());
    }



}
