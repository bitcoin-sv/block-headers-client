package com.nchain.headerSV.dao.postgresql;

import com.nchain.headerSV.dao.model.PeerDTO;
import com.nchain.headerSV.dao.postgresql.domain.Peer;
import com.nchain.headerSV.dao.postgresql.repository.PeerRepository;
import com.nchain.headerSV.dao.service.PersistenceEngine;
import com.nchain.headerSV.dao.service.PersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;

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
