package io.bitcoinsv.headerSV.service.sync;


import io.bitcoinsv.bitcoinjsv.bitcoin.api.extended.ChainInfo;
import io.bitcoinsv.jcl.net.network.PeerAddress;
import io.bitcoinsv.jcl.net.network.events.P2PEvent;
import io.bitcoinsv.jcl.net.protocol.events.control.PeerHandshakedEvent;
import io.bitcoinsv.jcl.net.protocol.messages.*;
import io.bitcoinsv.jcl.net.protocol.messages.common.BitcoinMsg;
import io.bitcoinsv.jcl.store.blockChainStore.BlockChainStore;
import io.bitcoinsv.bitcoinjsv.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinsv.bitcoinjsv.bitcoin.bean.base.HeaderBean;
import io.bitcoinsv.bitcoinjsv.bitcoin.bean.extended.LiteBlockBean;
import io.bitcoinsv.bitcoinjsv.core.Sha256Hash;
import io.bitcoinsv.headerSV.config.NetworkConfiguration;
import io.bitcoinsv.headerSV.service.HeaderSvService;
import io.bitcoinsv.headerSV.service.consumer.EventConsumer;
import io.bitcoinsv.headerSV.service.consumer.MessageConsumer;
import io.bitcoinsv.headerSV.service.network.NetworkService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
@Slf4j
@ConfigurationProperties(prefix = "headersv.sync")
public class BlockHeaderSyncService implements HeaderSvService, MessageConsumer, EventConsumer {

    private final NetworkService networkService;
    private final NetworkConfiguration networkConfiguration;
    private final BlockChainStore blockStore;

    @Setter
    private Set<String> headersToIgnore = Collections.emptySet();


    protected BlockHeaderSyncService(NetworkService networkService,
                                     NetworkConfiguration networkConfiguration,
                                     BlockChainStore blockStore) {
        this.networkService = networkService;
        this.networkConfiguration = networkConfiguration;
        this.blockStore = blockStore;
    }

    @Override
    public void start() {
        log.info("Starting blockstore..");
        blockStore.start();
        log.info("Blockstore started");

        log.info("Current blockchain state: ");
        blockStore.getTipsChains().forEach(t -> log.info("Chain Id: " + blockStore.getBlockChainInfo(t).get().getHeader().getHash() + " Height: " + blockStore.getBlockChainInfo(t).get().getHeight()));


        log.info("Listening for headers...");

        networkService.subscribe(HeadersMsg.class, this::consume, true, false);
        networkService.subscribe(InvMessage.class, this::consume, false, true);
        networkService.subscribe(PeerHandshakedEvent.class, this::consume);
    }

    @Override
    public void stop() {}

    @Override
    public void consume(BitcoinMsg message, PeerAddress peerAddress) {
        log.debug("Consuming message type: " + message.getHeader().getCommand());

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
        log.debug("Consuming event type: " + event.toString());

        if(event instanceof PeerHandshakedEvent){
            initiatePeerHandshake(((PeerHandshakedEvent) event).getPeerAddress());
        }
    }

    private void initiatePeerHandshake(PeerAddress peerAddress) {
        // request headers for each tip, at this point we don't know which nodes are SV and which are not
        blockStore.getTipsChains().forEach(h -> {
            //Let this peer know where we've sync'd up too
            updatePeerWithLatestHeader(h, peerAddress);

            // Ask peer to keep up this node updated of latest headers
            requestPeerToSendNewHeaders(peerAddress);

            //Request any headers the peer has from our latest tip
            requestHeadersFromHash(h, peerAddress);
        });
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
            HeaderReadOnly header = BlockHeaderMsgToBean(blockHeaderMsg);

            //Reject the whole message if the peer is sending bad blocks
            if (!validBlockHeader(header, peerAddress)){
                return;
            }

            //add to list to be processed
            blockHeaders.add(header);
        }

        //We only want to request headers for tips that have changed
        List<Sha256Hash> branchTips = blockStore.getTipsChains();

        blockStore.saveBlocks(blockHeaders);

        //if we receive any orphaned blocks, ask this node
        List<Sha256Hash> orphansToRemove = new ArrayList<>();

        blockStore.getOrphanBlocks().forEach(o ->{
            HeaderReadOnly orphanBlockHeader = blockStore.getBlock(o).get();
            requestParentHeadersForOrphanHash(orphanBlockHeader.getHash(), peerAddress);

            orphansToRemove.add(o);
        });

        //remove the orphans as we've made a best effort to retreive them
        orphansToRemove.forEach(blockStore::removeBlock);

        //check which tips have changed
        List<Sha256Hash> updatedtips = blockStore.getTipsChains().stream().filter(h -> !branchTips.contains(h)).collect(Collectors.toList());

        //Update the peers with the latest tips
        updatedtips.stream().forEach(this::updatePeersWithLatestHeader);

        //For the tips that have changed,
        updatedtips.stream().forEach(this::requestHeadersFromHash);

    }

    private HeaderReadOnly BlockHeaderMsgToBean(BlockHeaderMsg headersMsg){
        HeaderBean headerBean = new HeaderBean(new LiteBlockBean());
        headerBean.setTime(headersMsg.getCreationTimestamp());
        headerBean.setDifficultyTarget(headersMsg.getDifficultyTarget());
        headerBean.setNonce(headersMsg.getNonce());
        headerBean.setPrevBlockHash(Sha256Hash.wrapReversed(headersMsg.getPrevBlockHash().getHashBytes()));
        headerBean.setVersion(headersMsg.getVersion());
        headerBean.setMerkleRoot(Sha256Hash.wrapReversed(headersMsg.getMerkleRoot().getHashBytes()));
        headerBean.setHash(Sha256Hash.wrapReversed(headersMsg.getHash().getHashBytes()));

        return headerBean;
    }

    private boolean validBlockHeader(HeaderReadOnly header, PeerAddress peerAddress){
        //Reject the whole message if any of them are in the ignore list
        if(headersToIgnore.contains(header.getHash().toString())){
            log.info("Message containing header: " + header.getHash().toString() + " has been rejected due to being in the ignore list");
            networkService.blacklistPeer(peerAddress);
            return false;
        }

        /* We don't want to process this message, even it has some headers. Oherwise the different threads may request a branch that has already been processed, slowing down sync times.
           This also catches duplicate messages, so there's no need to store the message checksum and compare each message*/
        if (blockStore.getTipsChains().contains(header.getHash())) {
            log.debug("Message containing header: " + header.getHash().toString() + " has been rejected due to it containing processed headers");
            return false;
        }

        return true;
    }

    private void requestHeader(Sha256Hash hash, PeerAddress peerAddress){
        log.info("Requesting headers for block: " + hash );
        networkService.send(buildGetHeaderMsg(hash), peerAddress,true);
    }

    private void requestHeadersFromHash(Sha256Hash hash){
        log.info("Requesting headers from block: " + hash + " at height: " + blockStore.getBlockChainInfo(hash).get().getHeight());
        networkService.broadcast(buildGetHeaderMsgFromHash(hash), true);
    }

    private void requestHeadersFromHash(Sha256Hash hash, PeerAddress peerAddress){
        log.info("Requesting headers from block: " + hash + " at height: " + blockStore.getBlockChainInfo(hash).get().getHeight() + " from peer: " + peerAddress);
        networkService.send(buildGetHeaderMsgFromHash(hash), peerAddress, false);
    }

    private void requestParentHeadersForOrphanHash(Sha256Hash hash, PeerAddress peerAddress){
        log.info("Requesting parent headers for block: " + hash);
        networkService.send(buildGetHeadersForOrphanAncestors(hash), peerAddress, false);
    }

    private void requestPeerToSendNewHeaders(PeerAddress peerAddress){
        log.info("Requesting peer: " + peerAddress + " to inform client of any new headers");
        networkService.send(buildSendHeadersMsg(), peerAddress, false);
    }

    private void updatePeerWithLatestHeader(Sha256Hash hash, PeerAddress peerAddress){
        log.info("Advertising to peer: " + peerAddress + " that chain tip is: " + hash);
        networkService.send(buildBlockInventoryMsg(hash), peerAddress, false);
    }

    private void updatePeersWithLatestHeader(Sha256Hash hash){
        log.info("Advertising to all peers that chain tip is: " + hash);
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

        ChainInfo longestChainInfo = blockStore.getLongestChain().get();

        //Always included the tip
        blockLocatorHashes.add(longestChainInfo.getHeader().getHash());

        //ancestor locators should be something like 10, 20, 40.. 640..
        for(int i = 1; Math.exp(i) < longestChainInfo.getHeight(); i++){
            Sha256Hash ancestorHash = blockStore.getAncestorByHeight(longestChainInfo.getHeader().getHash(), longestChainInfo.getHeight() - (int) Math.exp(i)).get().getHeader().getHash();
            blockLocatorHashes.add(ancestorHash);
        }

        //Always included genesis
        blockLocatorHashes.add(networkConfiguration.getGenesisBlock().getHash());


        return buildGetHeadersMsg(blockLocatorHashes, orphanHash);
    }


    private GetHeadersMsg buildGetHeadersMsg(List<Sha256Hash> locatorHashes, Sha256Hash stopHash){
        List<HashMsg> blockLocatorHashMsgs = Collections.emptyList();

        //locator hash can be null if stop hash is defined, this will be treated as a call for that particular header
        if(locatorHashes != null) {
            blockLocatorHashMsgs = locatorHashes.stream().map(h -> HashMsg.builder().hash(h.getReversedBytes()).build()).collect(Collectors.toList());
        }

        BaseGetDataAndHeaderMsg baseGetDataAndHeaderMsg = BaseGetDataAndHeaderMsg.builder()
                .version(networkConfiguration.getProtocolConfig().getBasicConfig().getProtocolVersion())
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


}