package com.nchain.headerSV.tools;

import com.nchain.jcl.protocol.messages.HashMsg;
import com.nchain.jcl.tools.crypto.Sha256Wrapper;
import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;

import java.math.BigInteger;

/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 04/08/2020
 */
public class Util {

    public static BigInteger calculateWork(BigInteger target){
        return BigInteger.ONE.shiftLeft(256).divide(target.add(BigInteger.ONE));
    }

    public static BigInteger decompressCompactBits(long targetBits){
        byte[] targetBitsByteArray = BigInteger.valueOf(targetBits).toByteArray();

        BigInteger index = new BigInteger(targetBitsByteArray, 0, 1);
        BigInteger coefficent = new BigInteger(targetBitsByteArray, 1, 3);

        return coefficent.multiply(BigInteger.valueOf(2).pow(BigInteger.valueOf(8).multiply(index.subtract(BigInteger.valueOf(3L))).intValue()));
    }

    public static BlockHeader GENESIS_BLOCK_HEADER = BlockHeader.builder()
            .prevBlockHash(HashMsg.builder().hash(Sha256Wrapper.ZERO_HASH.getBytes()).build().toString())
            .hash(HashMsg.builder().hash(Sha256Wrapper.wrap("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f").getBytes()).build().toString())
            .version(1)
            .merkleRoot(HashMsg.builder().hash(Sha256Wrapper.wrap("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b").getBytes()).build().toString())
            .difficultyTarget(486604799)
            .nonce(2083236893L)
            .transactionCount(0)
            .creationTimestamp(1231006505)
            .confidence(0)
            .build();
}
