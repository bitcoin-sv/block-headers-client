package io.bitcoinsv.headerSV.core.events;

import io.bitcoinsv.bitcoinjsv.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinsv.bitcoinjsv.core.Sha256Hash;
import io.bitcoinsv.jcl.net.network.PeerAddress;
import io.bitcoinsv.jcl.tools.events.Event;

import java.util.List;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * author i.fernandez@nchain.com
 * <p>
 * This class represents an Event triggered when the HeaderSV Service updates its stored TIPS based on the new HEADERs
 * received from Peers. This even is triggered several times during Shyncrhonization: while we are still truying to
 * reach the TIP of the chian, the eents are triggered quite quickk every feww seconds or so. After we reach the TIP
 * of the chain, then flow if incoming HEADERS will be the rate of blocks being mined, so we can expect one event
 * every 10 mins on average.
 * @see ChainSynchronizedEvent
 * @see HeaderSvEventStreamer
 */
public class TipsUpdatedEvent extends Event {
    // List of current Tips stored
    private List<Sha256Hash> updatedTips;
    // New Headers that have updated the TIPs
    private List<HeaderReadOnly> newHeaders;
    private PeerAddress peerAddress;

    /**
     * Constructor
     */
    private TipsUpdatedEvent(List<Sha256Hash> updatedTips, List<HeaderReadOnly> newHeaders, PeerAddress peerAddress) {
        this.updatedTips = updatedTips;
        this.newHeaders = newHeaders;
        this.peerAddress = peerAddress;
    }

    public List<Sha256Hash> getUpdatedTips() {
        return updatedTips;
    }

    public List<HeaderReadOnly> getNewHeaders() {
        return newHeaders;
    }

    public PeerAddress getPeerAddress() {
        return peerAddress;
    }

    public static TipsUpdatedEventBuilder builder() {
        return new TipsUpdatedEventBuilder();
    }

    /**
     * Builder
     */
    public static class TipsUpdatedEventBuilder {
        private List<Sha256Hash> updatedTips;
        private List<HeaderReadOnly> newHeaders;
        private PeerAddress peerAddress;

        public TipsUpdatedEventBuilder updatedTips(List<Sha256Hash> updatedTips) {
            this.updatedTips = updatedTips;
            return this;
        }

        public TipsUpdatedEventBuilder newHeaders(List<HeaderReadOnly> newHeaders) {
            this.newHeaders = newHeaders;
            return this;
        }

        public TipsUpdatedEventBuilder peerAddress(PeerAddress peerAddress) {
            this.peerAddress = peerAddress;
            return this;
        }

        public TipsUpdatedEvent build() {
            return new TipsUpdatedEvent(updatedTips, newHeaders, peerAddress);
        }
    }
}
