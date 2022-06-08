package io.bitcoinsv.headerSV.core.events;

import io.bitcoinsv.jcl.tools.events.EventBus;
import io.bitcoinsv.jcl.tools.events.EventStreamer;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author i.fernandez@nchain.com
 *
 * This class is aplaceholder for all the EventStreamer that allow clients to subscribe to the different Events
 * triggered by the HeaderSV Service:
 *
 *  - TipsUpdatedEvent :Triggered every time we update our TIPS based on the HEADERs received from Peers
 *  - ChainSynchronizedEvent: Triggered only once, when we first synchornized and reach the TIP of the chain
 *
 * @see TipsUpdatedEvent
 * @see ChainSynchronizedEvent
 */
public class HeaderSvEventStreamer {

    // EventBus used to publish the Events
    private EventBus eventBus;

    /** Constructor */
    public HeaderSvEventStreamer(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /** Returns a streamer we can use to subscribe to "ChainSynchronizedEvent" events */
    public EventStreamer<ChainSynchronizedEvent> CHAIN_SYNCHRONIZED() {
        return new EventStreamer<>(this.eventBus, ChainSynchronizedEvent.class);
    }

    /** Returns a streamer we can use to subscribe to "TipsUpdatedEvent" events */
    public EventStreamer<TipsUpdatedEvent> TIPS_UPDATED() {
        return new EventStreamer<>(this.eventBus, TipsUpdatedEvent.class);
    }
}
