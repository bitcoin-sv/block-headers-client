package com.nchain.headerSV.service.cache;

import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import com.nchain.headerSV.service.cache.cached.CachedBranch;
import com.nchain.headerSV.service.cache.cached.CachedHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 12/08/2020
 */
@Service
public class BlockChainFacade {
    @Autowired
    BlockHeaderCacheService blockHeaderCacheService;

    public BlockHeaderQueryResult getBlockFromCache(String hash) {
        BlockHeaderQueryResult  queryResult = null;
        Optional<CachedHeader> cachedHeader = Optional.empty();
        Optional<BlockHeader>  blockHeader  = blockHeaderCacheService.getUnconnectedBlocks().entrySet().stream()
               .filter( e -> e.getKey().equals(hash)).map(Map.Entry::getValue).findFirst();

        queryResult = blockHeader.isPresent()? BlockHeaderQueryResult.builder()
               .blockHeader(blockHeader.get())
               .height(0)
               .work(0)
               .state("ORPHAN").build():  getBlockHeaderQueryResult(hash);

        return queryResult;
    }

    private BlockHeaderQueryResult getBlockHeaderQueryResult(String hash) {
        Optional<CachedHeader> cachedHeader;
        cachedHeader = blockHeaderCacheService.getConnectedBlocks().entrySet().stream()
                .filter(e -> e.getKey().equals(hash)).map(Map.Entry::getValue).findFirst();

        CachedBranch branch = null;
        BlockHeaderQueryResult queryResult = null;
        if(cachedHeader.isPresent()) {
            branch = blockHeaderCacheService.getBranch(cachedHeader.get().getBranchId());


            CachedBranch  maxWorkedHoldBranch = blockHeaderCacheService.getBranches().stream().max(Comparator.comparingDouble(CachedBranch::getWork)).get();
            boolean mainBranch = false;
            String branchstate = "unknown";
            if (maxWorkedHoldBranch != null  && branch != null )
                if( maxWorkedHoldBranch.getWork() < branch.getWork() || (maxWorkedHoldBranch.getId().equals(branch.getId()) && maxWorkedHoldBranch.getWork() == branch.getWork())) {

                    mainBranch = true;
                    branchstate = "main";
                }


            queryResult = BlockHeaderQueryResult.builder()
                    .blockHeader(cachedHeader.get().getBlockHeader())
                    .height(cachedHeader.get().getHeight())
                    .work(cachedHeader.get().getWork())
                    .bestChain(mainBranch)
                    .state(branchstate).build();
        }
        return queryResult;
    }
}
