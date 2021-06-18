package io.bitcoinsv.headerSV.tools;

import io.bitcoinj.bitcoin.Genesis;
import io.bitcoinj.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinj.params.Net;


/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
public class Util {

    public static HeaderReadOnly GENESIS_BLOCK_HEADER_MAINNET = Genesis.getHeaderFor(Net.MAINNET).getHeader();
    public static HeaderReadOnly GENESIS_BLOCK_HEADER_STNNET = Genesis.getHeaderFor(Net.STN).getHeader();
    public static HeaderReadOnly GENESIS_BLOCK_HEADER_TESTNET  = Genesis.getHeaderFor(Net.TESTNET3).getHeader();
    public static HeaderReadOnly GENESIS_BLOCK_HEADER_REGTEST = Genesis.getHeaderFor(Net.REGTEST).getHeader();

}
