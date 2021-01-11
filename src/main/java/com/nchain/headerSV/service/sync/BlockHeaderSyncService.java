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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
    private ScheduledExecutorService executor;

    //TODO logging

    private Set<Long> processedMessages = Collections.synchronizedSet(new HashSet<>());

    private BlockChainStore blockStore;

    @Setter
    private int requestHeadersRefreshIntervalMs;

    private static int REQUEST_HEADER_SCHEDULE_TIME_INITIAL_DELAY_MS = 5000;

    protected BlockHeaderSyncService(NetworkService networkService,
                                     ProtocolConfig protocolConfig,
                                     BlockChainStore blockStore) {
        this.networkService = networkService;
        this.protocolConfig = protocolConfig;
        this.blockStore = blockStore;

        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public synchronized void start() {
        log.info("Listening for incoming Headers...");
        networkService.subscribe(HeadersMsg.class, this::consume);
        networkService.subscribe(InvMessage.class, this::consume);

        blockStore.start();

        /* periodically check for new incoming headers */
        executor.scheduleAtFixedRate(this::requestHeaders, REQUEST_HEADER_SCHEDULE_TIME_INITIAL_DELAY_MS, requestHeadersRefreshIntervalMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void stop() {
        networkService.unsubscribe(HeadersMsg.class, this::consume);
        networkService.unsubscribe(InvMessage.class, this::consume);
        executor.shutdown();
    }

    @Override
    public <T extends Message> void consume(BitcoinMsg<T> message, PeerAddress peerAddress) {

        // Check if we've already processed this message
        if(processedMessages.contains(message.getHeader().getChecksum())){
            return;
        } else {
            processedMessages.add(message.getHeader().getChecksum());
        }

        log.debug("Consuming message type: " + message.getHeader().getCommand());

        switch(message.getHeader().getCommand()){
            case HeadersMsg.MESSAGE_TYPE:
                consumeHeaders((HeadersMsg) message.getBody());
                break;

            case InvMessage.MESSAGE_TYPE:
                consumeInv((InvMessage) message.getBody());
                break;

            default:
                throw new UnsupportedOperationException("Unhandled Message Type: " + message.getBody().getMessageType());
        }
    }

    private void consumeInv(InvMessage invMsg){
        List<InventoryVectorMsg> inventoryVectorMsgs = invMsg.getInvVectorList().stream().filter(iv -> iv.getType() == InventoryVectorMsg.VectorType.MSG_BLOCK).collect(Collectors.toList());

        //TODO someone could blacklist all connected nodes by spamming us with inv messages
        //TODO Do we need to periodically check for new headers?
        //TODO we won't need this if SendHeaders worked correctly
        requestHeaders();
    }

    private void consumeHeaders(HeadersMsg headerMsg){

        List<BlockHeader> blockHeaders = headerMsg.getBlockHeaderMsgList().stream().map(b ->
                BlockHeader.builder()
                        .version(b.getVersion())
                        .hash(Sha256Wrapper.of(b.getHash().getHashBytes()))
                        .prevBlockHash(Sha256Wrapper.of(b.getPrevBlockHash().getHashBytes()))
                        .merkleRoot(Sha256Wrapper.of(b.getMerkleRoot().getHashBytes()))
                        .time(b.getCreationTimestamp())
                        .difficultyTarget(b.getDifficultyTarget())
                        .nonce(b.getNonce())
                        .numTxs(b.getTransactionCount().getValue())
                        .build()
        ).collect(Collectors.toList());



        blockStore.saveBlocks(blockHeaders);

        //TODO if we fork, only continue down the configured chain

        requestHeaders();
    }

    public void requestHeaders(){
        blockStore.getTipsChains().forEach(b -> {
            log.info("Requesting headers for branch: " + b.toString());
            networkService.broadcast(getHeaderMsgFromHash(b.toString()));
        });
    }

    public void requestHeadersFromHash(Sha256Wrapper hash){
        networkService.broadcast(getHeaderMsgFromHash(hash.toString()));
    }

    private GetHeadersMsg getHeaderMsgFromHash(String hash){
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
