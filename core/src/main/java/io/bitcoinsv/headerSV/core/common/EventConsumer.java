package io.bitcoinsv.headerSV.core.common;


import io.bitcoinsv.jcl.tools.events.Event;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
public interface EventConsumer {
    /** method to consume given event */
    void consume(Event event);
}
