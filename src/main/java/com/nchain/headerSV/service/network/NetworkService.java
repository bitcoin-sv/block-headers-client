package com.nchain.headerSV.service.network;

import com.nchain.headerSV.service.consumer.MessageConsumer;
import com.nchain.jcl.net.network.PeerAddress;
import com.nchain.jcl.net.protocol.messages.common.Message;

import java.util.List;


/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 03/06/2020
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
    /** subscribe to network events */
    void subscribe(Class<? extends Message> eventClass, MessageConsumer eventHandler, boolean requiresMinimumPeers, boolean sendDuplicates);
    /** amount of connected peers */
    List<PeerAddress> getConnectedPeers();
    /** disconnects a peer */
    void disconnectPeer(PeerAddress peerAddress);
    /** blacklists a peer */
    void blacklistPeer(PeerAddress peerAddress);
}
