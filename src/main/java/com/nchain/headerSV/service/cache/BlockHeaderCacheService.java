package com.nchain.headerSV.service.cache;

import com.nchain.bna.tools.crypto.Sha256Wrapper;
import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import com.nchain.headerSV.dao.postgresql.repository.BlockHeaderRepository;
import com.nchain.headerSV.service.cache.cached.CachedBranch;

import com.nchain.headerSV.service.cache.cached.CachedHeader;
import com.nchain.headerSV.service.HeaderSvService;
import com.nchain.headerSV.service.propagation.buffer.MessageBufferService;
import com.nchain.headerSV.tools.Util;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.DepthFirstIterator;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 06/08/2020
 */
@Service
@Slf4j
public class BlockHeaderCacheService implements HeaderSvService {

    private final BlockHeaderRepository blockHeaderRepository;
    private Graph<String, DefaultEdge> blockChain;
    private HashMap<String, BlockHeader> unconnectedBlocks;
    private HashMap<String, CachedHeader> connectedBlocks;
    private HashMap<String, CachedBranch> branches;


    public BlockHeaderCacheService(BlockHeaderRepository blockHeaderRepository,
                                   MessageBufferService messageBufferService){
        this.blockHeaderRepository = blockHeaderRepository;

        init();
    }

    private void init(){
        //The blockchain is a directed 'tree' graph
        blockChain = GraphTypeBuilder.<String, DefaultEdge> directed().allowingMultipleEdges(false)
                .allowingSelfLoops(false).edgeClass(DefaultEdge.class).weighted(true).buildGraph();

        connectedBlocks = new HashMap<>();
        unconnectedBlocks = new HashMap<>();
        branches = new HashMap<>();

        //create the root branch
        String rootBranchId = generateBranchId(Sha256Wrapper.ZERO_HASH.toString());
        CachedBranch cachedBranch = CachedBranch.builder()
                .id(rootBranchId)
                .leafNode(Sha256Wrapper.ZERO_HASH.toString())
                .work(0).build();

        branches.put(cachedBranch.getId(), cachedBranch);

        //initialize the zero hash block for genesis to append too
        BlockHeader zeroBlockHeader = BlockHeader.builder().hash(Sha256Wrapper.ZERO_HASH.toString()).build();
        CachedHeader zeroBlockHeaderCached = CachedHeader.builder().blockHeader(zeroBlockHeader).work(0).height(-1).branchId(rootBranchId).build();

        connectedBlocks.put(zeroBlockHeader.getHash(), zeroBlockHeaderCached);
        blockChain.addVertex(zeroBlockHeader.getHash());

        //ensure genesis block exists in database
        initializeGenesis();
    }

    @Override
    public void start() {
        loadFromDatabase();
    }

    @Override
    public void stop() {

    }

    public List<CachedBranch> getBranches(){
        return new ArrayList<>(branches.values());
    }

    public CachedBranch getBranch(String branchId){
        return branches.get(branchId);
    }

    private boolean isCachable(BlockHeader blockHeader){
        return !connectedBlocks.containsKey(blockHeader.getHash()) && !unconnectedBlocks.containsKey(blockHeader.getHash());
    }

    public synchronized List<BlockHeader> addToCache(List<BlockHeader> blockHeaders){
        List<BlockHeader> uniqueBlockHeaders = blockHeaders.stream().filter(this::isCachable).collect(Collectors.toList());

        //add the vertex, connect the edges, calculate the weight + height
        uniqueBlockHeaders.forEach(this::addVertex);
        uniqueBlockHeaders.forEach(this::addEdge);

        branches.values().forEach(b -> connectBlocksFromVertex(b.getLeafNode(), true));

        return uniqueBlockHeaders;
    }


    private boolean addVertex(BlockHeader blockHeader){
        unconnectedBlocks.put(blockHeader.getHash(), blockHeader);
        try {
            blockChain.addVertex(blockHeader.getHash());
        } catch (NullPointerException ex){
            return false;
        }

        return true;
    }

    private boolean addEdge(BlockHeader blockHeader) {
        try {
            DefaultEdge edge = blockChain.addEdge(blockHeader.getPrevBlockHash(), blockHeader.getHash());
            //edge will be null if it already exists - should never be the case
            if (edge != null) {
                //edge weight will be block work for future traversals
                blockChain.setEdgeWeight(edge, calculateWork(blockHeader.getDifficultyTarget()));
                //add for processing
            }
        } catch (IllegalArgumentException ie) {
            //the parent vertex didn't exist
            log.debug("Unable to connect block: " + blockHeader);
            return false;
        }

        return true;
    }

    private void loadFromDatabase(){
        log.info("Initializing blockheader cache. Processing: " + blockHeaderRepository.count() + " entries...");
        List<BlockHeader> blockHeaderList = blockHeaderRepository.findAll();

         //add all the vertex's to the tree
        blockHeaderList.forEach(this::addVertex);
        //connect each vertex to its parent
        blockHeaderList.forEach(this::addEdge);
        //calculate height, branches and chain work
        connectBlocksFromVertex(Util.GENESIS_BLOCK_HEADER.getHash(), false);

        log.info("BlockHeader cache initialization complete");
    }

    private void connectBlocksFromVertex(String blockHeader, boolean startFromChild){
        //traverse through the built graph from genesis, and calculate the work and height given the parent
        DepthFirstIterator<String, DefaultEdge> iterator = new DepthFirstIterator(blockChain, blockHeader);

        if(startFromChild)
            iterator.next();

        while(iterator.hasNext()) {
            BlockHeader currentBlockHeader = unconnectedBlocks.get(iterator.next());
            connectBlockHeader(currentBlockHeader);
        }

    }

    private void connectBlockHeader(BlockHeader blockHeader){
        //get the header and calculate the work and height
        CachedHeader prevCachedBlockHeader = connectedBlocks.get(blockHeader.getPrevBlockHash());

        double work = Util.calculateWork(Util.decompressCompactBits(blockHeader.getDifficultyTarget())).doubleValue();
        int height = prevCachedBlockHeader.getHeight() + 1; //height is 1 greater than parent

        //get the parent branch and the cumulative work
        String branchId = prevCachedBlockHeader.getBranchId();
        double branchWork = branches.get(branchId).getWork();

        //if the vertex's parent has more than 1 edge, there's a fork (new branch)
        if(blockChain.incomingEdgesOf(blockHeader.getPrevBlockHash()).size() > 1) {
            branchId = generateBranchId(blockHeader.getHash());
        }

        //update the work if branch already exists, else add a new branch
        branches.put(branchId, CachedBranch.builder().id(branchId).work(branchWork + work).leafNode(blockHeader.getHash()).build());

        //cache the block header
        CachedHeader blockHeaderCached = CachedHeader.builder()
                .blockHeader(blockHeader)
                .work(work)
                .branchId(branchId)
                .height(height)
                .build();
        connectedBlocks.put(blockHeader.getHash(), blockHeaderCached);

        //block is now connected
        unconnectedBlocks.remove(blockHeader.getHash());

        log.info("Added block: " + blockHeader.getHash() + " to cache at height: " + height);
    }

    private String generateBranchId(String hash){
        return hash;
    }

    private double calculateWork(long target){
        return Util.calculateWork(Util.decompressCompactBits(target)).doubleValue();
    }

    private void initializeGenesis(){
        blockHeaderRepository.save(Util.GENESIS_BLOCK_HEADER);
    }
}
