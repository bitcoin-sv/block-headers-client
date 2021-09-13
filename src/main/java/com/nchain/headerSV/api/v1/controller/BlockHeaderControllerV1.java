package com.nchain.headerSV.api.v1.controller;

import com.nchain.headerSV.domain.dto.BlockHeaderDTO;
import com.nchain.headerSV.api.HSVFacade;
import com.nchain.headerSV.domain.dto.ChainStateDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author m.fletcher@nchain.com
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

    @RequestMapping("/{hash}/ancestors")
    public ResponseEntity<?> getAncestors(@PathVariable String hash, @RequestBody String ancestorHash){
        List<BlockHeaderDTO> headerHistory = hsvFacade.getAncestors(hash, ancestorHash);

        if (headerHistory == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "BlockHeader not found");
        }

        return new ResponseEntity<>(headerHistory, HttpStatus.OK);
    }

    @RequestMapping("/commonAncestor")
    public ResponseEntity<?> getCommonAncestor(@RequestBody List<String> blockHashes){
        BlockHeaderDTO headerHistory = hsvFacade.findCommonAncestor(blockHashes);

        if (headerHistory == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "BlockHeader not found");
        }

        return new ResponseEntity<>(headerHistory, HttpStatus.OK);
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
