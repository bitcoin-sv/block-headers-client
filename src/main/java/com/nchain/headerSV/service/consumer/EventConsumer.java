package com.nchain.headerSV.service.consumer;


import com.nchain.jcl.net.network.PeerAddress;
import com.nchain.jcl.net.network.events.P2PEvent;
import com.nchain.jcl.net.protocol.messages.common.BitcoinMsg;

/**
 * @author {m.fletcher}@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 28/07/2020
 */
public interface EventConsumer {
    /** method to consume given event */
    void consume(P2PEvent event);
}
