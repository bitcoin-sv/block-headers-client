package io.bitcoinsv.headerSV.core.service.network.impl;

import io.bitcoinsv.bitcoinjsv.bitcoin.Genesis;
import io.bitcoinsv.bitcoinjsv.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinsv.bitcoinjsv.params.Net;


/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
public class NetworkUtils {

    public static HeaderReadOnly GENESIS_BLOCK_HEADER_MAINNET = Genesis.getHeaderFor(Net.MAINNET).getHeader();
    public static HeaderReadOnly GENESIS_BLOCK_HEADER_STNNET = Genesis.getHeaderFor(Net.STN).getHeader();
    public static HeaderReadOnly GENESIS_BLOCK_HEADER_TESTNET  = Genesis.getHeaderFor(Net.TESTNET3).getHeader();
    public static HeaderReadOnly GENESIS_BLOCK_HEADER_REGTEST = Genesis.getHeaderFor(Net.REGTEST).getHeader();

}
