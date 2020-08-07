package com.nchain.headerSV.dao.model;

import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * author m.josenchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * date 21/07/2020
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlockHeaderDTO {
    private Long id;
    private String address;
    private String hash;
    private long version;
    private String prevBlockHash;
    private String merkleRoot;
    private long creationTimestamp;
    private long difficultyTarget;
    private long nonce;
    private long transactionCount;

    public static BlockHeaderDTO of(BlockHeader blockHeader) {
        return BlockHeaderDTO.builder()
                .address(blockHeader.getAddress())
                .hash(blockHeader.getHash())
                .creationTimestamp(blockHeader.getCreationTimestamp())
                .difficultyTarget(blockHeader.getDifficultyTarget())
                .merkleRoot(blockHeader.getMerkleRoot())
                .prevBlockHash(blockHeader.getPrevBlockHash())
                .version(blockHeader.getVersion())
                .nonce(blockHeader.getNonce())
                .prevBlockHash(blockHeader.getPrevBlockHash())
                .transactionCount(blockHeader.getTransactionCount())
                .build();
    }
}
