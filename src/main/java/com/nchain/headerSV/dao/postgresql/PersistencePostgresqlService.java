package com.nchain.headerSV.dao.postgresql;

import com.nchain.headerSV.dao.model.BlockHeaderDTO;
import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import com.nchain.headerSV.dao.postgresql.repository.BlockHeaderRepository;
import com.nchain.headerSV.dao.service.PersistenceEngine;
import com.nchain.headerSV.dao.service.PersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;


import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 01/07/2020
 */

/**
 * Implementaion of the PersistenceService interface based on PostgresQL
 */
@ConditionalOnProperty(value="headersv.persistenceEngine", havingValue = "postgresql", matchIfMissing = true)
@Service
@Slf4j
public class PersistencePostgresqlService implements PersistenceService {

    @Autowired
    private BlockHeaderRepository blockHeaderRepository;

    @Override
    public PersistenceEngine getEngine() {
        return PersistenceEngine.postgresql;
    }


    @Override
    @Transactional
    public void persistBlockHeaders(Collection<BlockHeader> blockHeaderAddrInfos) {
        log.debug("Persisting :" + blockHeaderAddrInfos.size() + ": blockHeaders");
        blockHeaderAddrInfos.forEach(this::persistBlockHeader);
        blockHeaderRepository.flush();
    }

    @Override
    public void persistBlockHeader(BlockHeader blockHeader) {
        try {
            blockHeaderRepository.save(blockHeader);
        } catch (DataIntegrityViolationException e) {
            log.debug("ERROR persistBlockHeader. BlockHeader: " + blockHeader.getHash(), e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public Optional<BlockHeaderDTO> retrieveBlockHeader(String hash) {
        Optional<BlockHeaderDTO> result = Optional.empty();
        try {
            BlockHeader blockHeaders = blockHeaderRepository.findByHash(hash);
            if (blockHeaders != null) {
                BlockHeaderDTO blockHeaderDTO = new BlockHeaderDTO();
                convertToBlockHeaderDTO(blockHeaders, blockHeaderDTO);
                result = Optional.of(blockHeaderDTO);
            }
        } catch (DataIntegrityViolationException e) {
            log.debug("ERROR retrieving BlockHeaderBy Hash: " + hash, e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private void convertToBlockHeaderDTO(BlockHeader from, BlockHeaderDTO to) {
        if (from == null || to == null)
            return;

        to.setHash(from.getHash());
        to.setCreationTimestamp(from.getCreationTimestamp());
        to.setDifficultyTarget(from.getDifficultyTarget());
        to.setMerkleRoot(from.getMerkleRoot());
        to.setNonce(from.getNonce());
        to.setDifficultyTarget(from.getDifficultyTarget());
        to.setPrevBlockHash(from.getPrevBlockHash());
        to.setVersion(from.getVersion());
        to.setTransactionCount(from.getTransactionCount());
    }

}
