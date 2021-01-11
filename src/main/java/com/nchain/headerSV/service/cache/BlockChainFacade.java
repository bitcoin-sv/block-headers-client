package com.nchain.headerSV.service.cache;

import com.nchain.headerSV.dao.model.BlockHeaderDTO;
import com.nchain.headerSV.service.sync.BlockHeaderSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 12/08/2020
 */
@Service
public class BlockChainFacade {



    @Autowired
    BlockHeaderSyncService blockHeaderSyncService;

    private enum BranchState {
        MAIN_CHAIN,
        ORPHAN,
        STALE
    }

    //TODO
//    public List<CachedBranch> getBranches(){
//        //TODO
////        return new ArrayList<>(blockHeaderCacheService.getBranches().values());
//        return null;
//    }

    public void purgeOrphanedBlocks(){
        //TODO
       // blockHeaderCacheService.purgeOrphanedBlocks();
    }

    public void purgeHeadersFromHash(String headerHash) {
        //TODO
//       blockHeaderCacheService.purgeHeadersFromHash(headerHash);
//        blockHeaderSyncService.clearMesssageCache();
//        blockHeaderSyncService.requestHeaders();
    }

    public BlockHeaderDTO getBlockFromCache(String hash) {
        //TODO
//        BlockHeaderQueryResult queryResult = null;
//        CachedHeader blockHeader = blockHeaderCacheService.getUnconnectedBlocks().get(hash);
//
//        queryResult = blockHeader != null ? BlockHeaderQueryResult.builder()
//                .blockHeader(blockHeader.getBlockHeader())
//                .height(blockHeader.getHeight())
//                .work(blockHeader.getWork())
//                .state(BranchState.ORPHAN.name())
//                .build() : getBlockHeaderQueryResult(hash);
//
//        return queryResult;
        return null;
    }

    private BlockHeaderDTO getBlockHeaderQueryResult(String hash) {
        //TODO
//        CachedHeader cachedHeader = blockHeaderCacheService.getConnectedBlocks().get(hash);
//
//        CachedBranch branch;
//        BlockHeaderQueryResult queryResult = null;
//        if (cachedHeader != null) {
//            branch = blockHeaderCacheService.getBranch(cachedHeader.getBranchId());
//
//
//            CachedBranch maxWorkedHoldBranch = blockHeaderCacheService.getBranches().values().stream().max(Comparator.comparingDouble(CachedBranch::getWork)).get();
//            boolean mainBranch = false;
//            int confirmations = 0;
//            String branchstate = BranchState.STALE.name();
//            if (maxWorkedHoldBranch != null && branch != null)
//                if (isBranchConnected(branch.getId(), maxWorkedHoldBranch.getId())) {
//                    mainBranch = true;
//                    branchstate = BranchState.MAIN_CHAIN.name();
//                    confirmations = branch.getHeight() - cachedHeader.getHeight();
//                }
//
//            queryResult = BlockHeaderQueryResult.builder()
//                    .blockHeader(cachedHeader.getBlockHeader())
//                    .height(Integer.valueOf(cachedHeader.getHeight()))
//                    .work(cachedHeader.getWork())
//                    .cumulativeWork(cachedHeader.getCumulativeWork())
//                    .bestChain(mainBranch)
//                    .confirmations(confirmations)
//                    .chainConfidence(branch.getConfidence())
//                    .state(branchstate).build();
//        }
//        return queryResult;
        return null;
    }

    private Boolean isBranchConnected(String childBranchId, String parentBranchId) {
        //TODO
//        HashMap<String, CachedBranch> branches = blockHeaderCacheService.getBranches();
//
//        for (CachedBranch branch = branches.get(parentBranchId);
//             branch != null;
//             branch = branches.get(branch.getParentBranchId())) {
//            if (branch.getId() == childBranchId) {
//                return true;
//            }
//        }

        return false;
    }
}
