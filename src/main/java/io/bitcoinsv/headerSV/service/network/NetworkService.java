package io.bitcoinsv.headerSV.service.network;

import io.bitcoinsv.jcl.net.network.PeerAddress;
import io.bitcoinsv.jcl.net.network.events.P2PEvent;
import io.bitcoinsv.jcl.net.protocol.messages.common.Message;

import io.bitcoinsv.headerSV.service.consumer.EventConsumer;
import io.bitcoinsv.headerSV.service.consumer.MessageConsumer;

import java.util.List;


/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
public interface NetworkService {
    /** Starts the Listener */
    void start();
    /** Stops the Listener */
    void stop();
    /** Publishes message to an individual peer */
    void send(Message message, PeerAddress peerAddress, boolean requiresMinimumPeers);
    /** Broadcasts a message to all connected peers */
    void broadcast(Message message, boolean requiresMinimumPeers);
    /** subscribe to network messages */
    void subscribe(Class<? extends Message> eventClass, MessageConsumer eventHandler, boolean requiresMinimumPeers, boolean sendDuplicates);
    /** subscribe to p2p events */
    void subscribe(Class<? extends P2PEvent> eventClass, EventConsumer eventConsumer);
    /** amount of connected peers */
    List<PeerAddress> getConnectedPeers();
    /** disconnects a peer */
    void disconnectPeer(PeerAddress peerAddress);
    /** blacklists a peer */
    void blacklistPeer(PeerAddress peerAddress);
}
