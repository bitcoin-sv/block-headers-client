package com.nchain.headerSV.service.sync;

import com.nchain.bna.protocol.config.ProtocolConfig;
import com.nchain.bna.protocol.messages.*;
import com.nchain.bna.protocol.messages.common.Message;
import com.nchain.bna.tools.bytes.HEX;
import com.nchain.bna.tools.crypto.Sha256Wrapper;
import com.nchain.headerSV.dao.postgresql.repository.BlockHeaderRepository;
import com.nchain.headerSV.service.network.NetworkService;
import com.nchain.headerSV.service.propagation.buffer.MessageBufferService;
import com.nchain.headerSV.service.sync.consumer.MessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.concurrent.ThreadSafe;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
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
@ThreadSafe
public class BlockHeadersSyncServiceImpl implements BlockHeadersSyncService, MessageConsumer {

    private final NetworkService networkService;
    private final ScheduledExecutorService executor;
    private final MessageBufferService messageBufferService;
    private final BlockHeaderRepository blockHeaderRepository;
    private final ProtocolConfig protocolConfig;

    private String headBlockHash = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f";

    @Autowired
    protected BlockHeadersSyncServiceImpl(NetworkService networkService,
                                          MessageBufferService messageBufferService,
                                          BlockHeaderRepository blockHeaderRepository,
                                          ProtocolConfig protocolConfig) {
        this.networkService = networkService;
        this.messageBufferService = messageBufferService;
        this.blockHeaderRepository = blockHeaderRepository;
        this.protocolConfig = protocolConfig;

        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public synchronized void start() {
        networkService.subscribe(HeadersMsg.class, this::consume);

        executor.scheduleAtFixedRate(this::requestNextHeaders, 1000, 5000, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void stop() {
        networkService.unsubscribe(HeadersMsg.class, this::consume);

    }

    @Override
    public <T extends Message> void consume(T message) {
        if(!(message instanceof HeadersMsg)){
            throw new UnsupportedOperationException("BlockHeadersSyncService can only consume objects of type 'HeadersMsg'");
        }

        HeadersMsg headerMsg = (HeadersMsg) message;
    }


    private void requestNextHeaders(){
        HashMsg hashMsg = HashMsg.builder().hash(HEX.decode(headBlockHash)).build();

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
    }

}
