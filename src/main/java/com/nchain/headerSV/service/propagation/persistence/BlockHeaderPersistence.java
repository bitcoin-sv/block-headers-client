package com.nchain.headerSV.service.propagation.persistence;

import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import com.nchain.headerSV.dao.service.PersistenceLocatorService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.concurrent.GuardedBy;
import java.util.Collection;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 21/07/2020
 */
@Service
@ConfigurationProperties(prefix = "headersv.propagation.persistence.block")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BlockHeaderPersistence extends AbstractPersistence<BlockHeader> {

    public BlockHeaderPersistence(PersistenceLocatorService persistenceLocatorService) {
        super(persistenceLocatorService);
    }

    @GuardedBy("this")
    @Override
    protected void process(Collection<BlockHeader> blockHeaders) {
        getPersistenceService().persistBlockHeaders(blockHeaders);

    }
}
