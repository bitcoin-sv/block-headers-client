package io.bitcoinsv.headerSV.core;

import io.bitcoinsv.headerSV.core.api.HeaderSvApi;
import io.bitcoinsv.headerSV.core.events.HeaderSvEventStreamer;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 * @author i.fernandez@nchain.com
 *
 * The HeaderSV Serive runs in the background while it sychronized with the Blockchain. It provides access to an
 * API with methods to retrieve infomration about the chain. If we want to make sure that we are fully synchornized
 * with the chain before we use this API, we can use the EVENTS triggered by the EventStreamer provided by this
 * serice, which can notify us when we reach the TIP.
 */
public interface HeaderSvService {
    /** Starts the Service */
    void start();
    /** Stops the Service */
    void stop();
    /** Returns a reference to the API that provides data about the chain */
    HeaderSvApi API();
    /**
     * Returns a reference to the Event Streamer of this Service. Clients can subscribe to events and react to them
     * asynchronously
     */
    HeaderSvEventStreamer EVENTS();
}