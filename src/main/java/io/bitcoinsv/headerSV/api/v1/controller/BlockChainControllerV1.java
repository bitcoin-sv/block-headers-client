package io.bitcoinsv.headerSV.api.v1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.bitcoinsv.headerSV.api.HSVFacade;
import io.bitcoinsv.headerSV.domain.dto.BlockHeaderDTO;
import io.bitcoinsv.headerSV.domain.dto.ChainStateDTO;

import java.util.List;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
@RestController
@RequestMapping("/api/v1/chain")
public class BlockChainControllerV1 {

    HSVFacade hsvFacade;

    public BlockChainControllerV1(HSVFacade blockChainFacade) {
        this.hsvFacade = blockChainFacade;
    }

    @RequestMapping("/tips")
    public List<ChainStateDTO> getTips(){
        List<ChainStateDTO> chainStateDTOList = hsvFacade.getChainTips();

        return chainStateDTOList;
    }

    @RequestMapping("/tips/prune/{hash}")
    public void pruneChain(@PathVariable String hash) {
        try{
            hsvFacade.pruneChain(hash);
        } catch (RuntimeException exception){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

}
