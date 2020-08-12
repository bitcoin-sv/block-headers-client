package com.nchain.headerSV.service.sync;

import com.nchain.bna.network.PeerAddress;
import com.nchain.bna.protocol.config.ProtocolConfig;
import com.nchain.bna.protocol.messages.*;
import com.nchain.bna.protocol.messages.common.BitcoinMsg;
import com.nchain.bna.protocol.messages.common.Message;
import com.nchain.bna.tools.bytes.HEX;
import com.nchain.bna.tools.crypto.Sha256Wrapper;
import com.nchain.headerSV.dao.model.BlockHeaderDTO;
import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import com.nchain.headerSV.domain.BlockHeaderAddrInfo;
import com.nchain.headerSV.service.HeaderSvService;
import com.nchain.headerSV.service.cache.BlockHeaderCacheService;
import com.nchain.headerSV.service.consumer.MessageConsumer;
import com.nchain.headerSV.service.network.NetworkService;
import com.nchain.headerSV.service.propagation.buffer.BufferedBlockHeaders;
import com.nchain.headerSV.service.propagation.buffer.MessageBufferService;
import com.nchain.headerSV.tools.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
public class BlockHeadersSyncServiceImpl implements HeaderSvService, MessageConsumer {

    private final NetworkService networkService;
    private final MessageBufferService messageBufferService;
    private final ProtocolConfig protocolConfig;
    private final BlockHeaderCacheService blockHeaderCacheService;
    private final TreeSet<Long> processedHeaders;

    protected BlockHeadersSyncServiceImpl(NetworkService networkService,
                                          MessageBufferService messageBufferService,
                                          ProtocolConfig protocolConfig,
                                          BlockHeaderCacheService blockHeaderCacheService) {
        this.networkService = networkService;
        this.protocolConfig = protocolConfig;
        this.blockHeaderCacheService = blockHeaderCacheService;
        this.messageBufferService = messageBufferService;

        this.processedHeaders = new TreeSet<>();
    }

    @Override
    public synchronized void start() {
        networkService.subscribe(HeadersMsg.class, this::consume);
        checkForHeaders();
    }

    @Override
    public synchronized void stop() {
        networkService.unsubscribe(HeadersMsg.class, this::consume);
    }

    private boolean hasMessageBeenProcessed(Long checksum){
        return processedHeaders.add(checksum);
    }

    @Override
    public <T extends Message> void consume(BitcoinMsg<T> message, PeerAddress peerAddress) {
        if(!(message.getBody() instanceof HeadersMsg)){
            throw new UnsupportedOperationException("BlockHeadersSyncService can only consume objects of type 'HeadersMsg'");
        }

        if(hasMessageBeenProcessed(message.getHeader().getChecksum()))
            return;

        log.info("Consuming message type: " + HeadersMsg.MESSAGE_TYPE);

        HeadersMsg headerMsg = (HeadersMsg) message.getBody();

        List<BlockHeader> validBlockHeaders = headerMsg.getBlockHeaderMsgList().stream().filter(this::validBlockHeader).map(BlockHeader::of).collect(Collectors.toList());
        Set<BlockHeader> headersAddedToCache = blockHeaderCacheService.addToCache(validBlockHeaders);
        List<BlockHeaderAddrInfo> headersToPersist = headersAddedToCache.stream().map(b -> BlockHeaderAddrInfo.of(b, peerAddress)).collect(Collectors.toList());

        //if we cached headers, request the next batch
        if(headersToPersist.size() > 0) {
            messageBufferService.queue(new BufferedBlockHeaders(headersToPersist));
            checkForHeaders();
        }

        log.info("Finished consuming message type: " + HeadersMsg.MESSAGE_TYPE);

    }

    public boolean validBlockHeader(BlockHeaderMsg blockHeader){
        //check proof of work
        BigInteger target = Util.decompressCompactBits(blockHeader.getDifficultyTarget());
        BigInteger blockHashValue = Sha256Wrapper.wrap(blockHeader.getHash().getHashBytes()).toBigInteger();

        if(blockHashValue.compareTo(target) > 0){
            return false;
        }

        return true;
    }


    private void checkForHeaders(){
        blockHeaderCacheService.getBranches().forEach(b -> {
            log.info("Requesting headers for branch: " + b.getLeafNode());
            HashMsg hashMsg = HashMsg.builder().hash(HEX.decode(b.getLeafNode())).build();

            List<HashMsg> hashMsgs = Arrays.asList(hashMsg);

            BaseGetDataAndHeaderMsg baseGetDataAndHeaderMsg = BaseGetDataAndHeaderMsg.builder()
                    .version(protocolConfig.getHandshakeProtocolVersion())
                    .blockLocatorHash(hashMsgs)
                    .hashCount(VarIntMsg.builder().value(1).build())
                    .hashStop(HashMsg.builder().hash(Sha256Wrapper.ZERO_HASH.getBytes()).build())
                    .build();

            GetHeadersMsg getHeadersMsg = GetHeadersMsg.builder()
                    .baseGetDataAndHeaderMsg(baseGetDataAndHeaderMsg)
                    .build();

            networkService.send(getHeadersMsg);
        });
    }
}
