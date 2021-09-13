package io.bitcoinsv.headerSV.api.v1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import io.bitcoinsv.headerSV.api.HSVFacade;
import io.bitcoinsv.headerSV.domain.dto.BlockHeaderDTO;
import io.bitcoinsv.headerSV.domain.dto.ChainStateDTO;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
@RestController
@RequestMapping("/api/v1/chain/header")
public class BlockHeaderControllerV1 {

    HSVFacade hsvFacade;

    public BlockHeaderControllerV1(HSVFacade blockChainFacade) {
        this.hsvFacade = blockChainFacade;
    }

    @RequestMapping("/{hash}")
    public ResponseEntity<?> getHeader(@PathVariable String hash, @RequestHeader(value = "Content-Type", required = false, defaultValue = "application/json") MediaType contentType) {
        BlockHeaderDTO blockHeaderDTO = hsvFacade.getBlockHeader(hash);

        if (blockHeaderDTO == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "BlockHeader not found");
        }

        switch (contentType.toString()) {
            case MediaType.APPLICATION_OCTET_STREAM_VALUE:
                return new ResponseEntity<>(blockHeaderDTO.getHeaderReadOnly().serialize(), HttpStatus.OK);

            default:
                return new ResponseEntity<>(blockHeaderDTO, HttpStatus.OK);
        }
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
    public ResponseEntity<?> getHeaderDetails(@PathVariable String hash) {
        ChainStateDTO blockHeaderStateDTO = hsvFacade.getBlockHeaderState(hash);

        if (blockHeaderStateDTO == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "BlockHeader not found");
        }

        return new ResponseEntity<>(blockHeaderStateDTO, HttpStatus.OK);
    }
}
