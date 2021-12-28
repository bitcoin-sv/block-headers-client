package io.bitcoinsv.headerSV.core.events;

import io.bitcoinsv.bitcoinjsv.bitcoin.api.extended.ChainInfo;
import io.bitcoinsv.jcl.tools.events.Event;

import java.util.List;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author i.fernandez@nchain.com
 *
 * This class represents an Event triggered when the HeaderSV Service has shynchornized with the whole chain. Its
 * triggered in 2 scenarios:
 *  - When we are shynchornizing, we reach the TIP, and then we do NOT received any HEADERS for some time (configurable)
 *  - When we start over, and we are already shynchronized with the TIP (from a previous run), and we do NOT receive any
 *    Headers after some time.
 *
 * So this Event is only triggered once. After synchronized with the TIP, the Serice will keep udating the chain
 * every time a block is main and broadcast, but in that case a "TipsUpdatedEvent" will be triggered every time.
 *
 * @see TipsUpdatedEvent
 * @see HeaderSvEventStreamer
 */
public class ChainSynchronizedEvent extends Event {

    // Current Tips of the Chain we have stored
    private List<ChainInfo> tips;

    /** Constructor */
    private ChainSynchronizedEvent(List<ChainInfo> tips) {
        this.tips = tips;
    }
    public List<ChainInfo> getTips() { return this.tips;}

    public static ChainSynchronizedEventBuilder builder() {
        return new ChainSynchronizedEventBuilder();
    }

    /**
     * Builder
     */
    public static class ChainSynchronizedEventBuilder {
        private List<ChainInfo> tips;

        public ChainSynchronizedEventBuilder tips(List<ChainInfo> tips) {
            this.tips = tips;
            return this;
        }

        public ChainSynchronizedEvent build() {
            return new ChainSynchronizedEvent(this.tips);
        }
    }

}
