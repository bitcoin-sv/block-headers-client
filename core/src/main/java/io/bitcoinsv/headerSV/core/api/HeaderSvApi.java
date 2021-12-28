package io.bitcoinsv.headerSV.core.api;

import com.google.common.collect.ImmutableList;
import io.bitcoinsv.bitcoinjsv.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinsv.bitcoinjsv.bitcoin.api.extended.ChainInfo;
import io.bitcoinsv.bitcoinjsv.core.Sha256Hash;
import io.bitcoinsv.headerSV.core.service.network.NetworkService;
import io.bitcoinsv.headerSV.core.service.storage.StorageService;
import io.bitcoinsv.jcl.net.network.PeerAddress;


import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 * @author i.fernandez@nchain.com
 */
public class HeaderSvApi {

    // Services needed by the Api:
    private final StorageService storeService;
    private final NetworkService networkService;

    /** Constructor */
    public HeaderSvApi(StorageService storeService,
                       NetworkService networkService){
        this.storeService = storeService;
        this.networkService = networkService;
    }

    /**
     * Returns info about a Block Hash in the blockchain
     * @param   hash block Hash in HEX format
     * @return  the Block Header, or null if it doesn't exist in the chain (it might be possible that we are still
     *          synchronizing
     */
    public HeaderReadOnly getBlockHeader(String hash){
        Optional<HeaderReadOnly> blockHeader = storeService.getBlock(Sha256Hash.wrap(hash));
        return blockHeader.orElseGet(() -> null);
    }

    /**
     * Returns a list of all the Headers that are stored starting at a particular height in the chain.
     * If there is a fork, there might be several Header at the same height
     * @param height        Height of the chain
     * @param numHeaders    Num of Headers to retrieve (max 2000)
     * @return              a list of headers that start at the height given, up to "numHeaders" headers.
     */
    public List<HeaderReadOnly> getHeadersByHeight(Integer height, Integer numHeaders){
        List<HeaderReadOnly> listBlockHeaders = new ArrayList<>();
        ChainInfo longestChainInfo = storeService.getLongestChain().get();
        HeaderReadOnly chainTip = longestChainInfo.getHeader();

        // validation of inputs
        int lastHeaderHeight = height + numHeaders - 1;
        if (height > longestChainInfo.getHeight()) {
            throw new IllegalArgumentException(String.format("Header at height %s exceeds the chain tip: %s", height, longestChainInfo.getHeight()));
        } else if (lastHeaderHeight > longestChainInfo.getHeight()) {
            lastHeaderHeight = longestChainInfo.getHeight();
        }

        for(int curHeight = height; curHeight<=lastHeaderHeight; curHeight++)
        {
            Optional<ChainInfo> header = storeService.getAncestorByHeight(chainTip.getHash(), curHeight);
            if (header.isEmpty()) {
                throw new IllegalStateException(String.format("Header at height %s not found", curHeight));
            }
            listBlockHeaders.add(header.get().getHeader());
        }
        return listBlockHeaders;
    }

    /**
     * Returns the list of Block Headers that sit between a hash given and an ancestor given. The ancestor can be a
     * direct ancestor or any other ancestor in the history line of ancestors of that block
     *
     * @param hash              Hash of a Block
     * @param ancestorHash      Hash of an ancestor of "hash" (previous parameter).
     * @return                  List of Blocks between the ancestor and the block given.
     */
    public List<HeaderReadOnly> getAncestors(String hash, String ancestorHash){
        Optional<ChainInfo> requestedBlock = storeService.getBlockChainInfo(Sha256Hash.wrap(hash));
        Optional<ChainInfo> ancestorBlock = storeService.getBlockChainInfo(Sha256Hash.wrap(ancestorHash));

        if(requestedBlock.isEmpty() || ancestorBlock.isEmpty()){
            return null;
        }

        if(ancestorBlock.get().getHeight() > requestedBlock.get().getHeight()){
            return null;
        } else if(ancestorBlock.get().getHeight() == requestedBlock.get().getHeight()){
            return Collections.emptyList();
        }

        List<HeaderReadOnly> ancestorList = new ArrayList<>();
        HeaderReadOnly currentBlock = storeService.getBlock(requestedBlock.get().getHeader().getPrevBlockHash()).get();

        while(!currentBlock.equals(ancestorBlock.get().getHeader())){
            ancestorList.add(currentBlock);
            currentBlock = storeService.getBlock(currentBlock.getPrevBlockHash()).get();
        }

        return ancestorList;
    }

    /**
     * It returns the Hash of the block that is a common ancestor of all the Block hashes given.
     * @param blockHashes   List of block hashes from which we want to know the common ancestor
     * @return              Hash of the common ancestor or null if there is none.
     */
    public HeaderReadOnly findCommonAncestor(List<String> blockHashes) {
        if (blockHashes == null || blockHashes.isEmpty()) return null;

        List<Sha256Hash> blockHashesSHA256 = blockHashes.stream().map(Sha256Hash::wrap).collect(Collectors.toList());
        Optional<ChainInfo> lowestCommonAncestor = storeService.getLowestCommonAncestor(blockHashesSHA256);
        if(lowestCommonAncestor.isEmpty()){
            return null;
        }
        return lowestCommonAncestor.get().getHeader();
    }

    /**
     * Returns the State of a Block, whichis a placeholder that combines several pieces of information of that block
     * @param hash  Block hash
     * @return      The state of a block Header, or null if the Block is not in the cahin
     */
    public ChainHeaderInfo getBlockHeaderState(String hash){
        Optional<HeaderReadOnly> blockHeaderOptional = storeService.getBlock(Sha256Hash.wrap(hash));

        if(blockHeaderOptional.isEmpty()){
            return null;
        }

        HeaderReadOnly blockHeader = blockHeaderOptional.get();
        ChainState blockHeaderState = ChainState.LONGEST_CHAIN;

        BigInteger chainWork = null;
        Integer chainHeight = null;
        Integer blockConfirmations = null;

        // Empty if the block is not connected
        Optional<ChainInfo> chainInfoOptional = storeService.getBlockChainInfo(blockHeader.getHash());
        if(chainInfoOptional.isEmpty()) {
            blockHeaderState = ChainState.ORPHAN;
        } else {
            // Get the longest chain info
            ChainInfo longestChainInfo = storeService.getLongestChain().get();

            // Get the longest chain this header appears inf
            ChainInfo headerlongestChainInfo = storeService.getTipsChains(Sha256Hash.wrap(hash))
                    .stream()
                    .map(headerHash -> storeService.getBlockChainInfo(headerHash))
                    .max(Comparator.comparing(chainInfo -> chainInfo.get().getChainWork()))
                    .get().get();

            chainWork = chainInfoOptional.get().getChainWork();
            chainHeight = chainInfoOptional.get().getHeight();
            blockConfirmations = headerlongestChainInfo.getHeight() - chainInfoOptional.get().getHeight() + 1;

            //If the tip of the requested headers work is less than the work of the longest chain, then it's stale
            if (headerlongestChainInfo.getChainWork().compareTo(longestChainInfo.getChainWork()) < 0) {
                blockHeaderState = ChainState.STALE;
            }
        }

        return ChainHeaderInfo.builder()
                .header(blockHeader)
                .state(blockHeaderState.name())
                .chainWork(chainWork)
                .height(chainHeight)
                .confirmations(blockConfirmations)
                .build();
    }

    /**
     * Returns the List of Connected Peers at the moment
     * @return List of Peer addresses
     */
    public List<PeerAddress> getConnectedPeers() {
        return ImmutableList.copyOf(networkService.getConnectedPeers());
    }

    /**
     * Returns the Info/State of all the current Tips. In case of possible forks, there might be more than one Tip
     * @return  List of States
     */
    public List<ChainHeaderInfo> getChainTips() {

        if(storeService.getTipsChains().isEmpty()){
            return Collections.emptyList();
        }

        // Get the tip hash and convert to ChainInfo
        List<ChainInfo> chainTips = storeService.getTipsChains().stream().map(h -> storeService.getBlockChainInfo(h).get()).collect(Collectors.toList());
        //Find the tip with the most work to identify main chain used for identifying state
        ChainInfo mainChainTip = chainTips.stream().max(Comparator.comparing(ChainInfo::getChainWork)).get();

        // Build DTO from tips
        List<ChainHeaderInfo> chainStateDTOS = chainTips.stream()
                .map(ci -> ChainHeaderInfo.builder()
                        .chainWork(ci.getChainWork())
                        .height(ci.getHeight())
                        .state(mainChainTip.equals(ci) ? ChainState.LONGEST_CHAIN.name() : ChainState.STALE.name())
                        .header(ci.getHeader())
                        .confirmations(1)
                        .build())
                .collect(Collectors.toList());

        return chainStateDTOS;
    }

    /**
     * It prunes/removes a complete branch of the blockchain, which root is specified by the Block hash given
     * @param hash Hash of the Root of the branch we want to prune
     */
    public void pruneChain(String hash) {
        // Get the tip hash and convert to ChainInfo
        List<ChainInfo> chainTips = storeService.getTipsChains().stream().map(h -> storeService.getBlockChainInfo(h).get()).collect(Collectors.toList());
        // Find the tip with the most work to identify main chain used for identifying state
        ChainInfo mainChainTip = chainTips.stream().max(Comparator.comparing(ChainInfo::getChainWork)).get();

        if(mainChainTip.getHeader().getHash().toString().equals(hash)){
            throw new UnsupportedOperationException("Cannot prune the longest chain.");
        }
        storeService.prune(Sha256Hash.wrap(hash), false);
    }

}
