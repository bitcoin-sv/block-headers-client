package com.nchain.headerSV.dao.postgresql.repository;

import com.nchain.headerSV.dao.postgresql.domain.Peer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 01/07/2020
 */
@ConditionalOnProperty(value="headersv.persistenceEngine", havingValue = "postgresql", matchIfMissing = true)
@Repository
public interface PeerRepository extends JpaRepository<Peer, Long> {
    List<Peer> findByAddressAndPort(String address, int port);
}
