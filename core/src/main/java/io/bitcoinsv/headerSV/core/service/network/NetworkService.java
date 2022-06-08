package io.bitcoinsv.headerSV.core.service.network;

import io.bitcoinsv.bitcoinjsv.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinsv.bitcoinjsv.params.Net;
import io.bitcoinsv.headerSV.core.common.EventConsumer;
import io.bitcoinsv.headerSV.core.common.MessageConsumer;
import io.bitcoinsv.jcl.net.network.PeerAddress;
import io.bitcoinsv.jcl.net.network.events.P2PEvent;
import io.bitcoinsv.jcl.net.protocol.messages.common.BodyMessage;
import io.bitcoinsv.jcl.net.protocol.messages.common.Message;
import io.bitcoinsv.jcl.tools.events.Event;

import java.util.List;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 * @author i.fernandez@nchain.com
 */
public interface NetworkService {
    /** Starts the Listener */
    void start();
    /** Stops the Listener */
    void stop();

    /** Returns the Net this Service is connected to*/
    Net getNet();

    /**
     * Returns the Genesis Block of the Network used by this Service.
     */
    HeaderReadOnly getGenesisBlock();

    /**
     * Publishes message to an individual peer. It returns True if the message has been sent, or FALSE if not becasue
     * we are not connected to enough peers
     */
    boolean send(BodyMessage message, PeerAddress peerAddress, boolean requiresMinimumPeers);
    /**
     * Broadcasts a message to all connected peers It returns True if the message has been sent, or FALSE if not becasue
     * we are not connected to enough peers
     */
    boolean broadcast(BodyMessage message, boolean requiresMinimumPeers);

    /** List of connected peers */
    List<PeerAddress> getConnectedPeers();

    /** subscribe to network messages */
    void subscribe(Class<? extends Message> eventClass, MessageConsumer eventHandler, boolean requiresMinimumPeers, boolean sendDuplicates);
    /** subscribe to events */
    void subscribe(Class<? extends Event> eventClass, EventConsumer eventConsumer);
    /** blacklists a peer */
    void blacklistPeer(PeerAddress peerAddress);

}
