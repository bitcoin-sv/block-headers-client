package com.nchain.headerSV.api.controller;

import com.nchain.headerSV.api.service.BlockHeaderService;
import com.nchain.headerSV.dao.model.BlockHeaderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 28/07/2020
 */
@RestController
public class BlockHeaderController {

    @Autowired
    private BlockHeaderService blockHeaderService;

    @RequestMapping("/getHeaderByHash/{hash}")
    public BlockHeaderDTO getHeader(@PathVariable String hash) { return blockHeaderService.getBlockHeader(hash);}
}
