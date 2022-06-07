package io.bitcoinsv.headerSV.core.config;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author i.fernandez@nchain.com
 *
 * This class is the Configuration of the HeaderSV Service.
 * DEFAULT Configuration for TESTNET
 */
public class HeaderSvConfigTestnet extends HeaderSvConfig {

    private final static Set<String> HEADERS_TO_IGNORE = Collections.emptySet();

    /** Constructor */
    HeaderSvConfigTestnet(Duration timeoutToTriggerSyncComplete) {
        super(timeoutToTriggerSyncComplete, HEADERS_TO_IGNORE);
    }

    /** Constructor */
    HeaderSvConfigTestnet() {
        super(HEADERS_TO_IGNORE);
    }

    public static HeaderSvConfigTestnetBuilder builder() {
        return new HeaderSvConfigTestnetBuilder();
    }

    /**
     * Builder
     */
    public static class HeaderSvConfigTestnetBuilder extends HeaderSvConfigBuilder {
        @Override
        public HeaderSvConfigTestnetBuilder headersToIgnore(Set<String> headersToIgnore) {
            throw new UnsupportedOperationException("Headers to ignore are already pre-configured in this Builder");
        }
        @Override
        public HeaderSvConfigTestnet build() {
            return new HeaderSvConfigTestnet(super.timeoutToTriggerSyncComplete);
        }
    }
}
