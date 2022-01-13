package io.bitcoinsv.headerSV.rest.v1.controller;


import io.bitcoinsv.bitcoinjsv.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinsv.headerSV.core.api.ChainHeaderInfo;
import io.bitcoinsv.headerSV.core.api.HeaderSvApi;
import io.bitcoinsv.headerSV.rest.v1.client.rest.HeaderSVRestEndpoints;
import io.bitcoinsv.headerSV.rest.v1.client.domain.BlockHeaderDTO;
import io.bitcoinsv.headerSV.rest.v1.client.domain.ChainStateDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
@RestController
//@RequestMapping("/api/v1/chain/header")
@RequestMapping(value = HeaderSVRestEndpoints.URL_CHAIN_HEADER)
public class BlockHeaderControllerV1 {

    HeaderSvApi headerSvApi;

    public BlockHeaderControllerV1(HeaderSvApi blockChainFacade) {
        this.headerSvApi = blockChainFacade;
    }

    //@RequestMapping("/{hash}")
    @RequestMapping(value = HeaderSVRestEndpoints.URL_CHAIN_HEADER_HASH)
    public ResponseEntity<?> getHeader(@PathVariable String hash, @RequestHeader(value = "Content-Type", required = false, defaultValue = "application/json") MediaType contentType) {

        HeaderReadOnly blockHeader = headerSvApi.getBlockHeader(hash);

        if (blockHeader == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "BlockHeader not found");
        }
        // We build the DTO:
        BlockHeaderDTO blockHeaderDTO = BlockHeaderDTO.of(blockHeader);

        switch (contentType.toString()) {
            case MediaType.APPLICATION_OCTET_STREAM_VALUE:
                return ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(blockHeaderDTO.getHeaderReadOnly().serialize());

            default:
                return new ResponseEntity<>(blockHeaderDTO, HttpStatus.OK);
        }
    }

    //@RequestMapping("/{hash}/ancestors")
    @Deprecated
    @RequestMapping(value = HeaderSVRestEndpoints.URL_CHAIN_HEADER_HASH_ANCESTORS_DEPRECATED)
    public ResponseEntity<?> getAncestorsDeprecated(@PathVariable String hash, @RequestBody String ancestorHash) {
        List<HeaderReadOnly> headerHistory = headerSvApi.getAncestors(hash, ancestorHash);

        if (headerHistory == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "BlockHeader either not found or not in the same chain");
        }

        // We build and return the DTOs:
        List<BlockHeaderDTO> headerHistoryDTO = headerHistory.stream().map(BlockHeaderDTO::of).collect(Collectors.toList());
        return new ResponseEntity<>(headerHistoryDTO, HttpStatus.OK);
    }

    //@RequestMapping("/{hash}/{ancestorHash}/}ancestors")
    @RequestMapping(value = HeaderSVRestEndpoints.URL_CHAIN_HEADER_HASH_ANCESTORS)
    public ResponseEntity<?> getAncestors(@PathVariable String hash, @PathVariable String ancestorHash) {
        List<HeaderReadOnly> headerHistory = headerSvApi.getAncestors(hash, ancestorHash);

        if (headerHistory == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "BlockHeader either not found or not in the same chain");
        }

        // We build and return the DTOs:
        List<BlockHeaderDTO> headerHistoryDTO = headerHistory.stream().map(BlockHeaderDTO::of).collect(Collectors.toList());
        return new ResponseEntity<>(headerHistoryDTO, HttpStatus.OK);
    }

    //@RequestMapping("/commonAncestor")
    @RequestMapping(value = HeaderSVRestEndpoints.URL_CHAIN_HEADER_COMMON_ANCESTORS)
    public ResponseEntity<?> getCommonAncestor(@RequestBody List<String> blockHashes){
        HeaderReadOnly headerHistory = headerSvApi.findCommonAncestor(blockHashes);

        if (headerHistory == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "BlockHeader either not found or not in the same chain");
        }

        // We build and return the DTO:
        BlockHeaderDTO headerHistoryDO = BlockHeaderDTO.of(headerHistory);

        return new ResponseEntity<>(headerHistoryDO, HttpStatus.OK);
    }


    //@RequestMapping("/state/{hash}")
    @RequestMapping(value = HeaderSVRestEndpoints.URL_CHAIN_HEADER_STATE)
    public ResponseEntity<?> getHeaderDetails(@PathVariable String hash) {
        ChainHeaderInfo blockHeaderState = headerSvApi.getBlockHeaderState(hash);

        if (blockHeaderState == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "BlockHeader not found");
        }

        // We build and return the DTO:
        ChainStateDTO blockHeaderStateDTO = ChainStateDTO.builder()
                .header(BlockHeaderDTO.of(blockHeaderState.getHeader()))
                .state(blockHeaderState.getState())
                .chainWork(blockHeaderState.getChainWork())
                .height(blockHeaderState.getHeight())
                .confirmations(blockHeaderState.getConfirmations())
                .build();

        return new ResponseEntity<>(blockHeaderStateDTO, HttpStatus.OK);
    }

    //@RequestMapping("/byHeight")
    @RequestMapping(value = HeaderSVRestEndpoints.URL_CHAIN_HEADER_BYHEIGHT)
    public ResponseEntity<?> getHeadersByHeight(@RequestParam Integer height, @RequestParam(defaultValue = "1") Integer count,
                                                @RequestHeader(value = "Accept", required = false, defaultValue = "application/json") MediaType acceptContentType){
        try {
            if (count > 2000) {
                throw new IllegalArgumentException("Count exceeds max value of 2000 headers");
            }

            List<HeaderReadOnly> headers = headerSvApi.getHeadersByHeight(height, count);
            if (acceptContentType.toString().equals(MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (HeaderReadOnly header : headers) {
                    baos.write(header.serialize());
                }
                return new ResponseEntity<>(baos.toByteArray(), HttpStatus.OK);
            } else {
                // MediaType.APPLICATION_JSON_VALUE
                List<BlockHeaderDTO> headersDTO = headers.stream().map(BlockHeaderDTO::of).collect(Collectors.toList());
                return new ResponseEntity<>(headersDTO, HttpStatus.OK);
            }
        }
        catch (IllegalArgumentException | IllegalStateException | IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }
}
