package com.nchain.headerSV.service.propagation.persistence;


import com.nchain.headerSV.service.geolocation.GeolocationService;
import com.nchain.headerSV.service.propagation.buffer.BufferedBlockHeaders;
import com.nchain.headerSV.service.propagation.buffer.BufferedMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 */
@Service
@AllArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class BufferedMessagePersistenceService {

    public final GeolocationService geoService;
    public final BlockHeaderPersistence blockHeaderPersistence;

    public void persist(BufferedMessage bufferedMessage) {
        if(bufferedMessage instanceof BufferedBlockHeaders) {
            process((BufferedBlockHeaders) bufferedMessage);
        }
    }


    private void process(BufferedBlockHeaders bufferedBlockHeader) {
      bufferedBlockHeader.getBlockHeaders().forEach(blockHeaderPersistence::persist);
    }


    public void stop() {
        blockHeaderPersistence.flush();
    }
}
