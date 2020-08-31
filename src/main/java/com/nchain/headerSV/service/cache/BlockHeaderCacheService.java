package com.nchain.headerSV.service.cache;

import com.nchain.jcl.tools.crypto.Sha256Wrapper;
import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import com.nchain.headerSV.dao.postgresql.repository.BlockHeaderRepository;
import com.nchain.headerSV.service.cache.cached.CachedBranch;
import com.nchain.headerSV.service.cache.cached.CachedHeader;
import com.nchain.headerSV.service.HeaderSvService;
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



    public BlockHeaderCacheService(BlockHeaderRepository blockHeaderRepository){
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

        //create and add the root branch the genesis will connect too
        String rootBranchId = generateBranchId(Sha256Wrapper.ZERO_HASH.toString());
        CachedBranch cachedRootBranch = CachedBranch.builder()
                .id(rootBranchId)
                .leafNode(Sha256Wrapper.ZERO_HASH.toString())
                .work(Double.valueOf(0)).build();

        branches.put(cachedRootBranch.getId(), cachedRootBranch);

        //initialize the root hash for genesis to append too
        BlockHeader rootBlockHeader = BlockHeader.builder().hash(Sha256Wrapper.ZERO_HASH.toString()).build();
        CachedHeader rootBlockHeaderCached = CachedHeader.builder().blockHeader(rootBlockHeader).work(0).height(-1).branchId(rootBranchId).build();

        //root 'header' is connected by default
        connectedBlocks.put(rootBlockHeader.getHash(), rootBlockHeaderCached);
        blockChain.addVertex(rootBlockHeader.getHash());

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

    public synchronized List<CachedBranch> getBranches(){
        return new ArrayList<>(branches.values());
    }

    public Integer getMinBranchHeight() {
        Set<Integer> brachHeights = branches.values().stream().map(b-> connectedBlocks.get(b.getLeafNode()).getHeight()).collect(Collectors.toSet());

        return Collections.min(brachHeights);
    }

    public Integer getMaxBranchHeight() {
        Set<Integer> brachHeights = branches.values().stream().map(b-> connectedBlocks.get(b.getLeafNode()).getHeight()).collect(Collectors.toSet());

        return Collections.max(brachHeights);
    }

    public CachedBranch getBranch(String branchId){
        return branches.get(branchId);
    }

    public synchronized Set<BlockHeader> addToCache(List<BlockHeader> blockHeaders){
        //Filter out any duplicate headers, and those which are already cached(processed or unprocessed)
        Set<BlockHeader> uniqueBlockHeaders = blockHeaders.stream().filter(b -> !connectedBlocks.containsKey(b.getHash()) && !unconnectedBlocks.containsKey(b.getHash())).collect(Collectors.toSet());

        //we want to process the unique headers, and reattempt connecting any existing unconnectedBlocks
        Set<BlockHeader> headersToProcess = new HashSet<>(uniqueBlockHeaders);
        headersToProcess.addAll(unconnectedBlocks.values());

        //add the vertex, connect the edges, connect any blocks that are forked
        headersToProcess.forEach(this::addVertex);
        headersToProcess.forEach(this::addEdge);
        headersToProcess.forEach(this::connectForkedBlock);

        //process the graph, building the newly added nodes from the branch tips. Any blocks unconnected after the graph has been built are orphans
        buildGraph();

        return uniqueBlockHeaders;
    }


    private boolean addVertex(BlockHeader blockHeader){
        try {
            //add the vertex to the graph, and the list of unconnected blocks for processing
            blockChain.addVertex(blockHeader.getHash());
            unconnectedBlocks.put(blockHeader.getHash(), blockHeader);
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
            }
        } catch (IllegalArgumentException ie) {
            //the parent vertex didn't exist
            return false;
        }

        return true;
    }

    private void connectForkedBlock(BlockHeader blockHeader){
        try {
            //if the vertex's parent has more than 1 edge, there's a fork (new branch)
            if (blockChain.outgoingEdgesOf(blockHeader.getPrevBlockHash()).size() > 1) {
                //We need to connect any blocks which are forks here, so they can be traversed when the graph is built. If they cannot be connected,
                //then they are either Orphans, or branches of unconnected blocks. Connecting the block will generate and update the branch automatically.
                if(connectedBlocks.get(blockHeader.getPrevBlockHash()) != null){
                   connectBlockToParent(blockHeader);
                }
            }
        } catch (IllegalArgumentException ex){
            //parent vertex not found, this is an orphan block
        }

    }

    private void loadFromDatabase(){
        log.info("Initializing blockheader cache. Processing: " + blockHeaderRepository.count() + " entries...");
        List<BlockHeader> blockHeaderList = blockHeaderRepository.findAll();

        addToCache(blockHeaderList);

        log.info("BlockHeader cache initialization complete");
    }

    /*
     * This function will do a depth first traversal from the tip of each branch which is connected to the main tree. We identify connected branches by those
     * which have work. If the work is null, it means that it's a branch of a block that is not yet connected. This branch will be traversed and calculated,
     * unless it is an orphan group of blocks, and therefore a branch which is not connected to the main tree so it will be ignored.
     *
     * Once traversed, each branch will be updated to reflect the state of the tree, and future traversals will be from the newly connected blocks, so we never
     * traverse the same block twice.
     *
     */
    private void buildGraph(){
        //We only want to traverse branches that are "connected" to the main branch (i.e work != null), else could end up traversing the same branch twice.
        branches.values().stream().filter(b -> b.getWork() != null).forEach(b -> {
            DepthFirstIterator<String, DefaultEdge> iterator = new DepthFirstIterator(blockChain, b.getLeafNode());

            // The leaf node is already connected, unless we've created a new branch
            iterator.next();

            //traverse the child nodes of the branch, anything below the leaf will be unconnected
            while(iterator.hasNext()) {
                BlockHeader currentBlockHeader = unconnectedBlocks.get(iterator.next());
                connectBlockToParent(currentBlockHeader);
            }
        });
    }

    private void connectBlockToParent(BlockHeader blockHeader){
        //get the header and calculate the work and height
        CachedHeader parentCachedBlockHeader = connectedBlocks.get(blockHeader.getPrevBlockHash());

        Double work = calculateWork(blockHeader.getDifficultyTarget());
        int height = parentCachedBlockHeader.getHeight() + 1; //height is 1 greater than parent

        //dynamically calculate and get the parent branch and the cumulative work
        String branchId = parentCachedBlockHeader.getBranchId();
        double branchWork = branches.get(branchId).getWork();

        //if the vertex's parent has more than 1 edge, there's a fork (new branch)
        if(blockChain.outgoingEdgesOf(blockHeader.getPrevBlockHash()).size() > 1) {
            branchId = generateBranchId(blockHeader.getHash());
        }

        //update the work for the existing branch
        branches.put(branchId, CachedBranch.builder().id(branchId).work(branchWork + work).leafNode(blockHeader.getHash()).build());

        //cache the block header
        CachedHeader blockHeaderCached = CachedHeader.builder()
                .blockHeader(blockHeader)
                .work(work)
                .branchId(branchId)
                .height(height)
                .build();

        //block is now connected
        connectedBlocks.put(blockHeader.getHash(), blockHeaderCached);
        unconnectedBlocks.remove(blockHeader.getHash());

        log.info("Added block: " + blockHeader.getHash() + " to cache at height: " + height);
    }

    public HashMap<String, BlockHeader> getUnconnectedBlocks() {
        return unconnectedBlocks;
    }

    public HashMap<String, CachedHeader> getConnectedBlocks() {
        return connectedBlocks;
    }

    private String generateBranchId(String hash){
        // the branch id is the first child nodes hash
        return hash;
    }

    private double calculateWork(long target){
        return Util.calculateWork(Util.decompressCompactBits(target)).doubleValue();
    }

    private void initializeGenesis(){
        blockHeaderRepository.save(Util.GENESIS_BLOCK_HEADER);
    }
}
