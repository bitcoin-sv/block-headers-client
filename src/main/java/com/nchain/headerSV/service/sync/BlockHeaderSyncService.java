package com.nchain.headerSV.service.sync;


import com.nchain.headerSV.service.HeaderSvService;
import com.nchain.headerSV.service.consumer.MessageConsumer;
import com.nchain.headerSV.service.network.NetworkService;
import com.nchain.headerSV.tools.Util;
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


import java.math.BigInteger;
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

    @Setter
    private Set<String> headersToIgnore = Collections.emptySet();

    @Setter
    /*This is the minimum amount of proof of work required. Defaulted the same as https://github.com/bitcoin-sv/bitcoin-sv/blob/master/src/chainparams.cpp#L104.
      If this is not set or extremely low, the application would be prone to spamming of valid fake headers*/
    private String powLimit = "00000000ffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
    private BigInteger powLimitValue;


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

        powLimitValue = Sha256Wrapper.wrap(powLimit).toBigInteger();
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
        List<BlockHeader> blockHeaders = new ArrayList<>(headerMsg.getBlockHeaderMsgList().size());
        for(BlockHeaderMsg blockHeaderMsg : headerMsg.getBlockHeaderMsgList()){

            //Reject the whole mesage if the peer is sending bad blocks
            if (!validBlockHeader(blockHeaderMsg, peerAddress)){
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

        //For the tips that have changed,
        updatedtips.stream().forEach(this::requestHeadersFromHash);
    }

    private boolean validBlockHeader(BlockHeaderMsg blockHeaderMsg, PeerAddress peerAddress){
        BigInteger target = Util.decompressCompactBits(blockHeaderMsg.getDifficultyTarget());
        BigInteger blockWork = Sha256Wrapper.wrap(blockHeaderMsg.getHash().getHashBytes()).toBigInteger();

        if(blockWork.compareTo(target) > 0){
            log.info("Message containing header: " + blockHeaderMsg.getHash().toString() + " has been rejected due to not enough work");
            networkService.blacklistPeer(peerAddress);
            return false;
        }

        if(blockWork.compareTo(powLimitValue) > 0){
            log.info("Message containing header: " + blockHeaderMsg.getHash().toString() + " has been rejected due to not satisfying the minimum amount of work");
            networkService.blacklistPeer(peerAddress);
            return false;
        }

        //TODO ensure minimum target aligns with expected from the network

        //Reject the whole message if any of them are in the ignore list
        if(headersToIgnore.contains(blockHeaderMsg.getHash().toString())){
            log.info("Message containing header: " + blockHeaderMsg.getHash().toString() + " has been rejected due to being in the ignore list");
            networkService.blacklistPeer(peerAddress);
            return false;
        }

        /* We don't want to process this message, even it has some headers. Oherwise the different threads may request a branch that has already been processed, slowing down sync times.
           This also catches duplicate messages, so there's no need to store the message checksum and compare each message*/
        if (blockStore.getTipsChains().contains(Sha256Wrapper.wrap(blockHeaderMsg.getHash().getHashBytes()))) {
            log.debug("Message containing header: " + blockHeaderMsg.getHash().toString() + " has been rejected due to it containing processed headers");
            return false;
        }

        return true;
    }

    private void requestHeadersFromHash(Sha256Wrapper hash){
        log.info("Requesting headers from block: " + hash + " at height: " + blockStore.getBlockChainInfo(hash).get().getHeight());
        networkService.broadcast(buildGetHeaderMsgFromHash(hash.toString()), true);
    }

    private void requestHeadersFromHash(Sha256Wrapper hash, PeerAddress peerAddress){
        log.debug("Requesting headers from block: " + hash + " at height: " + blockStore.getBlockChainInfo(hash).get().getHeight() + " from peer: " + peerAddress);
        networkService.send(buildGetHeaderMsgFromHash(hash.toString()), peerAddress, false);
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
