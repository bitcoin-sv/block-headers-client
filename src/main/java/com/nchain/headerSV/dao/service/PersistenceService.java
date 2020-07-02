package com.nchain.headerSV.dao.service;

import com.nchain.headerSV.dao.model.PeerDTO;

import java.util.Collection;

/**
 * Operations provided by the persistence Layer.
 * All business layers must use the operations defined here (direct access to JPA repositories is not
 * allowed).
 */
public interface PersistenceService {

    // it returns a Enumeration identifying the DB Engine implementation to use.
    PersistenceEngine getEngine();

    void persistPeers(Collection<PeerDTO> peerDTO);
    void persistPeer(PeerDTO peerDTO) ;
}
