package com.nchain.headerSV.tools;

import com.nchain.jcl.base.domain.api.base.BlockHeader;
import com.nchain.jcl.base.tools.crypto.Sha256Wrapper;


/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2020 Bitcoin Association
  * Copyright (c) 2018-2020 nChain Ltd
 * @date 04/08/2020
 */
public class Util {

    public static BlockHeader GENESIS_BLOCK_HEADER = BlockHeader.builder()
            .prevBlockHash(Sha256Wrapper.ZERO_HASH)
            .hash(Sha256Wrapper.wrap("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"))
            .version(1)
            .merkleRoot(Sha256Wrapper.wrap("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b"))
            .difficultyTarget(486604799)
            .nonce(2083236893L)
            .numTxs(0)
            .time(1231006505)
            .build();

}
