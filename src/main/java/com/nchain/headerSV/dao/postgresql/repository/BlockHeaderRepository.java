package com.nchain.headerSV.dao.postgresql.repository;

import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 21/07/2020
 */
@Repository
public interface BlockHeaderRepository extends JpaRepository<BlockHeader, Long> {

   BlockHeader findByHash(String hash);
}
