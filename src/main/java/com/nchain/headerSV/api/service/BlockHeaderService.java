package com.nchain.headerSV.api.service;

import com.nchain.headerSV.api.exception.BlockNotFoundException;
import com.nchain.headerSV.dao.model.BlockHeaderDTO;
import com.nchain.headerSV.service.cache.BlockChainFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 28/07/2020
 */
@Service
public class BlockHeaderService {

    @Autowired
    private BlockChainFacade blockChainFacade;

    public BlockHeaderDTO getBlockHeader(String hash) throws BlockNotFoundException {
        //TODO
//        Optional<BlockHeaderDTO> blockHeaderDTO = persistenceService.retrieveBlockHeader(hash);
//        BlockHeaderDTO blockHeader  =  blockHeaderDTO.get();
//
//        if(null == blockHeader) throw new BlockNotFoundException();
//
//        return blockHeader;
        return null;

    }

    public BlockHeaderDTO  getBlockStateByHash(String hash) throws BlockNotFoundException{
    //TODO
//        BlockHeaderQueryResult block = blockChainFacade.getBlockFromCache(hash);
//
//        if(null == block) throw new BlockNotFoundException();
//
//        return block;

        return null;
    }

    public void purgeOrphanedBlocks(){
        blockChainFacade.purgeOrphanedBlocks();
    }

    public void purgeHeadersFromHash(String hash){
        blockChainFacade.purgeHeadersFromHash(hash);
    }

//    public List<CachedBranch> getBranches(){
//       return blockChainFacade.getBranches();
//    }
}
