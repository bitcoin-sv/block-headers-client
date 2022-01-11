package io.bitcoinsv.headerSV.core.config;

import io.bitcoinsv.bitcoinjsv.params.Net;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author i.fernandez@nchain.com
 *
 * This class is the Configuration of the HeaderSV Service
 */
public class HeaderSvConfig {

    // Default time we wait until without getting any HEADERs before we assume we've reached the TIP of the chain
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
    // By default, we broadcast INV to let Peers know about our Height in the Chain
    private static final boolean DEFAULT_INV_BROADCAST_ENABLED = true;

    // If we haven't received any HEADERs after this time, we assume we have synchronized with the whole blockchain
    private Duration timeoutToTriggerSyncComplete;

    // If FALSE, then The synchronization will rely only on GET_HEADERS/HEADERS message, without processing incoming
    // INVs
    private boolean invBroadcastEnabled;

    // HEADERs messages containing these Hashes are ignored
    private Set<String> headersToIgnore;

    /** Constructor */
    protected HeaderSvConfig(Duration timeoutToTriggerSyncComplete,
                             boolean invBroadcastEnabled,
                             Set<String> headersToIgnore) {
        this.timeoutToTriggerSyncComplete = (timeoutToTriggerSyncComplete != null)? timeoutToTriggerSyncComplete : DEFAULT_TIMEOUT;
        this.invBroadcastEnabled = invBroadcastEnabled;
        this.headersToIgnore = headersToIgnore;
    }

    /** Constructor */
    protected HeaderSvConfig(Set<String> headersToIgnore) {
        this(DEFAULT_TIMEOUT, DEFAULT_INV_BROADCAST_ENABLED, headersToIgnore);
    }

    /** Constructor */
    protected HeaderSvConfig(Duration timeoutToTriggerSyncComplete, Set<String> headersToIgnore) {
        this(timeoutToTriggerSyncComplete, DEFAULT_INV_BROADCAST_ENABLED, headersToIgnore);
    }

    /** Constructor */
    protected HeaderSvConfig(boolean invBroadcastEnabled, Set<String> headersToIgnore) {
        this(DEFAULT_TIMEOUT, invBroadcastEnabled, headersToIgnore);
    }

    public Duration getTimeoutToTriggerSyncComplete()   { return this.timeoutToTriggerSyncComplete;}
    public Set<String> getHeadersToIgnore()             { return this.headersToIgnore;}
    public boolean isInvBroadcastEnabled()              { return this.invBroadcastEnabled;}

    /**
     * It returns a default and ready-to-use HeaderSVService Configuration based on the Network
     */
    public static HeaderSvConfig of(Net net) {
        switch (net) {
            case MAINNET:{
                return new HeaderSvConfigMainnet();
            }
            case REGTEST:{
                return new HeaderSvConfigRegtest();
            }
            case STN:{
                return new HeaderSvConfigStnnet();
            }
            case TESTNET3:{
                return new HeaderSvConfigTestnet();
            }
        } // switch
        throw new RuntimeException("No Network Supported: " + net);
    }

    public static HeaderSvConfigBuilder builder() {
        return new HeaderSvConfigBuilder();
    }

    /**
     * Builder
     */
    public static class HeaderSvConfigBuilder {
        protected Duration timeoutToTriggerSyncComplete;
        protected Set<String> headersToIgnore = Collections.emptySet();
        protected boolean invBroadcastEnabled = DEFAULT_INV_BROADCAST_ENABLED;

        public HeaderSvConfigBuilder timeoutToTriggerSyncComplete(Duration timeoutToTriggerSyncComplete) {
            this.timeoutToTriggerSyncComplete = timeoutToTriggerSyncComplete;
            return this;
        }

        public HeaderSvConfigBuilder headersToIgnore(Set<String> headersToIgnore) {
            this.headersToIgnore = headersToIgnore;
            return this;
        }

        public HeaderSvConfigBuilder invBroadcastEnabled(boolean invBroadcastEnabled) {
            this.invBroadcastEnabled = invBroadcastEnabled;
            return this;
        }

        public HeaderSvConfig build() {
            return new HeaderSvConfig(this.timeoutToTriggerSyncComplete, this.invBroadcastEnabled, this.headersToIgnore);
        }
    }
}
