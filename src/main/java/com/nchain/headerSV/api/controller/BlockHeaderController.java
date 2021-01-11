package com.nchain.headerSV.api.controller;

import com.nchain.headerSV.api.exception.BlockNotFoundException;
import com.nchain.headerSV.api.service.BlockHeaderService;
import com.nchain.headerSV.dao.model.BlockHeaderDTO;
import com.nchain.headerSV.service.network.NetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 28/07/2020
 */
@RestController
@Slf4j
public class BlockHeaderController {

    @Autowired
    private BlockHeaderService blockHeaderService;

    @Autowired
    private NetworkService networkService;

    @RequestMapping("/getHeaderByHash/{hash}")
    public BlockHeaderDTO getHeader(@PathVariable String hash) {
        try {
            return blockHeaderService.getBlockHeader(hash);
        }catch (BlockNotFoundException| NoSuchElementException  ex){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Block not found"
            );
        }
    }

    //TODO
//    @RequestMapping("/getHeaderStateByHash/{hash}")
//    public BlockHeaderQueryResult getHeaderDetails(@PathVariable String hash) {
//        try {
//
//            final BlockHeaderQueryResult blockStateByHash = blockHeaderService.getBlockStateByHash(hash);
//            return blockStateByHash;
//        }catch (BlockNotFoundException| NoSuchElementException  ex){
//            throw new ResponseStatusException(
//                    HttpStatus.NOT_FOUND, "Block not found"
//            );
//        }
//    }
//
//    @RequestMapping("/getConnectedPeersCount")
//    public PeerConnected getConnectedPeersCount() {
//            final int connectedPeersCount = networkService.getConnectedPeersCount();
//            PeerConnected peerConnected = PeerConnected.builder().peerCount(connectedPeersCount).build();
//            return peerConnected;
//
//    }

//    @RequestMapping("/getBranches")
//    public List<CachedBranch> getBranches(){
//        return blockHeaderService.getBranches();
//    }

    @RequestMapping("/purgeOrphanedBlocks")
    public void purgeOrphanedBlocks() {
        blockHeaderService.purgeOrphanedBlocks();
    }

    @RequestMapping("/purgeHeadersFromHash/{hash}")
    public void purgeHeadersFromHash(@PathVariable String hash) {blockHeaderService.purgeHeadersFromHash(hash);}

}
