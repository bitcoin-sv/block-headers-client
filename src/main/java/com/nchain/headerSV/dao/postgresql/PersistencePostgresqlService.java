package com.nchain.headerSV.dao.postgresql;

import com.nchain.headerSV.dao.model.BlockHeaderAddrDTO;
import com.nchain.headerSV.dao.model.BlockHeaderDTO;
import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import com.nchain.headerSV.dao.postgresql.domain.BlockHeaderAddr;
import com.nchain.headerSV.dao.postgresql.repository.BlockHeaderAddrRepository;
import com.nchain.headerSV.dao.postgresql.repository.BlockHeaderRepository;
import com.nchain.headerSV.dao.service.PersistenceEngine;
import com.nchain.headerSV.dao.service.PersistenceService;
import com.nchain.headerSV.domain.BlockHeaderAddrInfo;
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
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
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

    @Autowired
    private BlockHeaderAddrRepository blockHeaderAddrRepository;


    @Override
    public PersistenceEngine getEngine() {
        return PersistenceEngine.postgresql;
    }


    @Override
    @Transactional
    public void persistBlockHeaders(Collection<BlockHeaderAddrInfo> blockHeaderAddrInfos) {
        log.info("Persisting :" + blockHeaderAddrInfos.size() + ": blockHeaders");
        blockHeaderAddrInfos.forEach(this::persistBlockHeaderInfo);
        blockHeaderRepository.flush();
    }

    void persistBlockHeaderInfo(BlockHeaderAddrInfo blockHeaderInfo) {
        log.debug("Persisting blockheader: " + blockHeaderInfo);

        final BlockHeaderDTO blockHeaderDTO = BlockHeaderDTO.builder()
                .hash(blockHeaderInfo.getHash())
                .prevBlockHash(blockHeaderInfo.getPrevBlockHash())
                .merkleRoot(blockHeaderInfo.getMerkleRoot())
                .difficultyTarget(blockHeaderInfo.getDifficultyTarget())
                .transactionCount(blockHeaderInfo.getTransactionCount())
                .creationTimestamp(blockHeaderInfo.getCreationTimestamp())
                .version(blockHeaderInfo.getVersion())
                .nonce(blockHeaderInfo.getNonce()).build();

        final BlockHeaderAddrDTO blockHeaderAddrDTO = BlockHeaderAddrDTO.builder()
                .address(blockHeaderInfo.getAddress())
                .hash(blockHeaderInfo.getHash()).build();

        persistBlockHeader(blockHeaderDTO);
        persistBlockHeaderAddr(blockHeaderAddrDTO);
    }


    public void persistBlockHeaderAddr(BlockHeaderAddrDTO blockHeaderDTO) {
        try {
            BlockHeaderAddr blockHeaderAddr = new BlockHeaderAddr();
            converToBlockheaderAddr(blockHeaderDTO, blockHeaderAddr);
            blockHeaderAddrRepository.save(blockHeaderAddr);
        } catch (DataIntegrityViolationException e) {
            log.debug("ERROR persistBlockHeaderAddr. BlockHeader: " + blockHeaderDTO.getHash(), e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void converToBlockheaderAddr(BlockHeaderAddrDTO from, BlockHeaderAddr to) {

        if (from == null || to == null)
            return;

        to.setHash(from.getHash());
        to.setAddress(from.getAddress());
    }

    @Override
    public void persistBlockHeader(BlockHeaderDTO blockHeaderDTO) {
        try {
            BlockHeader blockHeaderToPersist = new BlockHeader();
            converToBlockheader(blockHeaderDTO, blockHeaderToPersist);
            blockHeaderRepository.save(blockHeaderToPersist);
        } catch (DataIntegrityViolationException e) {
            log.debug("ERROR persistBlockHeader. BlockHeader: " + blockHeaderDTO.getHash(), e);
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

    private void converToBlockheader(BlockHeaderDTO dto, BlockHeader to) {
        if (dto == null || to == null)
            return;

        to.setHash(dto.getHash());
        to.setCreationTimestamp(dto.getCreationTimestamp());
        to.setDifficultyTarget(dto.getDifficultyTarget());
        to.setMerkleRoot(dto.getMerkleRoot());
        to.setNonce(dto.getNonce());
        to.setDifficultyTarget(dto.getDifficultyTarget());
        to.setPrevBlockHash(dto.getPrevBlockHash());
        to.setVersion(dto.getVersion());
        to.setTransactionCount(dto.getTransactionCount());
    }
}
