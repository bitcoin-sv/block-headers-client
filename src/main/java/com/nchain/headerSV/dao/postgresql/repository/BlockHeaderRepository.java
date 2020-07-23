package com.nchain.headerSV.dao.postgresql.repository;

import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 21/07/2020
 */
@Repository
public interface BlockHeaderRepository extends JpaRepository<BlockHeader, String> {
   List<BlockHeader> findByAddressAndHash(String address, String hash);
}
