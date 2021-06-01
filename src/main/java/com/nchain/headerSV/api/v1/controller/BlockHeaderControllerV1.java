package com.nchain.headerSV.api.v1.controller;

import com.nchain.headerSV.domain.dto.BlockHeaderDTO;
import com.nchain.headerSV.api.HSVFacade;
import com.nchain.headerSV.domain.dto.ChainStateDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> getHeader(@PathVariable String hash, @RequestHeader(value = "Content-Type", required = false) MediaType contentType) {
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

    @RequestMapping("/state/{hash}")
    public ResponseEntity<?> getHeaderDetails(@PathVariable String hash) {
        ChainStateDTO blockHeaderStateDTO = hsvFacade.getBlockHeaderState(hash);

        if (blockHeaderStateDTO == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "BlockHeader not found");
        }

        return new ResponseEntity<>(blockHeaderStateDTO, HttpStatus.OK);
    }
}
