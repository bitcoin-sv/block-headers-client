package com.nchain.headerSV.service.sync;


import com.nchain.headerSV.service.HeaderSvService;
import com.nchain.headerSV.service.consumer.MessageConsumer;
import com.nchain.headerSV.service.network.NetworkService;
import com.nchain.jcl.base.domain.api.base.BlockHeader;
import com.nchain.jcl.base.tools.bytes.HEX;
import com.nchain.jcl.base.tools.crypto.Sha256Wrapper;
import com.nchain.jcl.net.network.PeerAddress;
import com.nchain.jcl.net.protocol.config.ProtocolConfig;
import com.nchain.jcl.net.protocol.messages.*;
import com.nchain.jcl.net.protocol.messages.common.BitcoinMsg;
import com.nchain.jcl.net.protocol.messages.common.Message;
import com.nchain.jcl.store.blockChainStore.BlockChainStore;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;

/**
 * @author {m.fletcher}@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 27/07/2020
 */
@Service
@Slf4j
@ConfigurationProperties(prefix = "headersv.sync")
public class BlockHeaderSyncService implements HeaderSvService, MessageConsumer {

    private final NetworkService networkService;
    private final ProtocolConfig protocolConfig;
    private final BlockChainStore blockStore;
    private boolean serviceStarted = false;

    @Setter
    private Set<String> headersToIgnore = Collections.emptySet();

    protected BlockHeaderSyncService(NetworkService networkService,
                                     ProtocolConfig protocolConfig,
                                     BlockChainStore blockStore) {
        this.networkService = networkService;
        this.protocolConfig = protocolConfig;
        this.blockStore = blockStore;
    }

    @Override
    public synchronized void start() {
        log.info("Listening for incoming Headers...");
        networkService.subscribe(HeadersMsg.class, this::consume);
        networkService.subscribe(InvMessage.class, this::consume);
        networkService.subscribe(VersionAckMsg.class, this::consume);

        log.debug("Starting blockstore..");
        blockStore.start();
        log.debug("Blockstore started");

        serviceStarted = true;
    }

    @Override
    public synchronized void stop() {}

    @Override
    public synchronized <T extends Message> void consume(BitcoinMsg<T> message, PeerAddress peerAddress) {
        log.debug("Consuming message type: " + message.getHeader().getCommand());

        switch(message.getHeader().getCommand()){
            case HeadersMsg.MESSAGE_TYPE:
                consumeHeadersMsg((HeadersMsg) message.getBody());
                break;

            case InvMessage.MESSAGE_TYPE:
                consumeInvMsg((InvMessage) message.getBody(), peerAddress);
                break;

            case VersionAckMsg.MESSAGE_TYPE:
                consumeVersionAckMsg(peerAddress);
                break;

            default:
                throw new UnsupportedOperationException("Unhandled Message Type: " + message.getBody().getMessageType());
        }
    }

    private void consumeVersionAckMsg(PeerAddress peerAddress) {
        // 'sendheaders' will trigger the peer to send any new headers it becomes aware of
        requestSendHeaders(peerAddress);    //TODO test if SendHeaders is working

        // request the next lot of headers
        blockStore.getTipsChains().forEach(h -> requestHeadersFromHash(h, peerAddress));
    }

    private void consumeInvMsg(InvMessage invMsg, PeerAddress peerAddress){
        List<InventoryVectorMsg> blockHeaderMessages = invMsg.getInvVectorList().stream().filter(iv -> iv.getType() == InventoryVectorMsg.VectorType.MSG_BLOCK).collect(Collectors.toList());

        //TODO check this works
        // request the next lot of headers from this peer if a blockheader inv message has been sent
        if(!blockHeaderMessages.isEmpty()) {
            blockStore.getTipsChains().forEach(h -> requestHeadersFromHash(h, peerAddress));
        }
    }

    /* Although blockStore is synchronized, we need to ensure another thread does not access
       simultaneously else we risk requesting multiple headers for already processed blocks. */
    private void consumeHeadersMsg(HeadersMsg headerMsg){
        //Convert each BlockHeaderMsg to a BlockHeader
        List<BlockHeader> blockHeaders = new ArrayList<>(headerMsg.getBlockHeaderMsgList().size());
        for(BlockHeaderMsg blockHeaderMsg : headerMsg.getBlockHeaderMsgList()){

            //Reject the whole message if any of them are in the ignore list
            if(headersToIgnore.contains(blockHeaderMsg.getHash().toString())){
                log.info("Message containing header: " + blockHeaderMsg.getHash() + " has been rejected due to being in the ignore list");
                return;
            }

            /* We don't want to process this message, even it has some headers. Otherwise the different threads may request a branch that has already been processed, slowing down sync times.
               This also catches duplicate messages, so there's no need to store the message checksum and compare each message*/
            if (blockStore.getTipsChains().contains(Sha256Wrapper.wrap(blockHeaderMsg.getHash().getHashBytes()))) {
                log.debug("Message containing header: " + blockHeaderMsg.getHash() + " has been rejected due to it containing processed headers");
                return;
            }

            //Convert a BlockHeaderMsg to a BlockHeader
            blockHeaders.add(BlockHeader.builder()
                    .version(blockHeaderMsg.getVersion())
                    .hash(Sha256Wrapper.wrap(blockHeaderMsg.getHash().getHashBytes()))
                    .prevBlockHash(Sha256Wrapper.wrap(blockHeaderMsg.getPrevBlockHash().getHashBytes()))
                    .merkleRoot(Sha256Wrapper.wrap(blockHeaderMsg.getMerkleRoot().getHashBytes()))
                    .time(blockHeaderMsg.getCreationTimestamp())
                    .difficultyTarget(blockHeaderMsg.getDifficultyTarget())
                    .nonce(blockHeaderMsg.getNonce())
                    .numTxs(blockHeaderMsg.getTransactionCount().getValue())
                    .build());
        }

        //We only want to request headers for tips that have changed
        List<Sha256Wrapper> branchTips = blockStore.getTipsChains();

        blockStore.saveBlocks(blockHeaders);

        //check which tips have changed
        List<Sha256Wrapper> updatedtips = blockStore.getTipsChains().stream().filter(h -> !branchTips.contains(h)).collect(Collectors.toList());

        //Broadcast to all peers, as duplicate messages are thrown away
        //TODO we might not want to throw headers away for confidence
        //TODO maybe only process messages if connected to minimum number of peers
        updatedtips.stream().forEach(this::requestHeadersFromHash);
    }


    private void requestHeadersFromHash(Sha256Wrapper hash){
        log.info("Requesting headers from block: " + hash + " at height: " + blockStore.getBlockChainInfo(hash).get().getHeight());
        networkService.broadcast(buildGetHeaderMsgFromHash(hash.toString()));
    }

    private void requestHeadersFromHash(Sha256Wrapper hash, PeerAddress peerAddress){
        log.debug("Requesting headers from block: " + hash + " at height: " + blockStore.getBlockChainInfo(hash).get().getHeight() + " from peer: " + peerAddress);
        networkService.send(buildGetHeaderMsgFromHash(hash.toString()), peerAddress);
    }

    private void requestSendHeaders(PeerAddress peerAddress){
        log.debug("Requesting peer : " + peerAddress + " to notify of new headers");
        networkService.send(buildSendHeadersMsg(), peerAddress);
    }
    private SendHeadersMsg buildSendHeadersMsg(){
        return SendHeadersMsg.builder().build();
    }

    private GetHeadersMsg buildGetHeaderMsgFromHash(String hash){
        HashMsg hashMsg = HashMsg.builder().hash(HEX.decode(hash)).build();
        List<HashMsg> hashMsgs = Arrays.asList(hashMsg);

        BaseGetDataAndHeaderMsg baseGetDataAndHeaderMsg = BaseGetDataAndHeaderMsg.builder()
                .version(protocolConfig.getBasicConfig().getProtocolVersion())
                .blockLocatorHash(hashMsgs)
                .hashCount(VarIntMsg.builder().value(1).build())
                .hashStop(HashMsg.builder().hash(Sha256Wrapper.ZERO_HASH.getBytes()).build())
                .build();

        GetHeadersMsg getHeadersMsg = GetHeadersMsg.builder()
                .baseGetDataAndHeaderMsg(baseGetDataAndHeaderMsg)
                .build();

        return getHeadersMsg;
    }

}
