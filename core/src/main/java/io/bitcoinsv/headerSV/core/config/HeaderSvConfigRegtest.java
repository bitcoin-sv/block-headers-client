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
 * DEFAULT Configuration for REGTEST
 */
public class HeaderSvConfigRegtest extends HeaderSvConfig {

    private final static Set<String> HEADERS_TO_IGNORE = Collections.emptySet();

    /** Constructor */
    HeaderSvConfigRegtest(Duration timeoutToTriggerSyncComplete) {
        super(timeoutToTriggerSyncComplete, HEADERS_TO_IGNORE);
    }

    /** Constructor */
    HeaderSvConfigRegtest() {
        super(HEADERS_TO_IGNORE);
    }

    public static HeaderSvConfigRegtestBuilder builder() {
        return new HeaderSvConfigRegtestBuilder();
    }

    /**
     * Builder
     */
    public static class HeaderSvConfigRegtestBuilder extends HeaderSvConfigBuilder {
        @Override
        public HeaderSvConfigRegtestBuilder headersToIgnore(Set<String> headersToIgnore) {
            throw new UnsupportedOperationException("Headers to ignore are already pre-configured in this Builder");
        }
        @Override
        public HeaderSvConfigRegtest build() {
            return new HeaderSvConfigRegtest(super.timeoutToTriggerSyncComplete);
        }
    }
}
