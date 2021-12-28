package io.bitcoinsv.headerSV.core.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author i.fernandez@nchain.com
 *
 * This class is the Configuration of the HeaderSV Service.
 * DEFAULT Configuration for MAINNET
 */
public class HeaderSvConfigMainnet extends HeaderSvConfig {

    private final static Set<String> HEADERS_TO_IGNORE = new HashSet(Arrays.asList(
            "000000000000000000afe19d2ba3afbbc2627b1a6d7ee2425f998ddabd6134ed",
            "00000000000000000019f1679932c8a69051fca08d0934ac0c6cad56077d0c66",
            "0000000000000000004626ff6e3b936941d341c5932ece4357eeccac44e6d56c",
            "0000000000000000055de705ef722c11d90c1a52de52c21aa646c6bb46de3770"
    ));

    /** Constructor */
    HeaderSvConfigMainnet(Duration timeoutToTriggerSyncComplete) {
        super(timeoutToTriggerSyncComplete, HEADERS_TO_IGNORE);
    }

    /** Constructor */
    HeaderSvConfigMainnet() {
        super(HEADERS_TO_IGNORE);
    }

    public static HeaderSvConfigMainnetBuilder builder() {
        return new HeaderSvConfigMainnetBuilder();
    }

    /**
     * Builder
     */
    public static class HeaderSvConfigMainnetBuilder extends HeaderSvConfigBuilder {
        @Override
        public HeaderSvConfigMainnetBuilder headersToIgnore(Set<String> headersToIgnore) {
            throw new UnsupportedOperationException("Headers to ignore are already pre-configured in this Builder");
        }
        @Override
        public HeaderSvConfigMainnet build() {
            return new HeaderSvConfigMainnet(super.timeoutToTriggerSyncComplete);
        }
    }
}
