package com.nchain.headerSV.dao.model;

import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * author m.josenchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * date 21/07/2020
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlockHeaderDTO {
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
