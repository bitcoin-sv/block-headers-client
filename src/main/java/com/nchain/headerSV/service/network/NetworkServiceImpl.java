package com.nchain.headerSV.service.network;

import com.nchain.bna.network.PeerAddress;
import com.nchain.bna.network.config.NetConfig;
import com.nchain.bna.network.listeners.PeerDisconnectedListener;
import com.nchain.bna.protocol.config.ProtocolConfig;
import com.nchain.bna.protocol.handlers.SetupHandlersBuilder;
import com.nchain.bna.protocol.listeners.MessageReceivedListener;
import com.nchain.bna.protocol.messages.*;
import com.nchain.bna.protocol.messages.common.BitcoinMsg;
import com.nchain.bna.protocol.messages.common.Message;
import com.nchain.bna.tools.RuntimeConfig;
import com.nchain.bna.tools.files.FileUtils;
import com.nchain.headerSV.domain.PeerInfo;
import com.nchain.headerSV.service.propagation.buffer.BufferedMessagePeer;
import com.nchain.headerSV.service.propagation.buffer.MessageBufferService;
import com.nchain.headerSV.service.sync.consumer.MessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 03/06/2020
 */
@Service
@Slf4j
public class NetworkServiceImpl implements NetworkService {


    // Basic Configuration to connect to the P2P Network and use the Bitcoin Protocol:
    private final RuntimeConfig runtimeConfig;
    private final NetConfig netConfig;
    private final ProtocolConfig protocolConfig;
    private final FileUtils fileUtils;

    // Protocol Handlers: This objects will carry out the Bitcoin Protocol and perform the
    // Serialization of messages.
    private SetupHandlersBuilder.HandlersSetup protocolHandler;

    // Service to store the info from the Network into the Repository as they come along
    private final MessageBufferService messageBufferService;

    // A Collection to keep track of the Peers handshaked:
    private final Map<PeerAddress, PeerInfo> peersInfo = new ConcurrentHashMap<>();

    private final Queue<PeerInfo> disconnectedPeersQueue = new LinkedBlockingQueue<>();

    private final Duration queueTimeOut = Duration.ofSeconds(10);

    private ScheduledExecutorService executor;

    private Map<Class<? extends Message>, List<MessageConsumer>> messageConsumers = new ConcurrentHashMap<>();

    @Autowired
    protected NetworkServiceImpl(RuntimeConfig runtimeConfig,
                                 NetConfig netConfig, ProtocolConfig protocolConfig,
                                 MessageBufferService messageBufferService,
                                 FileUtils fileUtils) {
        this.runtimeConfig = runtimeConfig;
        this.netConfig = netConfig;
        this.protocolConfig = protocolConfig;
        this.messageBufferService = messageBufferService;
        this.fileUtils = fileUtils;
        this.executor = Executors.newSingleThreadScheduledExecutor();

    }

    private void init() {
        log.info("Initalizing the handler" );

        protocolHandler = SetupHandlersBuilder.newSetup()
                .config()
                .id("HeaderSv")
                .runtime(runtimeConfig)
                .network(netConfig)
                .protocol(protocolConfig)
                .useFileUtils(fileUtils)
                .handlers()
                .custom()
                .addCallback(this::onPeerHandshaked)
                .addCallback(this::onPeerDisconnected)
                .addCallback((MessageReceivedListener) this::onMessage)
                .done();

        // We launch the Thread to process the disconneced Peers:
        executor.scheduleAtFixedRate(this::processDisconnectedPeers,
                this.queueTimeOut.toMillis(), this.queueTimeOut.toMillis(), TimeUnit.MILLISECONDS);
    }



    @Override
    public void start() {
        init();
        protocolHandler.start();
    }

    @Override
    public void stop() {
        protocolHandler.stop();
    }

    @Override
    public void send(Message message) {
       peersInfo.values().stream().filter(v-> v.isPeerConnectedStatus())
               .forEach(peer -> {
                   protocolHandler.getConnHandler().send(peer.getPeerAddress(), message);
                   log.info("Sending message: " + message + " to peer: " + peer.getPeerAddress());
               });
    }

    @Override
    public void subscribe(Class<? extends Message> eventClass, MessageConsumer messageConsumer) {
        List<MessageConsumer> consumers = new ArrayList<>();
        consumers.add(messageConsumer);
        messageConsumers.merge(eventClass, consumers, (w, prev) -> {prev.addAll(w); return prev;});
    }

    @Override
    public void unsubscribe(Class<? extends Message> eventClass, MessageConsumer messageConsumer) {
        List<MessageConsumer> consumers = new ArrayList<>();
        consumers.remove(messageConsumer);
        messageConsumers.merge(eventClass, consumers, (w, prev) -> {prev.addAll(w); return prev;});
    }

    private void processDisconnectedPeers() {
        while (!disconnectedPeersQueue.isEmpty()) messageBufferService.queue(new BufferedMessagePeer(disconnectedPeersQueue.poll()));
    }

    private void onMessage(PeerAddress peerAddress, BitcoinMsg<?> bitcoinMsg) {
        log.info("Incoming Message coming from:" + peerAddress + "type: " + bitcoinMsg.getHeader().getCommand());
        List<MessageConsumer> handlers = messageConsumers.get(bitcoinMsg.getBody().getClass());

        if(handlers == null) {
            return;
        }

        handlers.forEach(handler -> handler.consume(bitcoinMsg.getBody(), peerAddress));

    }

    private void onPeerDisconnected(PeerAddress peerAddress,  PeerDisconnectedListener.DisconnectionReason reason) {
        log.info("onPeerDisconnected: IP:" + peerAddress.toString()+":Reason:" + reason.toString());
        PeerInfo peerInfo = peersInfo.get(peerAddress);

        if(peerInfo == null)  peerInfo = new PeerInfo(peerAddress,  null, Optional.empty(), false);
        peerInfo.setPeerConnectedStatus(false);
        disconnectedPeersQueue.offer(peerInfo);

    }

    private void onPeerHandshaked(PeerAddress peerAddress, VersionMsg versionMsg) {
        log.info("onPeerHandshaked: IP:" + peerAddress.toString()+":User Agent:"+ versionMsg.getUser_agent() +": Version :" + versionMsg.getVersion());
        PeerInfo peerInfo = peersInfo.get(peerAddress);

        if (peerInfo == null) {
            peerInfo = new PeerInfo(peerAddress, versionMsg, Optional.empty(), true);
            log.info("onPeerConnected: :" + peerInfo.toString());
            peersInfo.put(peerAddress, peerInfo);
            messageBufferService.queue(new BufferedMessagePeer(peerInfo));
        }
     }

}
