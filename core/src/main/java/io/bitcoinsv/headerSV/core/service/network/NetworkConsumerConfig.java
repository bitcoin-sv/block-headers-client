package io.bitcoinsv.headerSV.core.service.network;
/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 * @author i.fernandez@nchain.com
 *
 * This class represetns the Configuration of a Consumer of an Event. Its used when we subscribe to an Event in
 * the Network Service. When subsribing to an Event, we are notifying about that event or not, depending on the
 * conditions specified here.
 */
public class NetworkConsumerConfig {
    // If False, we are notified only if the event (or the msg it represents) has NOT being processed before
    private boolean sendDuplicates;
    // If True, we are notified about the event ONLY if the Network Service is connected to enough peers
    private boolean requiresMinimumPeers;

    /** Constructor */
    private NetworkConsumerConfig(boolean sendDuplicates, boolean requiresMinimumPeers) {
        this.sendDuplicates = sendDuplicates;
        this.requiresMinimumPeers = requiresMinimumPeers;
    }

    public boolean isSendDuplicates() { return this.sendDuplicates;}
    public boolean isRequiresMinimumPeers() { return this.requiresMinimumPeers;}

    public static ConsumerConfigBuilder builder() {
        return new ConsumerConfigBuilder();
    }

    /**
     * Builder
     */
    public static class ConsumerConfigBuilder {
        private boolean sendDuplicates;
        private boolean requiresMinimumPeers;

        public ConsumerConfigBuilder sendDuplicates(boolean sendDuplicates) {
            this.sendDuplicates = sendDuplicates;
            return this;
        }

        public ConsumerConfigBuilder requiresMinimumPeers(boolean requiresMinimumPeers) {
            this.requiresMinimumPeers = requiresMinimumPeers;
            return this;
        }

        public NetworkConsumerConfig build() {
            return new NetworkConsumerConfig(this.sendDuplicates, this.requiresMinimumPeers);
        }
    }
}
