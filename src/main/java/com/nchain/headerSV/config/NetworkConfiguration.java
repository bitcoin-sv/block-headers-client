package com.nchain.headerSV.config;

import com.nchain.headerSV.config.networks.bsv.BSVMainnetConfig;
import com.nchain.headerSV.config.networks.bsv.BSVStnnetConfig;
import com.nchain.headerSV.config.networks.bsv.BSVTestnetConfig;
import com.nchain.headerSV.tools.Util;
import com.nchain.jcl.base.domain.api.base.BlockHeader;
import com.nchain.jcl.net.protocol.config.ProtocolConfig;
import com.nchain.jcl.net.protocol.handlers.handshake.HandshakeHandlerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.naming.ConfigurationException;


/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2021 nChain Ltd
 * @date 14/01/2021
 */
@Configuration
public class NetworkConfiguration {

    private final ProtocolConfig protocolConfig;
    private final BlockHeader genesisBlock;

    //TODO logging for all classes
    public NetworkConfiguration(@Value("${headersv.network.networkId:mainnet}") String networkId,
                                @Value("${headersv.network.minPeers:5}") int minPeers,
                                @Value("${headersv.network.maxPeers:15}") int maxPeers,
                                @Value("${headersv.network.relayTxs:false}") boolean relayTxs) throws ConfigurationException {

        HandshakeHandlerConfig handshakeHandlerConfig = HandshakeHandlerConfig.builder().relayTxs(relayTxs).build();

        switch(networkId){
            case "stnnet": //TODO
                genesisBlock = Util.GENESIS_BLOCK_HEADER_STNNET;
                protocolConfig = new BSVStnnetConfig().toBuilder()
                        .minPeers(minPeers)
                        .maxPeers(maxPeers)
                        .handshakeConfig(handshakeHandlerConfig)
                        .build();
                break;

            case "testnet": //TODO
                genesisBlock = Util.GENESIS_BLOCK_HEADER_TESTNET;
                protocolConfig = new BSVTestnetConfig().toBuilder()
                        .minPeers(minPeers)
                        .maxPeers(maxPeers)
                        .handshakeConfig(handshakeHandlerConfig)
                        .build();
                break;

            case "mainnet":
                genesisBlock = Util.GENESIS_BLOCK_HEADER_MAINNET;
                protocolConfig = new BSVMainnetConfig().toBuilder()
                        .minPeers(minPeers)
                        .maxPeers(maxPeers)
                        .handshakeConfig(handshakeHandlerConfig)
                        .build();
                break;

            default:
                throw new ConfigurationException("Invalid configuration 'networkId'. Either 'mainnet', 'stnnet' or 'testnet'");

        }
    }

    @Bean
    public ProtocolConfig getProtocolConfig(){
        return protocolConfig;
    }

    public BlockHeader getGenesisBlock(){
        return genesisBlock;
    }

}
