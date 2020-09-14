package com.nchain.headerSV.service.propagation.persistence;

import com.nchain.headerSV.dao.service.PersistenceEngine;
import com.nchain.headerSV.dao.service.PersistenceLocatorService;
import com.nchain.headerSV.dao.service.PersistenceService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.concurrent.GuardedBy;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 25/06/2020
 */
@ConfigurationProperties(prefix = "headersv.persistence")
public abstract class AbstractPersistence<T> {
    private final PersistenceLocatorService persistenceLocatorService;

    protected long lastFlushTime = System.currentTimeMillis();

    protected long lastFlushTimeMaxIntervalMs = 1000;

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

   public void persist(T table){
        if(enabled) {
            batch.add(table);
            if(batch.size() >= batchSize || System.currentTimeMillis() - lastFlushTime > lastFlushTimeMaxIntervalMs ) {
                flush();
            }
        }
   }

    @GuardedBy("this")
    public void flush() {
        process(batch);
        batch.clear();
        lastFlushTime = System.currentTimeMillis();
    }

    protected abstract void process(Collection<T> object);

    protected PersistenceService getPersistenceService() {
        return persistenceLocatorService.locate(PersistenceEngine.valueOf(persistenceEngine));
    }
}
