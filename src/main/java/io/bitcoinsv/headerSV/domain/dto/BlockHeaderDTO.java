package io.bitcoinsv.headerSV.domain.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.bitcoinj.bitcoin.api.base.HeaderReadOnly;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.jose@nchain.com
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlockHeaderDTO {
    @JsonIgnore
    private HeaderReadOnly headerReadOnly;
    private String hash;
    private long version;
    private String prevBlockHash;
    private String merkleRoot;
    private long creationTimestamp;
    private long difficultyTarget;
    private long nonce;
    private long transactionCount;
    private BigInteger work;

    public static BlockHeaderDTO of(HeaderReadOnly blockHeader) {
        return BlockHeaderDTO.builder()
                .hash(blockHeader.getHash().toString())
                .creationTimestamp(blockHeader.getTime())
                .difficultyTarget(blockHeader.getDifficultyTarget())
                .merkleRoot(blockHeader.getMerkleRoot().toString())
                .prevBlockHash(blockHeader.getPrevBlockHash().toString())
                .version(blockHeader.getVersion())
                .nonce(blockHeader.getNonce())
                .transactionCount(0)
                .work(blockHeader.getWork())
                .headerReadOnly(blockHeader)
                .build();
    }
}
