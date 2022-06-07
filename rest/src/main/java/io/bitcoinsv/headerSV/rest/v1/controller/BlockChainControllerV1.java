package io.bitcoinsv.headerSV.rest.v1.controller;

import io.bitcoinsv.headerSV.core.api.ChainHeaderInfo;
import io.bitcoinsv.headerSV.core.api.HeaderSvApi;
import io.bitcoinsv.headerSV.rest.v1.client.rest.HeaderSVRestEndpoints;
import io.bitcoinsv.headerSV.rest.v1.client.domain.BlockHeaderDTO;
import io.bitcoinsv.headerSV.rest.v1.client.domain.ChainStateDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
@RestController
//@RequestMapping("/api/v1/chain")
@RequestMapping(value = HeaderSVRestEndpoints.URL_CHAIN)
public class BlockChainControllerV1 {

    HeaderSvApi headerSvApi;

    public BlockChainControllerV1(HeaderSvApi blockChainFacade) {
        this.headerSvApi = blockChainFacade;
    }

    //@RequestMapping("/tips")
    @RequestMapping(value = HeaderSVRestEndpoints.URL_CHAIN_TIPS)
    public List<ChainStateDTO> getTips(){
        try{
            List<ChainHeaderInfo> chainStateList = headerSvApi.getChainTips();
            // Build DTO from tips
            List<ChainStateDTO> result = chainStateList.stream()
                            .map(ci -> ChainStateDTO.builder()
                                    .chainWork(ci.getChainWork())
                                    .height(ci.getHeight())
                                    .state(ci.getState())
                                    .header(BlockHeaderDTO.of(ci.getHeader()))
                                    .confirmations(1)
                                    .build())
                            .collect(Collectors.toList());
            return result;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    //@RequestMapping("/tips/prune/{hash}")
    @RequestMapping(value = HeaderSVRestEndpoints.URL_CHAIN_TIPS_PRUNE)
    public void pruneChain(@PathVariable String hash) {
        try{
            headerSvApi.pruneChain(hash);
        } catch (RuntimeException exception){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

}
