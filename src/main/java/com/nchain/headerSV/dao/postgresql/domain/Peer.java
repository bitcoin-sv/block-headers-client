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
 * @date 01/07/2020
 */
@Entity
@Table(name = "PEER")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Peer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NotNull
    private String address;

    @Column
    @NotNull
    private int port;

    @Column
    private String user_agent;

    @Column
    @NotNull
    private Integer protocol_version;

    @Column
    private String country;

    @Column
    private String city;

    @Column
    private String zipcode;

    @Column
    private Long services;

    @Column
    private boolean connectionstatus;

}
