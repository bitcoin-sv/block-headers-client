package com.nchain.headerSV.service.cache;

import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import com.nchain.headerSV.dao.postgresql.repository.BlockHeaderRepository;
import com.nchain.headerSV.service.HeaderSvService;
import com.nchain.headerSV.service.cache.cached.CachedBranch;
import com.nchain.headerSV.service.cache.cached.CachedHeader;
import com.nchain.headerSV.tools.Util;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.DepthFirstIterator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author m.fletcher@nchain.com
  * Copyright (c) 2018-2020 nChain Ltd
 * @date 06/08/2020
 */
@Service
@Slf4j
@ConfigurationProperties(prefix = "headersv.cache")
public class BlockHeaderCacheService implements HeaderSvService {

    private final BlockHeaderRepository blockHeaderRepository;

    private Graph<String, DefaultEdge> blockChain;
    private HashMap<String, CachedHeader> unconnectedBlocks = new HashMap<>();
    private HashMap<String, CachedHeader> connectedBlocks = new HashMap<>();
    private HashMap<String, CachedBranch> branches = new HashMap<>();
    private HashMap<String, BlockHeader> blocksToPersist = new HashMap<>();

    private ScheduledExecutorService executor;

    @Setter
    private Integer cacheFlushThreshold;

    @Setter
    private Integer lastFlushTimeMaxIntervalMs;

    @Setter
    private Integer lastFlushTimeMinIntervalMs;

    private long lastFlushTime;

    public BlockHeaderCacheService(BlockHeaderRepository blockHeaderRepository){
        this.blockHeaderRepository = blockHeaderRepository;

        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    private void init(){
        //The blockchain is a directed 'tree' graph
        blockChain = GraphTypeBuilder.<String, DefaultEdge> directed().allowingMultipleEdges(false)
                .allowingSelfLoops(false).edgeClass(DefaultEdge.class).weighted(true).buildGraph();

        //create and add the root branch the genesis will connect too
        String rootBranchId = generateBranchId(Util.ROOT_BLOCK_HEADER.getHash());
        CachedBranch cachedRootBranch = CachedBranch.builder()
                .id(rootBranchId)
                .leafNode(Util.ROOT_BLOCK_HEADER.getHash())
                .confidence(Long.MAX_VALUE)
                .work(Double.valueOf(0)).build();

        branches.put(cachedRootBranch.getId(), cachedRootBranch);

        //initialize the root hash for genesis to append too
        BlockHeader rootBlockHeader = Util.ROOT_BLOCK_HEADER;
        CachedHeader rootBlockHeaderCached = CachedHeader.builder().blockHeader(rootBlockHeader).work(Double.valueOf(0)).cumulativeWork(Double.valueOf(0)).height(-1).branchId(rootBranchId).build();

        //root 'header' is connected by default
        connectedBlocks.put(rootBlockHeader.getHash(), rootBlockHeaderCached);
        blockChain.addVertex(rootBlockHeader.getHash());

        //ensure genesis block exists in database
        initializeGenesis();

        //cache flush process
        executor.scheduleAtFixedRate(this::flush, lastFlushTimeMaxIntervalMs, lastFlushTimeMaxIntervalMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void start() {
        init();
        loadFromDatabase();
    }

    @Override
    public synchronized void stop() {
        flush();
    }

    public synchronized HashMap<String, CachedBranch> getBranches(){
        return branches;
    }

    public CachedBranch getBranch(String branchId){
        return branches.get(branchId);
    }

    public synchronized int addToCache(List<BlockHeader> blockHeaders){
        return addToCache(blockHeaders, true);
    }

    private synchronized int addToCache(List<BlockHeader> blockHeaders, boolean persist){
        //Sort lists into those that have been added, and those which have not
        Set<BlockHeader> uniqueBlockHeaders = blockHeaders.stream().filter(b -> !connectedBlocks.containsKey(b.getHash()) && !unconnectedBlocks.containsKey(b.getHash())).collect(Collectors.toSet());

        //we want to process the unique headers
        Set<BlockHeader> headersToProcess = new HashSet<>(uniqueBlockHeaders);

        //record the cache sizes to identify how many entries have been added
        int connectedSize = connectedBlocks.size();
        int unconnectedSize = unconnectedBlocks.size();

        //reattempt connecting any existing unconnectedBlocks
        headersToProcess.addAll(unconnectedBlocks.values().stream().map(cb -> cb.getBlockHeader()).collect(Collectors.toList()));

        //add the new vertex's
        headersToProcess.forEach(this::addVertex);
        //connect the edges
        headersToProcess.forEach(this::addEdge);

        //process the graph, building the newly added nodes from the branch tips. Any blocks unconnected after the graph has been built are orphans
        buildGraph();

        //Persist the block headers we've just added
        if(persist) {
            uniqueBlockHeaders.forEach(this::persistBlockHeader);
        }

        //check if we should flush the cash
        if(blocksToPersist.size() >= cacheFlushThreshold){
            flush();
        }

        //calculate how many connected and unconnected blocks we've added
        int blocksConnected = connectedBlocks.size() - connectedSize;
        int blocksUnconnected = unconnectedBlocks.size() - unconnectedSize;

        //if we've connected blocks, log the quantity
        if(connectedBlocks.size() > connectedSize) {
            log.info("Added " + blocksConnected + " blockheaders to cache. Branch Heights:  " + branches.values().stream().map(b -> b.getHeight()).collect(Collectors.toList()));
        }

        //if we've added unconnected blocks, log the quantity
        if(unconnectedBlocks.size() > unconnectedSize){
            log.info("Added " + blocksUnconnected + " orphaned blockheaders to cache.");
        }

        return blocksConnected;
    }


    private boolean addVertex(BlockHeader blockHeader){
        try {
            //add the vertex to the graph, and the list of unconnected blocks for processing
            blockChain.addVertex(blockHeader.getHash());
            unconnectedBlocks.put(blockHeader.getHash(), CachedHeader.builder().work(calculateWork(blockHeader.getDifficultyTarget())).blockHeader(blockHeader).build());
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

    private void loadFromDatabase(){
        log.info("Loading BlockHeaders from database..");
        List<BlockHeader> blockHeaderList = blockHeaderRepository.findAll();

        log.info("BlockHeaders Loaded. Building Blockheader cache. Processing: " + blockHeaderList.size() + " entries...");
        addToCache(blockHeaderList, false);

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
        DepthFirstIterator<String, DefaultEdge> iterator = new DepthFirstIterator(blockChain, Util.ROOT_BLOCK_HEADER.getHash());

        //traverse the tree from the root, and connect and blocks which are unconnected
        while(iterator.hasNext()) {
            String blockHeaderHash = iterator.next();

            if(unconnectedBlocks.containsKey(blockHeaderHash)) {
                connectBlockToParent(unconnectedBlocks.get(blockHeaderHash));
            }
        }
    }

    private void connectBlockToParent(CachedHeader cachedBlockHeader){
        //get the header and calculate the work and height
        BlockHeader blockHeader = cachedBlockHeader.getBlockHeader();
        CachedHeader parentCachedBlockHeader = connectedBlocks.get(blockHeader.getPrevBlockHash());

        //get the work
        Double work = cachedBlockHeader.getWork();
        Double cumulativeWork = parentCachedBlockHeader.getCumulativeWork();

        //calculate the height given the parents height
        int height = parentCachedBlockHeader.getHeight() + 1; //height is 1 greater than parent

        //Get the branch we're currently on and it's confidence
        String branchId = parentCachedBlockHeader.getBranchId();
        Long branchConfidence = Math.min(branches.get(branchId).getConfidence(), blockHeader.getConfidence());

        //if the vertex's parent has more than 1 edge, there's a fork (new branch)
        if(blockChain.outgoingEdgesOf(blockHeader.getPrevBlockHash()).size() > 1) {
            branchId = generateBranchId(blockHeader.getHash());
        }

        //update the work for the existing branch
        branches.put(branchId, CachedBranch.builder()
                .id(branchId)
                .work(cumulativeWork + work)
                .parentBranchId(parentCachedBlockHeader.getBranchId())
                .leafNode(blockHeader.getHash())
                .height(height)
                .confidence(branchConfidence)
                .build());

        //cache the block header
        CachedHeader blockHeaderCached = CachedHeader.builder()
                .blockHeader(blockHeader)
                .work(work)
                .cumulativeWork(cumulativeWork + work)
                .branchId(branchId)
                .height(height)
                .build();

        //block is now connected
        connectedBlocks.put(blockHeader.getHash(), blockHeaderCached);
        unconnectedBlocks.remove(blockHeader.getHash());

        log.debug("Added blockheader: " + blockHeader.getHash() + " to cache at height: " + height);
    }

    public HashMap<String, CachedHeader> getUnconnectedBlocks() {
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

    private void initializeGenesis() {
        blockHeaderRepository.saveAndFlush(Util.GENESIS_BLOCK_HEADER);
    }

    private void persistBlockHeader(BlockHeader blockHeader) {
        blocksToPersist.put(blockHeader.getHash(), blockHeader);
    }

    public synchronized void flush(){
        if((blocksToPersist.size() > 0 &&
                (System.currentTimeMillis() - lastFlushTimeMinIntervalMs >= lastFlushTime))) {
            blockHeaderRepository.saveAll(new ArrayList<>(blocksToPersist.values()));

            blocksToPersist.clear();

            lastFlushTime = System.currentTimeMillis();
        }
    }

    public synchronized void purgeOrphanedBlocks(){
        List<BlockHeader> orphanBlockHashes = unconnectedBlocks.values().stream().map(bh -> bh.getBlockHeader()).peek(bh -> blockChain.removeVertex(bh.getHash())).collect(Collectors.toList());

        blockHeaderRepository.deleteAll(orphanBlockHashes);

        log.info("Purging " + orphanBlockHashes.size() + " blockheaders blocks from database and cache");

        unconnectedBlocks.clear();
    }

    public synchronized void purgeHeadersFromHash(String headerHash){
        //we don't want to delete the genesis or root block hash. Header also needs to exist.
        if(headerHash.equals(Util.GENESIS_BLOCK_HEADER.getHash()) || headerHash.equals(Util.ROOT_BLOCK_HEADER.getHash()) || !connectedBlocks.containsKey(headerHash)){
            return;
        }

        CachedHeader cachedHeader = connectedBlocks.get(headerHash);
        CachedHeader cachedHeaderParent = connectedBlocks.get(cachedHeader.getBlockHeader().getPrevBlockHash());

        //iterate from the given header to the tips of any connected branched belo
        DepthFirstIterator<String, DefaultEdge> iterator = new DepthFirstIterator(blockChain, headerHash);

        List<BlockHeader> blocksToPurge = new ArrayList<>();
        Set<String> branchesToPurge = new HashSet<>();

        while(iterator.hasNext()) {
            CachedHeader cachedHeaderToRemove = connectedBlocks.get(iterator.next());
            BlockHeader blockHeaderToRemove = cachedHeaderToRemove.getBlockHeader();

            //remove headers from the graph
            blockChain.removeVertex(blockHeaderToRemove.getHash());
            blockChain.removeEdge(blockHeaderToRemove.getPrevBlockHash(), blockHeaderToRemove.getHash());

            //remove headers from memory
            connectedBlocks.remove(blockHeaderToRemove.getHash());

            blocksToPurge.add(blockHeaderToRemove);
            branchesToPurge.add(cachedHeaderToRemove.getBranchId());

            //try remove the header from the persistence queue as it may not have been flushed yet
            blocksToPersist.remove(blockHeaderToRemove.getHash());
        }

        // if branch has nodes above it still. Update the leaf node and work then remove it from delete batch
        String generatedBranchId = generateBranchId(cachedHeader.getBlockHeader().getHash());
        if(!branches.containsKey(generatedBranchId)){
            branchesToPurge.remove(cachedHeader.getBranchId());

            CachedBranch headerBranch = branches.get(cachedHeader.getBranchId());
            headerBranch.setLeafNode(cachedHeader.getBlockHeader().getPrevBlockHash());
            headerBranch.setHeight(cachedHeader.getHeight() - 1);
            headerBranch.setWork(cachedHeaderParent.getCumulativeWork());
        }

        //remove branches from memory
        branchesToPurge.forEach(branches::remove);

        //purge headers from database
        if(blocksToPurge.size() > 0) {
            blockHeaderRepository.deleteAll(blocksToPurge);

            log.info("Purging " + blocksToPurge.size() + " blockheaders from database and cache");
        }

    }



}
