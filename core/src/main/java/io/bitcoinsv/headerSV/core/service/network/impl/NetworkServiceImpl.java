package io.bitcoinsv.headerSV.core.service.network.impl;

import io.bitcoinsv.bitcoinjsv.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinsv.bitcoinjsv.params.Net;
import io.bitcoinsv.headerSV.core.service.network.NetworkConsumerConfig;
import io.bitcoinsv.headerSV.core.common.EventConsumer;
import io.bitcoinsv.headerSV.core.common.MessageConsumer;
import io.bitcoinsv.headerSV.core.service.network.NetworkService;
import io.bitcoinsv.jcl.net.network.PeerAddress;
import io.bitcoinsv.jcl.net.network.events.P2PEvent;
import io.bitcoinsv.jcl.net.network.events.PeerDisconnectedEvent;
import io.bitcoinsv.jcl.net.protocol.events.control.PeerHandshakedEvent;
import io.bitcoinsv.jcl.net.protocol.events.data.MsgReceivedEvent;
import io.bitcoinsv.jcl.net.protocol.messages.BlockHeaderMsg;
import io.bitcoinsv.jcl.net.protocol.messages.common.BitcoinMsgBuilder;
import io.bitcoinsv.jcl.net.protocol.messages.common.BodyMessage;
import io.bitcoinsv.jcl.net.protocol.messages.common.Message;
import io.bitcoinsv.jcl.net.protocol.wrapper.P2P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
public class NetworkServiceImpl extends NetworkServiceBase implements NetworkService {

    private Logger log = LoggerFactory.getLogger(NetworkServiceImpl.class);
    private NetworkConfiguration networkConfiguration;

    // Protocol Handlers: This objects will carry out the Bitcoin Protocol and perform the
    // Serialization of messages.
    private P2P p2p;
    private boolean p2pShared;

    // A Collection to keep track of the Peers handshaked:
    private List<PeerAddress> connectedPeers = Collections.synchronizedList(new ArrayList<>());

    // keep track of service state
    private boolean serviceStarted = false;


    /**
     * Constructor.
     * It creates an instance of NetworkService, using an internal P2P Service to connect to the Blockchain
     */
    public NetworkServiceImpl(NetworkConfiguration networkConfiguration) {
        this.networkConfiguration = networkConfiguration;
    }

    /**
     * Constructor.
     * It creates an instance of NetworkService, reusing a pre-existing P2P Service to connect to the blockchain
     */
    public NetworkServiceImpl(NetworkConfiguration networkConfiguration, P2P p2p) {
        this.networkConfiguration = networkConfiguration;
        this.p2p = p2p;
        this.p2pShared = true;
    }

    private void init() {
        log.info("Initalizing Network Service");

        if (p2p == null) {
            p2p = P2P.builder(networkConfiguration.getProtocolConfig().getId())
                    .config(networkConfiguration.getProtocolConfig())
                    .config(networkConfiguration.getNetworkConfig())
                    .build();
        }
        p2p.EVENTS.PEERS.DISCONNECTED.forEach(this::onPeerDisconnected);
        p2p.EVENTS.PEERS.HANDSHAKED.forEach(this::onPeerHandshaked);
        p2p.EVENTS.MSGS.ALL.forEach(this::onMessage);
    }


    @Override
    public void start() {
        serviceStarted = true;
        init();
        if (!p2pShared) {
            p2p.start();
        }
        // If some InitialConnections are specified, we connect to them after startup:
        networkConfiguration.getInitialConnections().forEach(p -> {
            log.info("Connecting manually to " + p + "...");
            p2p.REQUESTS.PEERS.connect(p).submit();
        });
        log.info("Network service started");
    }

    @Override
    public void stop() {
        serviceStarted = false;
        if (!p2pShared) {
            p2p.stop();
        }
        log.info("Network service stopped");
    }

    @Override
    public boolean broadcast(BodyMessage message, boolean requiresMinimumPeers) {
        if(requiresMinimumPeers){
            if (!checkMinimumPeersConnected()) return false;
        }

        BitcoinMsgBuilder bitcoinMsgBuilder = new BitcoinMsgBuilder<>(networkConfiguration.getProtocolConfig().getBasicConfig(), message);
        p2p.REQUESTS.MSGS.broadcast(bitcoinMsgBuilder.build()).submit();
        return true;
    }

    @Override
    public boolean send(BodyMessage message, PeerAddress peerAddress, boolean requiresMinimumPeers) {
        if(requiresMinimumPeers){
            if (!checkMinimumPeersConnected()) return false;
        }

        BitcoinMsgBuilder bitcoinMsgBuilder = new BitcoinMsgBuilder<>(networkConfiguration.getProtocolConfig().getBasicConfig(), message);
        p2p.REQUESTS.MSGS.send(peerAddress, bitcoinMsgBuilder.build()).submit();
        return true;
    }


    @Override
    public List<PeerAddress> getConnectedPeers() {
        return new ArrayList<>(connectedPeers);
    }

    @Override
    public void blacklistPeer(PeerAddress peerAddress) {
        log.info("Peer: " + peerAddress + " has been blacklisted by the application");
        p2p.REQUESTS.PEERS.blacklist(peerAddress);
    }

    @Override
    public Net getNet() {
        return networkConfiguration.getNetworkParams().getNet();
    }

    @Override
    public HeaderReadOnly getGenesisBlock() {
        return this.networkConfiguration.getGenesisBlock();
    }



    private void onPeerDisconnected(PeerDisconnectedEvent event) {
        log.debug("Peer disconnected IP:" + event.getPeerAddress().toString() + ": Reason:" + event.getReason().toString());

        connectedPeers.remove(event.getPeerAddress());
    }

    private void onPeerHandshaked(PeerHandshakedEvent event) {
        connectedPeers.add(event.getPeerAddress());
        super.onEvent(event);
    }

    @Override
    protected synchronized boolean checkMinimumPeersConnected() {
        if(connectedPeers.size() < networkConfiguration.getProtocolConfig().getBasicConfig().getMinPeers().getAsInt()) {
            if(serviceStarted) {
                log.warn("Network activity has been paused due to peer connections falling below the minimum threshold. Waiting for additional peers..");
                serviceStarted = false;
            }

            return false;
        }

        if(!serviceStarted) {
            log.warn("Network activity has resumed as peer count has risen above the minimum threshold");
            serviceStarted = true;
        }

        return true;
    }

}
