package com.nchain.headerSV.dao.postgresql.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NotNull
    private String address;

    @Column
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
}
