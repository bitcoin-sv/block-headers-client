package com.nchain.headerSV.dao.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 */
@Service
public class PersistenceLocatorService {
    private final Map<PersistenceEngine, PersistenceService> serviceMap;

    public PersistenceLocatorService(List<PersistenceService> services) {
        this.serviceMap = services.stream().collect(Collectors.toMap(PersistenceService::getEngine, e -> e));
    }

    public PersistenceService locate(PersistenceEngine persistenceEngine) {
        return serviceMap.get(persistenceEngine);
    }
}
