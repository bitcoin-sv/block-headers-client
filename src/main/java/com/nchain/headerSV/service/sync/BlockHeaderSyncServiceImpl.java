package com.nchain.headerSV.service.sync;


import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import com.nchain.headerSV.domain.BlockHeaderAddrInfo;
import com.nchain.headerSV.service.HeaderSvService;
import com.nchain.headerSV.service.cache.BlockHeaderCacheService;
import com.nchain.headerSV.service.consumer.MessageConsumer;
import com.nchain.headerSV.service.network.NetworkService;
import com.nchain.headerSV.service.propagation.buffer.BufferedBlockHeaders;
import com.nchain.headerSV.service.propagation.buffer.MessageBufferService;
import com.nchain.headerSV.tools.Util;
import com.nchain.jcl.network.PeerAddress;
import com.nchain.jcl.protocol.config.ProtocolConfig;
import com.nchain.jcl.protocol.messages.*;
import com.nchain.jcl.protocol.messages.common.BitcoinMsg;
import com.nchain.jcl.protocol.messages.common.Message;
import com.nchain.jcl.tools.bytes.HEX;
import com.nchain.jcl.tools.crypto.Sha256Wrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author {m.fletcher}@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 27/07/2020
 */
@Service
@Slf4j
public class BlockHeaderSyncServiceImpl implements HeaderSvService, MessageConsumer {

    private final NetworkService networkService;
    private final ProtocolConfig protocolConfig;
    private final MessageBufferService messageBufferService;
    private final BlockHeaderCacheService blockHeaderCacheService;
    private ScheduledExecutorService executor;

    private static int REQUEST_HEADER_SCHEDULE_TIME_MS = 30000;
    private static int REQUEST_HEADER_SCHEDULE_TIME_INITIAL_DELAY_MS = 5000;

    protected BlockHeaderSyncServiceImpl(NetworkService networkService,
                                         ProtocolConfig protocolConfig,
                                         MessageBufferService messageBufferService,
                                         BlockHeaderCacheService blockHeaderCacheService) {
        this.networkService = networkService;
        this.protocolConfig = protocolConfig;
        this.blockHeaderCacheService = blockHeaderCacheService;
        this.messageBufferService = messageBufferService;

        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public synchronized void start() {
        networkService.subscribe(HeadersMsg.class, this::consume);
        networkService.subscribe(InvMessage.class, this::consume);

        /* periodically check for new incoming headers */
        executor.scheduleAtFixedRate(this::requestHeaders, REQUEST_HEADER_SCHEDULE_TIME_INITIAL_DELAY_MS, REQUEST_HEADER_SCHEDULE_TIME_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void stop() {
        networkService.unsubscribe(HeadersMsg.class, this::consume);
        networkService.unsubscribe(InvMessage.class, this::consume);
        executor.shutdown();
    }

    @Override
    public <T extends Message> void consume(BitcoinMsg<T> message, PeerAddress peerAddress) {

        log.info("Consuming message type: " + message.getHeader().getCommand());

        switch(message.getHeader().getCommand()){
            case HeadersMsg.MESSAGE_TYPE:
                consumeHeaders((HeadersMsg) message.getBody(), peerAddress);
                break;

            case InvMessage.MESSAGE_TYPE:
                consumeInv((InvMessage) message.getBody(), peerAddress);
                break;

            default:
                throw new UnsupportedOperationException("Unhandled Message Type: " + message.getBody().getMessageType());
        }
    }

    private void consumeInv(InvMessage invMsg, PeerAddress peerAddress){
        List<InventoryVectorMsg> inventoryVectorMsgs = invMsg.getInvVectorList().stream().filter(iv -> iv.getType() == InventoryVectorMsg.VectorType.MSG_BLOCK).collect(Collectors.toList());

        inventoryVectorMsgs.forEach(iv -> requestHeadersFromHash(Sha256Wrapper.wrapReversed(iv.getHashMsg().getHashBytes()).toString(), peerAddress));
    }

    private void consumeHeaders(HeadersMsg headerMsg, PeerAddress peerAddress){
        List<BlockHeader> validBlockHeaders = headerMsg.getBlockHeaderMsgList().stream().filter(this::validBlockHeader).map(BlockHeader::of).collect(Collectors.toList());
        Set<BlockHeader> headersAddedToCache = blockHeaderCacheService.addToCache(validBlockHeaders);
        List<BlockHeaderAddrInfo> headersToPersist = validBlockHeaders.stream().map(b -> BlockHeaderAddrInfo.of(b, peerAddress)).collect(Collectors.toList());

        //we want to persist PeerAddresses, even if the header exists.
        messageBufferService.queue(new BufferedBlockHeaders(headersToPersist));

        //if we received new headers, request the next batch
        if(headersAddedToCache.size() > 0) {
            String lastHash = headerMsg.getBlockHeaderMsgList().get(headerMsg.getBlockHeaderMsgList().size() - 1).getHash().toString();
            requestHeadersFromHash(lastHash);
        }
    }


    private boolean validBlockHeader(BlockHeaderMsg blockHeader){
        //check proof of work
        BigInteger target = Util.decompressCompactBits(blockHeader.getDifficultyTarget());
        BigInteger blockHashValue = Sha256Wrapper.wrap(blockHeader.getHash().getHashBytes()).toBigInteger();

        if(blockHashValue.compareTo(target) > 0){
            return false;
        }

        return true;
    }

    private void requestHeadersFromHash(String hash, PeerAddress peerAddress){
        networkService.send(getHeaderFromHash(hash), peerAddress);
    }

    private void requestHeadersFromHash(String hash){
        networkService.broadcast(getHeaderFromHash(hash));
    }

    private void requestHeaders(){
        blockHeaderCacheService.getBranches().forEach(b -> {
            log.info("Requesting headers for branch: " + b.getLeafNode());
            networkService.broadcast(getHeaderFromHash(b.getLeafNode()));
        });
    }

    private GetHeadersMsg getHeaderFromHash(String hash){
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
