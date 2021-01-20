package com.nchain.headerSV.api.v1.controller;

import com.nchain.headerSV.domain.dto.BlockHeaderDTO;
import com.nchain.headerSV.domain.dto.ChainStateDTO;
import com.nchain.headerSV.api.HSVFacade;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2021 nChain Ltd
 * @date 15/01/2021
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

    @RequestMapping("/orphans")
    public void getOrphans() {
        List<BlockHeaderDTO> orphanBlocks = hsvFacade.getOrphans();
    }

    @RequestMapping("/orphans/prune")
    public void pruneOrphans() {
        hsvFacade.pruneOrphans();
    }

    @RequestMapping("/tips/prune")
    public void pruneAllTips(){
        hsvFacade.pruneAllTips();
    }

    @RequestMapping("/tips/prune/{hash}")
    public void pruneChain(@PathVariable String hash) {
        try{
            hsvFacade.pruneChain(hash);
        } catch (RuntimeException exception){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

}
