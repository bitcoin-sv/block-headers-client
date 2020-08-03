package com.nchain.headerSV.service.sync.consumer;

import com.nchain.bna.protocol.messages.common.Message;

/**
 * @author {m.fletcher}@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 28/07/2020
 */
public interface MessageConsumer {
    /** message listener to consume given message */
    <T extends Message> void consume(T message);
}
