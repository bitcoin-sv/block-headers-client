package com.nchain.headerSV.dao.postgresql.domain;

import com.nchain.bna.protocol.messages.BlockHeaderMsg;
import com.nchain.bna.tools.bytes.HEX;
import com.nchain.bna.tools.crypto.Sha256Wrapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 21/07/2020
 */
@Entity
@Table(name = "BLOCKHEADER")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlockHeader {

    @Id
    @NotNull
    private String hash;

    @Column
    @NotNull
    private long version;

    @Column
    @NotNull
    private String prevBlockHash;

    @Column
    @NotNull
    private String merkleRoot;

    @Column
    @NotNull
    private long creationTimestamp;

    @Column
    @NotNull
    private long difficultyTarget;

    @Column
    @NotNull
    private long nonce;

    @Column
    private long transactionCount;

    public static BlockHeader of(BlockHeaderMsg blockHeaderMsg){
        return BlockHeader.builder()
                .version(blockHeaderMsg.getVersion())
                .hash(HEX.encode(blockHeaderMsg.getHash().getHashBytes()))
                .prevBlockHash(HEX.encode(Sha256Wrapper.wrapReversed(blockHeaderMsg.getPrevBlockHash().getHashBytes()).getBytes()))
                .merkleRoot(HEX.encode(Sha256Wrapper.wrapReversed(blockHeaderMsg.getMerkleRoot().getHashBytes()).getBytes()))
                .creationTimestamp(blockHeaderMsg.getCreationTimestamp())
                .difficultyTarget(blockHeaderMsg.getDifficultyTarget())
                .nonce(blockHeaderMsg.getNonce())
                .transactionCount(blockHeaderMsg.getTransactionCount().getValue())
                .build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}
