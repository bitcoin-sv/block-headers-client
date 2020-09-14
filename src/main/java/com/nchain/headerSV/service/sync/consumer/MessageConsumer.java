package com.nchain.headerSV.service.sync.consumer;


import com.nchain.jcl.net.protocol.messages.common.Message;

/**
 * @author {m.fletcher}@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 28/07/2020
 */
public interface MessageConsumer {
    /** message listener to consume given message */
    <T extends Message> void consume(T message);
}
