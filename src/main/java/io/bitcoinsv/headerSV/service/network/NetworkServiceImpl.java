package io.bitcoinsv.headerSV.service.network;

import io.bitcoinsv.jcl.net.network.PeerAddress;
import io.bitcoinsv.jcl.net.network.events.P2PEvent;
import io.bitcoinsv.jcl.net.network.events.PeerDisconnectedEvent;
import io.bitcoinsv.jcl.net.protocol.events.control.PeerHandshakedEvent;
import io.bitcoinsv.jcl.net.protocol.events.data.MsgReceivedEvent;
import io.bitcoinsv.jcl.net.protocol.messages.common.BitcoinMsgBuilder;
import io.bitcoinsv.jcl.net.protocol.messages.common.Message;
import io.bitcoinsv.jcl.net.protocol.wrapper.P2P;

import io.bitcoinsv.headerSV.config.NetworkConfiguration;
import io.bitcoinsv.headerSV.service.consumer.ConsumerConfig;
import io.bitcoinsv.headerSV.service.consumer.EventConsumer;
import io.bitcoinsv.headerSV.service.consumer.MessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
@Service
@Slf4j
public class NetworkServiceImpl implements NetworkService {

    private NetworkConfiguration networkConfiguration;

    // Protocol Handlers: This objects will carry out the Bitcoin Protocol and perform the
    // Serialization of messages.
    private P2P p2p;

    // A Collection to keep track of the Peers handshaked:
    private List<PeerAddress> connectedPeers = Collections.synchronizedList(new ArrayList<>());

    //Map all the subscribers by message
    private Map<Class<? extends Message>, Map<MessageConsumer, ConsumerConfig>> messageConsumers = new ConcurrentHashMap<>();
    private Map<Class<? extends P2PEvent>, List<EventConsumer>> eventConsumers = new ConcurrentHashMap<>();

    // keep track of service state
    private boolean serviceStarted = false;

    //keep track of received messages
    private Set<Long> processedMessages = Collections.synchronizedSet(new HashSet<>());

    @Autowired
    protected NetworkServiceImpl(NetworkConfiguration networkConfiguration) {
        this.networkConfiguration = networkConfiguration;
    }

    private void init() {
        log.info("Initalizing Network Service");


        p2p = P2P.builder(networkConfiguration.getProtocolConfig().getId())
                .config(networkConfiguration.getProtocolConfig())
                .config(networkConfiguration.getJCLNetworkConfig())
                .build();

        p2p.EVENTS.PEERS.DISCONNECTED.forEach(this::onPeerDisconnected);
        p2p.EVENTS.PEERS.HANDSHAKED.forEach(this::onPeerHandshaked);
        p2p.EVENTS.MSGS.ALL.forEach(this::onMessage);
    }


    @Override
    public void start() {
        serviceStarted = true;
        init();
        p2p.start();
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
        p2p.stop();
        log.info("Network service stopped");
    }

    @Override
    public void broadcast(Message message, boolean requiresMinimumPeers) {
        if(requiresMinimumPeers){
            checkMinimumPeersConnected();
        }

        BitcoinMsgBuilder bitcoinMsgBuilder = new BitcoinMsgBuilder<>(networkConfiguration.getProtocolConfig().getBasicConfig(), message);
        p2p.REQUESTS.MSGS.broadcast(bitcoinMsgBuilder.build()).submit();
    }

    @Override
    public void send(Message message, PeerAddress peerAddress, boolean requiresMinimumPeers) {
        if(requiresMinimumPeers){
            checkMinimumPeersConnected();
        }

        BitcoinMsgBuilder bitcoinMsgBuilder = new BitcoinMsgBuilder<>(networkConfiguration.getProtocolConfig().getBasicConfig(), message);
        p2p.REQUESTS.MSGS.send(peerAddress, bitcoinMsgBuilder.build()).submit();
    }

    @Override
    public void subscribe(Class<? extends Message> eventClass, MessageConsumer messageConsumer, boolean requiresMinimumPeers, boolean sendDuplicates) {
        HashMap<MessageConsumer, ConsumerConfig> entry = new HashMap<>();
        entry.put(messageConsumer, ConsumerConfig.builder()
                .requiresMinimumPeers(Boolean.valueOf(requiresMinimumPeers))
                .sendDuplicates(sendDuplicates)
                .build());

        messageConsumers.merge(eventClass, entry, (w, prev) -> {
            prev.putAll(w);
            return prev;
        });
    }

    @Override
    public void subscribe(Class<? extends P2PEvent> eventClass, EventConsumer messageConsumer) {
        List<EventConsumer> entry = new ArrayList<>();
        entry.add(messageConsumer);

        eventConsumers.merge(eventClass, entry, (w, prev) -> {
            prev.addAll(w);
            return prev;
        });
    }

    @Override
    public List<PeerAddress> getConnectedPeers() {
        return new ArrayList<>(connectedPeers);
    }

    @Override
    public void disconnectPeer(PeerAddress peerAddress) {
        log.info("Peer: " + peerAddress + " has been disconnected by the application");
        p2p.REQUESTS.PEERS.disconnect(peerAddress);
    }

    @Override
    public void blacklistPeer(PeerAddress peerAddress) {
        log.info("Peer: " + peerAddress + " has been blacklisted by the application");
        p2p.REQUESTS.PEERS.blacklist(peerAddress);
    }

    private void onMessage(MsgReceivedEvent msgReceivedEvent) {
        log.debug("Incoming Message coming from:" + msgReceivedEvent.getPeerAddress() + "type: " + msgReceivedEvent.getBtcMsg().getHeader().getCommand());

        Map<MessageConsumer, ConsumerConfig> handlers = messageConsumers.get(msgReceivedEvent.getBtcMsg().getBody().getClass());

        if (handlers == null) {
            return;
        }

        handlers.forEach((consumer, config) -> {
            if (config.isRequiresMinimumPeers()) {
                if(!checkMinimumPeersConnected()) {
                    log.info("Message " + msgReceivedEvent.getBtcMsg().getHeader().getCommand() + " rejected. Not enough connected peers.");
                    return;
                }
            }

            // Check if we've already processed this header message
            if(!config.isSendDuplicates()) {
                if (processedMessages.contains(msgReceivedEvent.getBtcMsg().getHeader().getChecksum())) {
                    return;
                } else {
                    processedMessages.add(msgReceivedEvent.getBtcMsg().getHeader().getChecksum());
                }
            }

            consumer.consume(msgReceivedEvent.getBtcMsg(), msgReceivedEvent.getPeerAddress());
        });
    }

    private void onPeerDisconnected(PeerDisconnectedEvent event) {
        log.debug("Peer disconnected IP:" + event.getPeerAddress().toString() + ": Reason:" + event.getReason().toString());

        connectedPeers.remove(event.getPeerAddress());
    }

    private void onPeerHandshaked(PeerHandshakedEvent event) {
        log.debug("Peer connected IP:" + event.getPeerAddress().toString() + ": User Agent:" + event.getVersionMsg().getUser_agent() + ": Version :" + event.getVersionMsg().getVersion());

        connectedPeers.add(event.getPeerAddress());

        eventConsumers.get(event.getClass()).forEach(c -> c.consume(event));
    }

    private synchronized boolean checkMinimumPeersConnected() {
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
