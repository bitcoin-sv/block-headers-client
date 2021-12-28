package io.bitcoinsv.headerSV.core.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author i.fernandez@nchain.com
 *
 * This class is the Configuration of the HeaderSV Service.
 * DEFAULT Configuration for STNNET
 */
public class HeaderSvConfigStnnet extends HeaderSvConfig {

    private final static Set<String> HEADERS_TO_IGNORE = Collections.emptySet();

    /** Constructor */
    HeaderSvConfigStnnet(Duration timeoutToTriggerSyncComplete) {
        super(timeoutToTriggerSyncComplete, HEADERS_TO_IGNORE);
    }

    /** Constructor */
    HeaderSvConfigStnnet() {
        super(HEADERS_TO_IGNORE);
    }

    public static HeaderSvConfigStnnettBuilder builder() {
        return new HeaderSvConfigStnnettBuilder();
    }

    /**
     * Builder
     */
    public static class HeaderSvConfigStnnettBuilder extends HeaderSvConfigBuilder {
        @Override
        public HeaderSvConfigStnnettBuilder headersToIgnore(Set<String> headersToIgnore) {
            throw new UnsupportedOperationException("Headers to ignore are already pre-configured in this Builder");
        }
        @Override
        public HeaderSvConfigStnnet build() {
            return new HeaderSvConfigStnnet(super.timeoutToTriggerSyncComplete);
        }
    }
}
