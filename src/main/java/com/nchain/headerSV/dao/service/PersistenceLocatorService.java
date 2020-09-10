package com.nchain.headerSV.dao.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
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
