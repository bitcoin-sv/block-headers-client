package com.nchain.headerSV.service.query;

import com.nchain.headerSV.domain.ChainState;
import com.nchain.headerSV.domain.dto.BlockHeaderDTO;
import com.nchain.headerSV.domain.dto.BlockHeaderStateDTO;
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

    public BlockHeaderStateDTO getBlockHeaderState(String hash){
        Optional<BlockHeader> blockHeaderOptional = blockChainStore.getBlock(Sha256Wrapper.wrap(hash));

        if(blockHeaderOptional.isEmpty()){
            return null;
        }

        BlockHeader blockHeader = blockHeaderOptional.get();
        ChainState blockHeaderState = ChainState.MAIN_CHAIN;

        //Empty if the block is not connected
        Optional<ChainInfo> chainInfoOptional = blockChainStore.getBlockChainInfo(blockHeader.getHash());
        if(chainInfoOptional.isEmpty()) {
            blockHeaderState = ChainState.ORPHAN;
        }

        //Get the chain info for this block and the longest chain
        ChainInfo headerChainInfo = chainInfoOptional.get();
        ChainInfo longestChainInfo = blockChainStore.getLongestChain().get();

        //Check if the block is in the longest chain
        if(!headerChainInfo.equals(longestChainInfo)){
            blockHeaderState = ChainState.STALE;
        }

        ChainStateDTO chainStateDTO = ChainStateDTO.builder()
                .state(blockHeaderState.name())
                .chainWork(headerChainInfo.getChainWork())
                .height(headerChainInfo.getHeight())
                .build();

        return BlockHeaderStateDTO.builder()
                .blockHeader(BlockHeaderDTO.of(blockHeader)) //TODO confirmations
                .chainState(chainStateDTO) //TODO confidence
                .build(); //TODO should we consider returning which network we're connected too?
    }



    public List<PeerAddressDTO> getConnectedPeers() {
        return networkService.getConnectedPeers().stream().map(PeerAddressDTO::of).collect(Collectors.toList());
    }

    public List<ChainStateDTO> getChainTips() {

        if(blockChainStore.getTipsChains().isEmpty()){
            return Collections.emptyList();
        }

        //Get the tip hash and convert to ChanInfo
        List<ChainInfo> chainTips = blockChainStore.getTipsChains().stream().map(h -> blockChainStore.getBlockChainInfo(h).get()).collect(Collectors.toList());
        //Find the tip with the most work to identify main chain
        ChainInfo mainChainTip = chainTips.stream().max(Comparator.comparing(ChainInfo::getChainWork)).get();

        //Build DTO from tips
        List<ChainStateDTO> chainStateDTOS = chainTips.stream()
                .map(ci -> ChainStateDTO.builder()
                        .chainWork(ci.getChainWork())
                        .height(ci.getHeight())
                        .state(mainChainTip.equals(ci) ? ChainState.MAIN_CHAIN.name() : ChainState.ORPHAN.name())
                        .tip(BlockHeaderDTO.of(ci.getHeader())) //TODO calculate work properly
                        .build())
                .collect(Collectors.toList());

        return chainStateDTOS;
    }

    public void pruneChain(String hash) {
        blockChainStore.prune(Sha256Wrapper.wrap(hash), false);
    }
}
