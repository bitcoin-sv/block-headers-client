package com.nchain.headerSV.dao.service;

import com.nchain.headerSV.dao.model.BlockHeaderDTO;
import com.nchain.headerSV.domain.BlockHeaderAddrInfo;

import java.util.Collection;
import java.util.Optional;

/**
 * Operations provided by the persistence Layer.
 * All business layers must use the operations defined here (direct access to JPA repositories is not
 * allowed).
 */
public interface PersistenceService {

    // it returns a Enumeration identifying the DB Engine implementation to use.
    PersistenceEngine getEngine();

    void persistBlockHeaders(Collection<BlockHeaderAddrInfo> blockHeaderAddrInfo);
    void persistBlockHeader(BlockHeaderDTO blockHeaderDTO);

    Optional<BlockHeaderDTO> retrieveBlockHeader(String hash);

}
