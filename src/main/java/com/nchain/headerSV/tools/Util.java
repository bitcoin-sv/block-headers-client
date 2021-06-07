package com.nchain.headerSV.tools;

import io.bitcoinj.bitcoin.Genesis;
import io.bitcoinj.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinj.params.Net;


/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2020 Bitcoin Association
  * Copyright (c) 2018-2020 nChain Ltd
 * @date 04/08/2020
 */
public class Util {

    public static HeaderReadOnly GENESIS_BLOCK_HEADER_MAINNET = Genesis.getHeaderFor(Net.MAINNET).getHeader();
    public static HeaderReadOnly GENESIS_BLOCK_HEADER_STNNET = Genesis.getHeaderFor(Net.STN).getHeader();
    public static HeaderReadOnly GENESIS_BLOCK_HEADER_TESTNET  = Genesis.getHeaderFor(Net.TESTNET3).getHeader();
    public static HeaderReadOnly GENESIS_BLOCK_HEADER_REGTEST = Genesis.getHeaderFor(Net.REGTEST).getHeader();

}
