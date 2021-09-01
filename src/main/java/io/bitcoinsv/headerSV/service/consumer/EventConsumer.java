package io.bitcoinsv.headerSV.service.consumer;


import io.bitcoinsv.jcl.net.network.PeerAddress;
import io.bitcoinsv.jcl.net.network.events.P2PEvent;
import io.bitcoinsv.jcl.net.protocol.messages.common.BitcoinMsg;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
public interface EventConsumer {
    /** method to consume given event */
    void consume(P2PEvent event);
}
