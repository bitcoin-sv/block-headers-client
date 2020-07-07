package com.nchain.headerSV.service.listener;

import com.nchain.bna.network.PeerAddress;
import com.nchain.bna.network.config.NetConfig;
import com.nchain.bna.network.listeners.PeerDisconnectedListener;
import com.nchain.bna.protocol.config.ProtocolConfig;
import com.nchain.bna.protocol.handlers.SetupHandlersBuilder;
import com.nchain.bna.protocol.listeners.PeerHandshakeAcceptedListener;
import com.nchain.bna.protocol.messages.VersionMsg;
import com.nchain.bna.tools.RuntimeConfig;
import com.nchain.bna.tools.files.FileUtils;
import com.nchain.headerSV.domain.PeerInfo;
import com.nchain.headerSV.service.propagation.buffer.BufferedMessagePeer;
import com.nchain.headerSV.service.propagation.buffer.MessageBufferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
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
public class ListenerServiceImpl implements ListenerService {

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

    @Autowired
    protected ListenerServiceImpl(RuntimeConfig runtimeConfig,
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
        // We connect all the Handlers together:
        protocolHandler = SetupHandlersBuilder.newSetup()
                .config()
                .id("HeaderSv")
                .runtime(runtimeConfig)
                .network(netConfig)
                .protocol(protocolConfig)
                .handlers()
                .useFileUtils(fileUtils)
                .custom()
                .addCallback((PeerHandshakeAcceptedListener) this::onPeerHandshaked)
                .addCallback((PeerDisconnectedListener) this::onPeerDisconnected)
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

    private void processDisconnectedPeers() {
        while (!disconnectedPeersQueue.isEmpty()) messageBufferService.queue(new BufferedMessagePeer(disconnectedPeersQueue.poll()));
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
            peerInfo = new PeerInfo(peerAddress, versionMsg, Optional.empty(), true); // fee is null at this point
            log.info("onPeerConnected: :" + peerInfo.toString());
            peersInfo.put(peerAddress, peerInfo);
            messageBufferService.queue(new BufferedMessagePeer(peerInfo));
        }
     }

}
