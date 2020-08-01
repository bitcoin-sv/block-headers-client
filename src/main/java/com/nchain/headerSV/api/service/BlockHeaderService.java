package com.nchain.headerSV.api.service;

import com.nchain.headerSV.dao.model.BlockHeaderDTO;
import com.nchain.headerSV.dao.service.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 28/07/2020
 */
@Service
public class BlockHeaderService {

    @Autowired
    private PersistenceService persistenceService;

    public BlockHeaderDTO getBlockHeader(String hash) {
        Optional<BlockHeaderDTO> blockHeaderDTO = persistenceService.retrieveBlockHeader(hash);

        BlockHeaderDTO blockHeader  = blockHeaderDTO.get();
        return blockHeader;

    }
}
