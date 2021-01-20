package com.nchain.headerSV.api.v1.controller;

import com.nchain.headerSV.domain.dto.BlockHeaderDTO;
import com.nchain.headerSV.api.HSVFacade;
import com.nchain.headerSV.domain.dto.ChainStateDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 28/07/2020
 */
@RestController
@RequestMapping("/api/v1/chain/header")
public class BlockHeaderControllerV1 {

    HSVFacade hsvFacade;

    public BlockHeaderControllerV1(HSVFacade blockChainFacade) {
        this.hsvFacade = blockChainFacade;
    }

    @RequestMapping("/{hash}")
    public BlockHeaderDTO getHeader(@PathVariable String hash) {
        BlockHeaderDTO blockHeaderDTO = hsvFacade.getBlockHeader(hash);

        if (blockHeaderDTO == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "BlockHeader not found");
        }

        return blockHeaderDTO;
    }

    @RequestMapping("/state/{hash}")
    public ChainStateDTO getHeaderDetails(@PathVariable String hash) {
        ChainStateDTO blockHeaderStateDTO = hsvFacade.getBlockHeaderState(hash);

        if (blockHeaderStateDTO == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "BlockHeader not found");
        }

        return blockHeaderStateDTO;
    }
}
