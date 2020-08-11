package com.nchain.headerSV.dao.postgresql;

import com.nchain.headerSV.dao.model.BlockHeaderAddrDTO;
import com.nchain.headerSV.dao.model.BlockHeaderDTO;
import com.nchain.headerSV.dao.model.PeerDTO;
import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import com.nchain.headerSV.dao.postgresql.domain.BlockHeaderAddr;
import com.nchain.headerSV.dao.postgresql.domain.Peer;
import com.nchain.headerSV.dao.postgresql.repository.BlockHeaderAddrRepository;
import com.nchain.headerSV.dao.postgresql.repository.BlockHeaderRepository;
import com.nchain.headerSV.dao.postgresql.repository.PeerRepository;
import com.nchain.headerSV.dao.service.PersistenceEngine;
import com.nchain.headerSV.dao.service.PersistenceService;
import com.nchain.headerSV.domain.BlockHeaderAddrInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;
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

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PeerRepository peerRepository;

    @Autowired
    private BlockHeaderRepository blockHeaderRepository;

    @Autowired
    private BlockHeaderAddrRepository blockHeaderAddrRepository;


    @Override
    public PersistenceEngine getEngine() {
        return PersistenceEngine.postgresql;
    }

    @Override
    public void persistPeers(Collection<PeerDTO> peerDTOs) {
        log.debug("Persisting "+peerDTOs.size()+" peers");
        peerDTOs.forEach(p -> log.debug(" - persisting Peer: " + p));
        peerDTOs.forEach(this::persistPeer);
    }

    @Override
    public void persistPeer(PeerDTO peerDTO) {
        try {
            List<Peer> peers = peerRepository.findByAddressAndPort(peerDTO.getAddress(), peerDTO.getPort());
            Peer peer = (peers != null && peers.size() > 0)? peers.get(0) : new Peer();       ;
            convertPeer(peerDTO, peer);
            peerRepository.save(peer);
        } catch (DataIntegrityViolationException e) {
            log.debug("ERROR persistPeer. Peer: " + peerDTO.getAddress(), e);
            // We ignore errors when trying to write duplicated values same peer)
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }

    @Override
    public void persistBlockHeaders(Collection<BlockHeaderAddrInfo> blockHeaderAddrInfos) {
        log.debug("Persisting :"+blockHeaderAddrInfos.size()+": blockHeaders");
        blockHeaderAddrInfos.forEach((d -> log.debug("-persisting blockheaders" +d)));
        blockHeaderAddrInfos.forEach(this::persistBlockHeaderInfo);
        //blockHeaderDTOS.forEach(this::persistBlockHeaderAddr);

    }

    void persistBlockHeaderInfo(BlockHeaderAddrInfo blockHeaderInfo) {
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
        try{
            List<BlockHeaderAddr> blockHeaders = blockHeaderAddrRepository.findByAddressAndHash(blockHeaderDTO.getAddress(), blockHeaderDTO.getHash());
            BlockHeaderAddr blockHeaderAddr = (blockHeaders != null && blockHeaders.size()> 0) ? blockHeaders.get(0):new BlockHeaderAddr();
            converToBlockheaderAddr(blockHeaderDTO, blockHeaderAddr);
            blockHeaderAddrRepository.save(blockHeaderAddr);
        }catch (DataIntegrityViolationException e) {
            log.debug("ERROR persistBlockHeaderAddr. BlockHeader: " + blockHeaderDTO.getHash(), e);
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

    }

    private void converToBlockheaderAddr(BlockHeaderAddrDTO from, BlockHeaderAddr to) {

        if(from == null || to == null)
            return;

        to.setHash(from.getHash());
        to.setAddress(from.getAddress());
    }

    @Override
    public void persistBlockHeader(BlockHeaderDTO blockHeaderDTO) {
        try{
            List<BlockHeader> blockHeaders = blockHeaderRepository.findByHash(blockHeaderDTO.getHash());
            BlockHeader blockHeader = (blockHeaders != null && blockHeaders.size()> 0) ? blockHeaders.get(0):new BlockHeader();
            converToBlockheader(blockHeaderDTO, blockHeader);
            blockHeaderRepository.save(blockHeader);
        }catch (DataIntegrityViolationException e) {
            log.debug("ERROR persistBlockHeader. BlockHeader: " + blockHeaderDTO.getHash(), e);
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

    }

    public Optional<BlockHeaderDTO> retrieveBlockHeader(String hash) {
        Optional<BlockHeaderDTO> result = Optional.empty();
        try{
           List<BlockHeader> blockHeaders = blockHeaderRepository.findByHash(hash);
           if (blockHeaders != null && blockHeaders.size()> 0) {
               BlockHeaderDTO blockHeaderDTO = new BlockHeaderDTO();
               convertToBlockHeaderDTO(blockHeaders.get(0), blockHeaderDTO);
               result = Optional.of(blockHeaderDTO);
           }
        }catch (DataIntegrityViolationException e) {
            log.debug("ERROR retrieving BlockHeaderBy Hash: " + hash, e);
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
        return result;
    }

    private void convertToBlockHeaderDTO(BlockHeader from, BlockHeaderDTO to) {
        if(from == null || to == null)
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
        if(dto == null || to == null)
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


//    public PeerDTO retrievePeer(PeerDTO peerDTO) {
//        try {
//            List<Peer> peers = peerRepository.findByAddressAndPort(peerDTO.getAddress(), peerDTO.getPort());
//            Peer peer = (peers != null && peers.size() > 0)? peers.get(0) : new Peer();
//
//
//        }
//    }

    private void convertPeer(PeerDTO from, Peer to) {
        if (from == null || to == null) return;
        to.setAddress(from.getAddress());
        to.setPort(from.getPort());
        to.setCity(from.getCity());
        to.setCountry(from.getCountry());
        to.setProtocol_version(from.getProtocolVersion());
        to.setUser_agent(from.getUserAgent());
        to.setZipcode(from.getZipcode());
        to.setServices(from.getServices());
        to.setConnectionstatus(from.isConnectionStatus());

    }

    private void convertToPeerDTO(Peer from, PeerDTO to) {
        if (from == null || to == null) return;
        to.setAddress(from.getAddress());
        to.setPort(from.getPort());
        to.setCity(from.getCity());
        to.setCountry(from.getCountry());
        to.setProtocolVersion(from.getProtocol_version());
        to.setUserAgent(from.getUser_agent());
        to.setZipcode(from.getZipcode());
        to.setServices(from.getServices());
        to.setConnectionStatus(from.isConnectionstatus());

    }
}
