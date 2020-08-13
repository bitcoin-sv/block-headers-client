package com.nchain.headerSV.api.controller;

import com.nchain.headerSV.api.exception.BlockNotFoundException;
import com.nchain.headerSV.api.service.BlockHeaderService;
import com.nchain.headerSV.dao.model.BlockHeaderDTO;
import com.nchain.headerSV.service.cache.BlockHeaderQueryResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 28/07/2020
 */
@RestController
@Slf4j
public class BlockHeaderController {

    @Autowired
    private BlockHeaderService blockHeaderService;

    @RequestMapping("/getHeaderByHash/{hash}")
    public BlockHeaderDTO getHeader(@PathVariable String hash) {
        try {
            return blockHeaderService.getBlockHeader(hash);
        }catch (BlockNotFoundException  ex){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Block not found"
            );
        }
    }

    @RequestMapping("/getHeaderStateByHash/{hash}")
    public BlockHeaderQueryResult getHeaderDetails(@PathVariable String hash) {
        try {

            final BlockHeaderQueryResult blockStateByHash = blockHeaderService.getBlockStateByHash(hash);
            return blockStateByHash;
        }catch (BlockNotFoundException  ex){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Block not found"
            );
        }
    }
}
