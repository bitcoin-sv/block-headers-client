package io.bitcoinsv.headerSV.core.api;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
public enum ChainState {
    LONGEST_CHAIN,
    ORPHAN,
    STALE
}
