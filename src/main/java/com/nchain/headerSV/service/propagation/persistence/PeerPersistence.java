package com.nchain.headerSV.service.propagation.persistence;

import com.nchain.headerSV.dao.model.PeerDTO;
import com.nchain.headerSV.dao.service.PersistenceLocatorService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 */
@Service
@ConfigurationProperties(prefix = "headersv.listener-app.propagation.persistence.peer")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PeerPersistence extends AbstractPersistence<PeerDTO> {

    public PeerPersistence(PersistenceLocatorService persistenceLocatorService) {
        super(persistenceLocatorService);
    }

    @Override
    protected void process(Collection<PeerDTO> peerDTOS) {
        getPersistenceService().persistPeers(peerDTOS);

    }
}
