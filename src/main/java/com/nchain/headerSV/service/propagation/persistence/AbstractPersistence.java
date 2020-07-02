package com.nchain.headerSV.service.propagation.persistence;

import com.nchain.headerSV.dao.service.PersistenceEngine;
import com.nchain.headerSV.dao.service.PersistenceLocatorService;
import com.nchain.headerSV.dao.service.PersistenceService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.concurrent.GuardedBy;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 25/06/2020
 */
public abstract class AbstractPersistence<T> {
    private final PersistenceLocatorService persistenceLocatorService;

    @Autowired
    public AbstractPersistence(PersistenceLocatorService persistenceLocatorService) {
        this.persistenceLocatorService = persistenceLocatorService;
    }

    @Setter
    protected Integer batchSize;

    @Value("${headersv.persistenceEngine}")
    protected String persistenceEngine;

    @Setter
    protected boolean enabled;


    private Collection<T> batch = new LinkedList<>();

    public int getCurrentBatchSize() {
        return batch.size();
    }

   public void persist(T table){
        if(enabled) {
            batch.add(table);
            if(batch.size() >= batchSize) {
                flush();
            }
        }
   }

    @GuardedBy("this")
    public void flush() {
        process(batch);
        batch.clear();
    }

    protected abstract void process(Collection<T> object);

    protected PersistenceService getPersistenceService() {
        return persistenceLocatorService.locate(PersistenceEngine.valueOf(persistenceEngine));
    }
}
