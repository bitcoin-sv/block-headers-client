package com.nchain.headerSV.api;

import com.nchain.headerSV.domain.ChainState;
import com.nchain.headerSV.domain.dto.BlockHeaderDTO;
import com.nchain.headerSV.domain.dto.ChainStateDTO;
import com.nchain.headerSV.domain.dto.PeerAddressDTO;
import com.nchain.headerSV.service.network.NetworkService;
import com.nchain.jcl.base.domain.api.base.BlockHeader;
import com.nchain.jcl.base.domain.api.extended.ChainInfo;
import com.nchain.jcl.base.tools.crypto.Sha256Wrapper;
import com.nchain.jcl.store.blockChainStore.BlockChainStore;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 12/08/2020
 */
@Service
public class HSVFacade {

    private final BlockChainStore blockChainStore;

    private final NetworkService networkService;

    public HSVFacade(BlockChainStore blockChainStore,
                     NetworkService networkService){
        this.blockChainStore = blockChainStore;
        this.networkService = networkService;
    }

    public BlockHeaderDTO getBlockHeader(String hash){
        Optional<BlockHeader> blockHeader = blockChainStore.getBlock(Sha256Wrapper.wrap(hash));

        if(blockHeader.isEmpty()){
            return null;
        }

        return BlockHeaderDTO.of(blockHeader.get());
    }

    public List<BlockHeaderDTO> getAncestors(String hash, String ancestorHash){
        Optional<ChainInfo> requestedBlock = blockChainStore.getBlockChainInfo(Sha256Hash.wrap(hash));
        Optional<ChainInfo> ancestorBlock = blockChainStore.getBlockChainInfo(Sha256Hash.wrap(ancestorHash));

        if(requestedBlock.isEmpty() || ancestorBlock.isEmpty()){
            return null;
        }

        if(ancestorBlock.get().getHeight() > requestedBlock.get().getHeight()){
            return null;
        } else if(ancestorBlock.get().getHeight() == requestedBlock.get().getHeight()){
            return Collections.emptyList();
        }

        List<BlockHeaderDTO> ancestorList = new ArrayList<>();
        HeaderReadOnly currentBlock = blockChainStore.getBlock(requestedBlock.get().getHeader().getPrevBlockHash()).get();

        while(!currentBlock.equals(ancestorBlock.get().getHeader())){

            ancestorList.add(BlockHeaderDTO.of(currentBlock));

            currentBlock = blockChainStore.getBlock(currentBlock.getPrevBlockHash()).get();
        }

        return ancestorList;
    }

    public BlockHeaderDTO findCommonAncestor(List<String> blockHashes) {
        List<Sha256Hash> blockHashesSHA256 = blockHashes.stream().map(Sha256Hash::wrap).collect(Collectors.toList());

        Optional<ChainInfo> lowestCommonAncestor = blockChainStore.getLowestCommonAncestor(blockHashesSHA256);

        if(lowestCommonAncestor.isEmpty()){
            return null;
        }

        return BlockHeaderDTO.of(lowestCommonAncestor.get().getHeader());
    }


    public ChainStateDTO getBlockHeaderState(String hash){
        Optional<BlockHeader> blockHeaderOptional = blockChainStore.getBlock(Sha256Wrapper.wrap(hash));

        if(blockHeaderOptional.isEmpty()){
            return null;
        }

        BlockHeader blockHeader = blockHeaderOptional.get();
        ChainState blockHeaderState = ChainState.LONGEST_CHAIN;

        //Empty if the block is not connected
        Optional<ChainInfo> chainInfoOptional = blockChainStore.getBlockChainInfo(blockHeader.getHash());
        if(chainInfoOptional.isEmpty()) {
            blockHeaderState = ChainState.ORPHAN;
        }

        //Get the chain info for this block
        ChainInfo headerChainInfo = chainInfoOptional.get();

        //Get the longest chain info
        ChainInfo longestChainInfo = blockChainStore.getLongestChain().get();

        //Get the longest chain this header appears in
        ChainInfo headerlongestChainInfo = blockChainStore.getTipsChains(Sha256Wrapper.wrap(hash))
                .stream()
                .map(headerHash -> blockChainStore.getBlockChainInfo(headerHash))
                .max(Comparator.comparing(chainInfo -> chainInfo.get().getChainWork()))
                .get().get();

        //If the tip of the requested headers work is less than the work of the longest chain, then it's stale
        if(headerlongestChainInfo.getChainWork().compareTo(longestChainInfo.getChainWork()) < 0){
            blockHeaderState = ChainState.STALE;
        }

        return ChainStateDTO.builder()
                    .header(BlockHeaderDTO.of(blockHeader))
                    .state(blockHeaderState.name())
                    .chainWork(headerChainInfo.getChainWork())
                    .height(headerChainInfo.getHeight())
                    .confirmations(headerlongestChainInfo.getHeight() - headerChainInfo.getHeight())
                    .build();
    }



    public List<PeerAddressDTO> getConnectedPeers() {
        return networkService.getConnectedPeers().stream().map(PeerAddressDTO::of).collect(Collectors.toList());
    }

    public List<ChainStateDTO> getChainTips() {

        if(blockChainStore.getTipsChains().isEmpty()){
            return Collections.emptyList();
        }

        //Get the tip hash and convert to ChainInfo
        List<ChainInfo> chainTips = blockChainStore.getTipsChains().stream().map(h -> blockChainStore.getBlockChainInfo(h).get()).collect(Collectors.toList());
        //Find the tip with the most work to identify main chain used for identifying state
        ChainInfo mainChainTip = chainTips.stream().max(Comparator.comparing(ChainInfo::getChainWork)).get();

        //Build DTO from tips
        List<ChainStateDTO> chainStateDTOS = chainTips.stream()
                .map(ci -> ChainStateDTO.builder()
                        .chainWork(ci.getChainWork())
                        .height(ci.getHeight())
                        .state(mainChainTip.equals(ci) ? ChainState.LONGEST_CHAIN.name() : ChainState.STALE.name())
                        .header(BlockHeaderDTO.of(ci.getHeader()))
                        .confirmations(0)
                        .build())
                .collect(Collectors.toList());

        return chainStateDTOS;
    }

    public void pruneChain(String hash) {
        //Get the tip hash and convert to ChainInfo
        List<ChainInfo> chainTips = blockChainStore.getTipsChains().stream().map(h -> blockChainStore.getBlockChainInfo(h).get()).collect(Collectors.toList());
        //Find the tip with the most work to identify main chain used for identifying state
        ChainInfo mainChainTip = chainTips.stream().max(Comparator.comparing(ChainInfo::getChainWork)).get();
        
        if(mainChainTip.getHeader().getHash().toString().equals(hash)){
            throw new UnsupportedOperationException("Cannot prune the longest chain.");
        }

        blockChainStore.prune(Sha256Wrapper.wrap(hash), false);
    }

}
