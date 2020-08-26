package com.nchain.headerSV.dao.postgresql.repository;

import com.nchain.headerSV.dao.postgresql.domain.BlockHeaderAddr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 05/08/2020
 */
public interface BlockHeaderAddrRepository extends JpaRepository<BlockHeaderAddr, Long> {
    List<BlockHeaderAddr> findByAddressAndHash(String address, String hash);
    List<BlockHeaderAddr> findByHash(String hash);
}
