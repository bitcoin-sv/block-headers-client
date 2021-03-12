package com.nchain.headerSV.service.sync;


import com.nchain.headerSV.service.HeaderSvService;
import com.nchain.headerSV.service.consumer.MessageConsumer;
import com.nchain.headerSV.service.network.NetworkService;
import com.nchain.headerSV.tools.Util;
import com.nchain.jcl.net.network.PeerAddress;
import com.nchain.jcl.net.protocol.config.ProtocolConfig;
import com.nchain.jcl.net.protocol.messages.*;
import com.nchain.jcl.net.protocol.messages.common.BitcoinMsg;
import com.nchain.jcl.net.protocol.messages.common.Message;
import com.nchain.jcl.store.blockChainStore.BlockChainStore;
import io.bitcoinj.bitcoin.api.base.AbstractBlock;
import io.bitcoinj.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinj.bitcoin.bean.base.HeaderBean;
import io.bitcoinj.bitcoin.bean.extended.LiteBlockBean;
import io.bitcoinj.core.Sha256Hash;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;


import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static io.bitcoinj.core.Utils.HEX;

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
    public void start() {
        log.info("Starting blockstore..");
        blockStore.start();
        log.info("Blockstore started");

        log.info("Current blockchain state: ");
        blockStore.getTipsChains().forEach(t -> {
            log.info("Chain Id: " + blockStore.getBlockChainInfo(t).get().getHeader().getHash() + " Height: " + blockStore.getBlockChainInfo(t).get().getHeight());
        });


        log.info("Listening for headers...");

        networkService.subscribe(HeadersMsg.class, this::consume, true, false);
        networkService.subscribe(InvMessage.class, this::consume, false, true);
        networkService.subscribe(VersionAckMsg.class, this::consume, false, true);

    }

    @Override
    public void stop() {}

    @Override
    public <T extends Message> void consume(BitcoinMsg<T> message, PeerAddress peerAddress) {
        log.debug("Consuming message type: " + message.getHeader().getCommand());

        switch(message.getHeader().getCommand()){
            case HeadersMsg.MESSAGE_TYPE:
                consumeHeadersMsg((HeadersMsg) message.getBody(), peerAddress);
                break;

            case InvMessage.MESSAGE_TYPE:
                consumeInvMsg((InvMessage) message.getBody(), peerAddress);
                break;

            case VersionAckMsg.MESSAGE_TYPE:
                consumeVersionAckMsg((VersionAckMsg) message.getBody(), peerAddress);
                break;

            default:
                throw new UnsupportedOperationException("Unhandled Message Type: " + message.getBody().getMessageType());
        }
    }

    private void consumeVersionAckMsg(VersionAckMsg versionAckMsg, PeerAddress peerAddress) {
        // request headers for each tip, at this point we don't know which nodes are SV and which are not
        blockStore.getTipsChains().forEach(h -> requestHeadersFromHash(h, peerAddress));

    }

    private void consumeInvMsg(InvMessage invMsg, PeerAddress peerAddress){
        List<InventoryVectorMsg> blockHeaderMessages = invMsg.getInvVectorList().stream().filter(iv -> iv.getType() == InventoryVectorMsg.VectorType.MSG_BLOCK).collect(Collectors.toList());

        if(!blockHeaderMessages.isEmpty()) {
            blockStore.getTipsChains().forEach(h -> requestHeadersFromHash(h, peerAddress));
        }
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

        //check which tips have changed
        List<Sha256Hash> updatedtips = blockStore.getTipsChains().stream().filter(h -> !branchTips.contains(h)).collect(Collectors.toList());

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

    private void requestHeadersFromHash(Sha256Hash hash){
        log.info("Requesting headers from block: " + hash + " at height: " + blockStore.getBlockChainInfo(hash).get().getHeight());
        networkService.broadcast(buildGetHeaderMsgFromHash(hash), true);
    }

    private void requestHeadersFromHash(Sha256Hash hash, PeerAddress peerAddress){
        log.info("Requesting headers from block: " + hash + " at height: " + blockStore.getBlockChainInfo(hash).get().getHeight() + " from peer: " + peerAddress);
        networkService.send(buildGetHeaderMsgFromHash(hash), peerAddress, false);
    }

    private GetHeadersMsg buildGetHeaderMsgFromHash(Sha256Hash hash){
        HashMsg hashMsg = HashMsg.builder().hash(hash.getReversedBytes()).build();
        List<HashMsg> hashMsgs = Arrays.asList(hashMsg);

        BaseGetDataAndHeaderMsg baseGetDataAndHeaderMsg = BaseGetDataAndHeaderMsg.builder()
                .version(protocolConfig.getBasicConfig().getProtocolVersion())
                .blockLocatorHash(hashMsgs)
                .hashCount(VarIntMsg.builder().value(1).build())
                .hashStop(HashMsg.builder().hash(Sha256Hash.ZERO_HASH.getBytes()).build())
                .build();

        GetHeadersMsg getHeadersMsg = GetHeadersMsg.builder()
                .baseGetDataAndHeaderMsg(baseGetDataAndHeaderMsg)
                .build();

        return getHeadersMsg;
    }

}
