package com.nchain.headerSV.service.network;

import com.nchain.jcl.network.PeerAddress;
import com.nchain.jcl.protocol.messages.common.Message;
import com.nchain.headerSV.service.consumer.MessageConsumer;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 03/06/2020
 */
public interface NetworkService {
    /** Starts the Listener */
    void start();
    /** Stops the Listener */
    void stop();
    /** Publishes message to an individual peer */
    void send(Message message, PeerAddress peerAddress);
    /** Broadcasts a message to all connected peers */
    void broadcast(Message message);
    /** subscribe to network events */
    void subscribe(Class<? extends Message> eventClass, MessageConsumer eventHandler);
    /** unsubscribe to network events */
    void unsubscribe(Class<? extends Message> eventClass, MessageConsumer eventHandler);
    /** return the number of connected peers*/
    int getConnectedPeersCount();
}
