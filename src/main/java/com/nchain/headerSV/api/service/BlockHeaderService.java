package com.nchain.headerSV.api.service;

import com.nchain.headerSV.api.exception.BlockNotFoundException;
import com.nchain.headerSV.dao.model.BlockHeaderDTO;
import com.nchain.headerSV.dao.service.PersistenceService;
import com.nchain.headerSV.service.cache.BlockChainFacade;
import com.nchain.headerSV.service.cache.BlockHeaderQueryResult;
import com.nchain.headerSV.service.cache.cached.CachedBranch;
import com.nchain.headerSV.service.cache.cached.CachedHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 28/07/2020
 */
@Service
public class BlockHeaderService {

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private BlockChainFacade blockChainFacade;

    public BlockHeaderDTO getBlockHeader(String hash) throws BlockNotFoundException {
        Optional<BlockHeaderDTO> blockHeaderDTO = persistenceService.retrieveBlockHeader(hash);
        BlockHeaderDTO blockHeader  =  blockHeaderDTO.get();

        if(null == blockHeader) throw new BlockNotFoundException();

        return blockHeader;

    }

    public BlockHeaderQueryResult  getBlockStateByHash(String hash) throws BlockNotFoundException{

        BlockHeaderQueryResult block = blockChainFacade.getBlockFromCache(hash);

        if(null == block) throw new BlockNotFoundException();

        return block;

    }

    public void purgeOrphanedBlocks(){
        blockChainFacade.purgeOrphanedBlocks();
    }

    public List<CachedBranch> getBranches(){
       return blockChainFacade.getBranches();
    }
}
