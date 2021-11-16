package io.bitcoinsv.headerSV.api;

import io.bitcoinsv.headerSV.domain.ChainState;
import io.bitcoinsv.headerSV.domain.dto.BlockHeaderDTO;
import io.bitcoinsv.headerSV.domain.dto.ChainStateDTO;
import io.bitcoinsv.headerSV.domain.dto.PeerAddressDTO;
import io.bitcoinsv.headerSV.service.network.NetworkService;
import io.bitcoinsv.jcl.store.blockChainStore.BlockChainStore;
import io.bitcoinsv.bitcoinjsv.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinsv.bitcoinjsv.bitcoin.api.extended.ChainInfo;
import io.bitcoinsv.bitcoinjsv.core.Sha256Hash;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
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
        Optional<HeaderReadOnly> blockHeader = blockChainStore.getBlock(Sha256Hash.wrap(hash));

        if(blockHeader.isEmpty()){
            return null;
        }

        return BlockHeaderDTO.of(blockHeader.get());
    }

    public List<BlockHeaderDTO> getHeadersByHeight(Integer height, Integer count){
        List<BlockHeaderDTO> listBlockHeaders = new ArrayList<>();
        ChainInfo longestChainInfo = blockChainStore.getLongestChain().get();
        HeaderReadOnly chainTip = longestChainInfo.getHeader();

        // validation of inputs
        int lastHeaderHeight = height + count - 1;
        if (height > longestChainInfo.getHeight()) {
            throw new IllegalArgumentException(String.format("Header at height %s exceeds the chain tip: %s", height, longestChainInfo.getHeight()));
        } else if (lastHeaderHeight > longestChainInfo.getHeight()) {
            lastHeaderHeight = longestChainInfo.getHeight();
        }

        for(int curHeight = height; curHeight<=lastHeaderHeight; curHeight++)
        {
            Optional<ChainInfo> header = blockChainStore.getAncestorByHeight(chainTip.getHash(), curHeight);
            if (header.isEmpty()) {
                throw new IllegalStateException(String.format("Header at height %s not found", curHeight));
            }
            listBlockHeaders.add(BlockHeaderDTO.of(header.get().getHeader()));
        }
        return listBlockHeaders;
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
        Optional<HeaderReadOnly> blockHeaderOptional = blockChainStore.getBlock(Sha256Hash.wrap(hash));

        if(blockHeaderOptional.isEmpty()){
            return null;
        }

        HeaderReadOnly blockHeader = blockHeaderOptional.get();
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
        ChainInfo headerlongestChainInfo = blockChainStore.getTipsChains(Sha256Hash.wrap(hash))
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
                .confirmations(headerlongestChainInfo.getHeight() - headerChainInfo.getHeight() + 1)
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
                        .confirmations(1)
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

        blockChainStore.prune(Sha256Hash.wrap(hash), false);
    }

}
