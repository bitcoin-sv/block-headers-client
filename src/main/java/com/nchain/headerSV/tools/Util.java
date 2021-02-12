package com.nchain.headerSV.tools;




import com.nchain.jcl.net.protocol.messages.BlockHeaderMsg;
import com.nchain.jcl.net.protocol.messages.HashMsg;
import io.bitcoinj.bitcoin.Genesis;
import io.bitcoinj.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinj.params.Net;

import java.math.BigInteger;

/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2020 Bitcoin Association
  * Copyright (c) 2018-2020 nChain Ltd
 * @date 04/08/2020
 */
public class Util {

    public static BigInteger decompressCompactBits(long targetBits){
        byte[] targetBitsByteArray = BigInteger.valueOf(targetBits).toByteArray();

        BigInteger index = new BigInteger(targetBitsByteArray, 0, 1);
        BigInteger coefficent = new BigInteger(targetBitsByteArray, 1, 3);

        return coefficent.multiply(BigInteger.valueOf(2).pow(BigInteger.valueOf(8).multiply(index.subtract(BigInteger.valueOf(3L))).intValue()));
    }

    public static HeaderReadOnly GENESIS_BLOCK_HEADER_MAINNET = Genesis.getHeaderFor(Net.MAINNET).getHeader();
    public static HeaderReadOnly GENESIS_BLOCK_HEADER_STNNET = Genesis.getHeaderFor(Net.STN).getHeader();
    public static HeaderReadOnly GENESIS_BLOCK_HEADER_TESTNET  = Genesis.getHeaderFor(Net.TESTNET3).getHeader();

}
