package io.bitcoinsv.headerSV.service.sync.consumer;


import com.nchain.jcl.net.protocol.messages.common.Message;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
public interface MessageConsumer {
    /** message listener to consume given message */
    <T extends Message> void consume(T message);
}
