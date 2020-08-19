package com.nchain.headerSV.service.propagation.persistence;

import com.nchain.jcl.protocol.messages.BlockHeaderMsg;
import com.nchain.headerSV.dao.model.BlockHeaderDTO;
import com.nchain.headerSV.dao.service.PersistenceLocatorService;
import com.nchain.headerSV.domain.BlockHeaderAddrInfo;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.concurrent.GuardedBy;
import java.util.Collection;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 21/07/2020
 */
@Service
@ConfigurationProperties(prefix = "headersv.propagation.persistence.block")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BlockHeaderPersistence extends AbstractPersistence<BlockHeaderAddrInfo> {

    public BlockHeaderPersistence(PersistenceLocatorService persistenceLocatorService) {
        super(persistenceLocatorService);
    }

    @GuardedBy("this")
    @Override
    protected void process(Collection<BlockHeaderAddrInfo> blockHeaderDTOS) {
        getPersistenceService().persistBlockHeaders(blockHeaderDTOS);

    }
}
