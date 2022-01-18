package io.bitcoinsv.headerSV.core.service;

import io.bitcoinsv.bitcoinjsv.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinsv.bitcoinjsv.bitcoin.api.extended.ChainInfo;
import io.bitcoinsv.bitcoinjsv.bitcoin.bean.base.HeaderBean;
import io.bitcoinsv.bitcoinjsv.bitcoin.bean.extended.LiteBlockBean;
import io.bitcoinsv.bitcoinjsv.core.Sha256Hash;
import io.bitcoinsv.headerSV.core.HeaderSvService;
import io.bitcoinsv.headerSV.core.api.HeaderSvApi;
import io.bitcoinsv.headerSV.core.common.EventConsumer;
import io.bitcoinsv.headerSV.core.common.MessageConsumer;
import io.bitcoinsv.headerSV.core.config.HeaderSvConfig;
import io.bitcoinsv.headerSV.core.events.ChainSynchronizedEvent;
import io.bitcoinsv.headerSV.core.events.HeaderSvEventStreamer;
import io.bitcoinsv.headerSV.core.events.TipsUpdatedEvent;
import io.bitcoinsv.headerSV.core.service.network.NetworkService;
import io.bitcoinsv.headerSV.core.service.storage.StorageService;
import io.bitcoinsv.jcl.net.network.PeerAddress;
import io.bitcoinsv.jcl.net.network.events.P2PEvent;
import io.bitcoinsv.jcl.net.protocol.config.ProtocolConfig;
import io.bitcoinsv.jcl.net.protocol.config.ProtocolConfigBuilder;
import io.bitcoinsv.jcl.net.protocol.events.control.PeerHandshakedEvent;
import io.bitcoinsv.jcl.net.protocol.messages.*;
import io.bitcoinsv.jcl.net.protocol.messages.common.BitcoinMsg;
import io.bitcoinsv.jcl.tools.events.EventBus;
import io.bitcoinsv.jcl.tools.thread.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 * @author i.fernandez@nchain.com
 *
 * Implements the HeaderSV Service. t relies on a NetworkService and a StorageService to do the job.
 */
public class HeadersSvServiceImpl implements HeaderSvService, MessageConsumer, EventConsumer {

    private final Logger log = LoggerFactory.getLogger(HeadersSvServiceImpl.class);

    // Basic Config and Services needed:
    private final HeaderSvConfig config;
    private final NetworkService networkService;
    private final StorageService store;

    // Implementation of API and Event Streamer:
    private final HeaderSvApi api;
    private final EventBus eventBus;
    private final HeaderSvEventStreamer eventStreamer;

    // Values pre-calculated and stored here for convenience:
    private final HeaderReadOnly genesisBlock;
    private final long protocolVersion;

    // We trigger and Thread to to check WHEN we can trigger the CHAIN_SYNCHRONIZED Event.
    // A CHAIN_SYNCHRONIZED event is triggered when we haven't received any Headers for some time. But we need to
    // consider 2 different scenarios:
    // - WE already asked Peers and received some Headers, but now they have stopped.
    // - We already asked Peers but they never replied ( this might happen in a new run, when we are already synced)

    private final ExecutorService executor;
    private AtomicReference<Instant> lastTipsUpdate = new AtomicReference<>();
    private AtomicBoolean requestForHeadersSent = new AtomicBoolean();
    private AtomicBoolean chainSyncEventTriggered = new AtomicBoolean();


    /** Constructor */
    public HeadersSvServiceImpl(HeaderSvConfig config, NetworkService networkService, StorageService store) {
        this.config = config;
        this.networkService = networkService;
        this.store = store;
        this.api = new HeaderSvApi(store, networkService);

        // EVENT BUS CONFIG........
        this.eventBus = EventBus.builder()
                .executor(ThreadUtils.getSingleThreadExecutorService("headerSV-eventStreamer"))
                .build();
        this.eventStreamer = new HeaderSvEventStreamer(this.eventBus);

        ProtocolConfig protocolConfig = ProtocolConfigBuilder.get(networkService.getNet().params());

        // For convenience, we obtain some fields. the genesisBlock is obtained from the BNetworkService instead of
        // from the protocolConfig, because sometimes (in testing) the NetworkService will be connected to a specific
        // Network, but it will be using a hardcoded genesis block

        genesisBlock = networkService.getGenesisBlock();                        // from the network Service
        protocolVersion = protocolConfig.getBasicConfig().getProtocolVersion(); // from the protocol Config

        this.executor = ThreadUtils.getSingleThreadExecutorService("headerSV-service");

        // We subscribe to the events from the Network Service:
        networkService.subscribe(HeadersMsg.class, this::consume, true, false);
        networkService.subscribe(InvMessage.class, this::consume, false, true);
        networkService.subscribe(PeerHandshakedEvent.class, this::consume);
    }

    @Override
    public void start() {
        log.info("Starting blockstore...");
        store.start();
        log.info("Blockstore started.");

        log.info("Starting Network Service...");
        networkService.start();
        log.info("Network Service started.");

        log.info("Current blockchain state: ");
        store.getTipsChains().forEach(t -> log.info("Chain Id: " + store.getBlockChainInfo(t).get().getHeader().getHash() + " Height: " + store.getBlockChainInfo(t).get().getHeight()));

        // We start the monitor to check if the CHAIN_SYNCHRONIZED Event shoul dbe triggered...
        this.executor.execute(this::monitor);

        log.info("Listening for headers...");
    }

    @Override
    public void stop() {
        log.info("Stopping blockstore...");
        store.stop();
        log.info("Blockstore stopped.");

        log.info("Stopping Network Service...");
        networkService.stop();
        log.info("NetworkService stopped.");

        this.executor.shutdownNow();
    }

    @Override
    public void consume(BitcoinMsg message, PeerAddress peerAddress) {
        log.trace("Consuming message type: " + message.getHeader().getCommand());

        switch(message.getHeader().getCommand()){
            case HeadersMsg.MESSAGE_TYPE:
                consumeHeadersMsg((HeadersMsg) message.getBody(), peerAddress);
                break;

            case InvMessage.MESSAGE_TYPE:
                consumeInvMsg((InvMessage) message.getBody(), peerAddress);
                break;

            default:
                throw new UnsupportedOperationException("Unhandled Message Type: " + message.getBody().getMessageType());
        }
    }


    @Override
    public void consume(P2PEvent event) {
        log.trace("Consuming event type: " + event.toString());

        if(event instanceof PeerHandshakedEvent){
            initiatePeerHandshake(((PeerHandshakedEvent) event).getPeerAddress());
        }
    }

    @Override
    public HeaderSvApi API() {
        return this.api;
    }

    @Override
    public HeaderSvEventStreamer EVENTS() {
        return this.eventStreamer;
    }

    private void initiatePeerHandshake(PeerAddress peerAddress) {
        // request headers for each tip, at this point we don't know which nodes are SV and which are not
        store.getTipsChains().forEach(h -> {
            //Let this peer know where we've sync'd up too
            if (config.isInvBroadcastEnabled()) {
                updatePeerWithLatestHeader(h, peerAddress);
            }

            //Request any headers the peer has from our latest tip
            requestHeadersFromHash(h, peerAddress);
        });

        // Ask peer to keep up this node updated of latest headers (if enabled)
        if (config.isSendHeadersEnabled()) {
            requestPeerToSendNewHeaders(peerAddress);
        }
    }

    /*
     * Most of the time, new header updates will be sent via a 'headers' msg in response to 'sendheaders'. But there's some scenarios, such as generating a large volume of blocks via bitcoin-cli,
     * or by some astronomical odds a large amount of blocks were generated in quick succession, our peer could fall behind and no longer qualify to receive 'headers' and so 'inv' messages will be sent instead. Consuming those Inv messages
     * and requesting the headers for them will enable our peer to "catch up".
     */
    private void consumeInvMsg(InvMessage invMsg, PeerAddress peerAddress){
        List<Sha256Hash> blockHeaderMessages = invMsg.getInvVectorList().stream().filter(iv -> iv.getType() == InventoryVectorMsg.VectorType.MSG_BLOCK).map(inv -> Sha256Hash.wrapReversed(inv.getHashMsg().getHashBytes())).collect(Collectors.toList());

        blockHeaderMessages.forEach(h -> requestHeader(h, peerAddress));
    }

    /* Although blockStore is synchronized, we need to ensure another thread does not access
       simultaneously else we risk requesting multiple headers for already processed blocks. */
    private synchronized void consumeHeadersMsg(HeadersMsg headerMsg, PeerAddress peerAddress){

        //Convert each BlockHeaderMsg to a BlockHeader
        List<HeaderReadOnly> blockHeaders = new ArrayList<>(headerMsg.getBlockHeaderMsgList().size());
        for(BlockHeaderMsg blockHeaderMsg : headerMsg.getBlockHeaderMsgList()){

            //convert the header message to a header
            HeaderReadOnly header = blockHeaderMsg.toBean();

            //Reject the whole message if the peer is sending bad blocks
            if (!validBlockHeader(header, peerAddress)){
                return;
            }

            //add to list to be processed
            blockHeaders.add(header);
        }

        //We only want to request headers for tips that have changed
        List<Sha256Hash> branchTips = store.getTipsChains();

        // WE save the headers and keep a reference to the ones really saved, these are the new ones
        List<HeaderReadOnly> headersSaved = store.saveBlocks(blockHeaders);

        //if we receive any orphaned blocks, ask this node
        List<Sha256Hash> orphansToRemove = new ArrayList<>();

        store.getOrphanBlocks().forEach(o ->{
            HeaderReadOnly orphanBlockHeader = store.getBlock(o).get();
            requestParentHeadersForOrphanHash(orphanBlockHeader.getHash(), peerAddress);

            orphansToRemove.add(o);
        });

        //remove the orphans as we've made a best effort to retreive them
        orphansToRemove.forEach(store::removeBlock);

        //check which tips have changed
        List<Sha256Hash> updatedtips = store.getTipsChains().stream().filter(h -> !branchTips.contains(h)).collect(Collectors.toList());

        // If the tips have changed, we keep processing...
        if (!updatedtips.isEmpty()) {
            // Update the peers with the latest tips (ONLY if Enabled)
            if (config.isInvBroadcastEnabled()) {
                updatedtips.stream().forEach(this::updatePeersWithLatestHeader);
            }

            // For the tips that have changed,
            updatedtips.stream().forEach(this::requestHeadersFromHash);

            // We trigger an Event containing the updated tips and the Headers saved:
            TipsUpdatedEvent event = TipsUpdatedEvent.builder()
                    .updatedTips(updatedtips)
                    .newHeaders(headersSaved).build();
            this.eventBus.publish(event);

            // We register this Update:
            this.lastTipsUpdate.set(Instant.now());
            this.chainSyncEventTriggered.set(false);
        }

    }

    private boolean validBlockHeader(HeaderReadOnly header, PeerAddress peerAddress){
        //Reject the whole message if any of them are in the ignore list
        if(config.getHeadersToIgnore().contains(header.getHash().toString())){
            log.debug("Message containing header: " + header.getHash().toString() + " has been rejected due to being in the ignore list");
            networkService.blacklistPeer(peerAddress);
            return false;
        }

        /* We don't want to process this message, even it has some headers. Otherwise the different threads may request a branch that has already been processed, slowing down sync times.
           This also catches duplicate messages, so there's no need to store the message checksum and compare each message.

           So this is basically an "overlap validation", so we don't process messages that contain Headers already processed
         */
        if (store.getTipsChains().contains(header.getHash())) {
            log.debug("Message containing header: " + header.getHash().toString() + " has been rejected due to it containing processed headers");
            return false;
        }

        return true;
    }

    private void requestHeader(Sha256Hash hash, PeerAddress peerAddress){
        log.debug("Requesting headers for block: " + hash );
        boolean requestSent = networkService.send(buildGetHeaderMsg(hash), peerAddress,true);
        if (requestSent) this.requestForHeadersSent.set(true);
    }

    private void requestHeadersFromHash(Sha256Hash hash){
        log.info("Requesting headers from block: " + hash + " at height: " + store.getBlockChainInfo(hash).get().getHeight());
        boolean requestSent = networkService.broadcast(buildGetHeaderMsgFromHash(hash), true);
        if (requestSent) this.requestForHeadersSent.set(true);
    }

    private void requestHeadersFromHash(Sha256Hash hash, PeerAddress peerAddress){
        log.debug("Requesting headers from block: " + hash + " at height: " + store.getBlockChainInfo(hash).get().getHeight() + " from peer: " + peerAddress);
        boolean requestSent = networkService.send(buildGetHeaderMsgFromHash(hash), peerAddress, false);
        if (requestSent) this.requestForHeadersSent.set(true);
    }

    private void requestParentHeadersForOrphanHash(Sha256Hash hash, PeerAddress peerAddress){
        log.debug("Requesting parent headers for block: " + hash);
        networkService.send(buildGetHeadersForOrphanAncestors(hash), peerAddress, false);
    }

    private void requestPeerToSendNewHeaders(PeerAddress peerAddress){
        log.debug("Requesting peer: " + peerAddress + " to inform client of any new headers");
        networkService.send(buildSendHeadersMsg(), peerAddress, false);
    }

    private void updatePeerWithLatestHeader(Sha256Hash hash, PeerAddress peerAddress){
        log.debug("Advertising to peer: " + peerAddress + " that chain tip is: " + hash);
        networkService.send(buildBlockInventoryMsg(hash), peerAddress, false);
    }

    private void updatePeersWithLatestHeader(Sha256Hash hash){
        log.debug("Advertising to all peers that chain tip is: " + hash);
        networkService.broadcast(buildBlockInventoryMsg(hash), false);
    }

    private SendHeadersMsg buildSendHeadersMsg(){
        SendHeadersMsg sendHeadersMsg = SendHeadersMsg.builder().build();

        return sendHeadersMsg;
    }

    private InvMessage buildBlockInventoryMsg(Sha256Hash hash) {
        HashMsg hashMsg = HashMsg.builder().hash(hash.getReversedBytes()).build();

        InventoryVectorMsg inventoryVectorMsg = InventoryVectorMsg.builder().type(InventoryVectorMsg.VectorType.MSG_BLOCK).hashMsg(hashMsg).build();
        InvMessage invMessage = InvMessage.builder().invVectorMsgList(Arrays.asList(inventoryVectorMsg)).build();

        return invMessage;
    }

    private GetHeadersMsg buildGetHeadersForOrphanAncestors(Sha256Hash orphanHash){
        List<Sha256Hash> blockLocatorHashes = new ArrayList<>();

        // NOTE:
        // When building the GET_HEADERS message, we can use several Hash Locators. In a general scenario, we can add
        // our tips, and then some other locators referencing Block hashes at different heights, like [tip-50],
        // [tip-100], etc
        // But in this particular case, we can only use our TIPS as Locators. If we use any other "previous" block hash
        // as a locator, the next HEADERS message that the Node sends as a response will be REJECTED due to the
        // "overlap-validation" we do in the "validBlockHeader" method.
        // Example:
        // - Our tips are #120 and #45 (main branch and fork)
        // - If we use #120, #45, and also #100, #80, #20 as locator, the Node will pick the first one that it also has
        //   in its chain. It might be any of them, for example:
        //      - If the Node chooses #45 (our fork tip), it will send in the HEADERS #46, #47, etc.. This will WORK
        //      - If the Node chooses #120 (main tip), it will send #121, #122, etc. This will WORK.
        //      - If the Node chooses #80, ti will send #81, #82,... all the way and also including #120, our Tip of the
        //        main branch. SINCE ANY HEADERS CONTAINING ONE OF OUR TIPS IS REJECTED THIS MESSAGE WIL BE REJECTED

        // So bottom line: We can also use our TIPs as Hash locators in this Message

        List<Sha256Hash> tips = store.getTipsChains();
        blockLocatorHashes.addAll(tips);
        return buildGetHeadersMsg(blockLocatorHashes, orphanHash);
    }


    private GetHeadersMsg buildGetHeadersMsg(List<Sha256Hash> locatorHashes, Sha256Hash stopHash){
        List<HashMsg> blockLocatorHashMsgs = Collections.emptyList();

        //locator hash can be null if stop hash is defined, this will be treated as a call for that particular header
        if(locatorHashes != null) {
            blockLocatorHashMsgs = locatorHashes.stream().map(h -> HashMsg.builder().hash(h.getReversedBytes()).build()).collect(Collectors.toList());
        }

        BaseGetDataAndHeaderMsg baseGetDataAndHeaderMsg = BaseGetDataAndHeaderMsg.builder()
                .version(this.protocolVersion)
                .blockLocatorHash(blockLocatorHashMsgs)
                .hashCount(VarIntMsg.builder().value(blockLocatorHashMsgs.size()).build())
                .hashStop(HashMsg.builder().hash(stopHash.getReversedBytes()).build())
                .build();

        GetHeadersMsg getHeadersMsg = GetHeadersMsg.builder()
                .baseGetDataAndHeaderMsg(baseGetDataAndHeaderMsg)
                .build();

        return getHeadersMsg;
    }

    private GetHeadersMsg buildGetHeaderMsg(Sha256Hash hash){
        return buildGetHeadersMsg(Collections.emptyList(), hash);
    }

    private GetHeadersMsg buildGetHeaderMsgFromHash(Sha256Hash hash){
        return buildGetHeadersMsg(Arrays.asList(hash), Sha256Hash.ZERO_HASH);
    }

    /**
     * This method runs in an infinite loop, checking if we have reached the tip of the chain, and in that case it
     * triggers a ChainSynchronizedEvent. We consider that we reached the Tip of the chain when the Tips in our storage
     * have NOT been updated for some time (defined in the configuration).
     */
    private void monitor() {
        try {
            while (true) {
                if ((!this.chainSyncEventTriggered.get())
                        && (requestForHeadersSent.get())
                        && (lastTipsUpdate.get() != null)
                        && (Duration.between(this.lastTipsUpdate.get(), Instant.now()).compareTo(this.config.getTimeoutToTriggerSyncComplete()) > 0)
                ) {
                    List<ChainInfo> tipsChainInfo = store.getTipsChains().stream().map(h -> store.getBlockChainInfo(h).get()).collect(Collectors.toList());
                    ChainSynchronizedEvent event = ChainSynchronizedEvent.builder()
                            .tips(tipsChainInfo)
                            .build();
                    this.eventBus.publish(event);
                    this.chainSyncEventTriggered.set(true);
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            // Nothing, most probably the system shutting down...
        }
    }

}
