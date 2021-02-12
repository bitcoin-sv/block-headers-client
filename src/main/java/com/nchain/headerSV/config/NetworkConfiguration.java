package com.nchain.headerSV.config;

import com.nchain.headerSV.tools.Util;
import com.nchain.jcl.net.protocol.config.ProtocolConfig;
import com.nchain.jcl.net.protocol.config.ProtocolConfigBuilder;
import com.nchain.jcl.net.protocol.config.provided.ProtocolBSVMainConfig;
import io.bitcoinj.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinj.params.MainNetParams;
import io.bitcoinj.params.Net;
import io.bitcoinj.params.STNParams;
import io.bitcoinj.params.TestNet3Params;
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
    private final HeaderReadOnly genesisBlock;

    public NetworkConfiguration(@Value("${headersv.network.networkId:}") String networkId,
                                @Value("${headersv.network.minPeers:5}") int minPeers,
                                @Value("${headersv.network.maxPeers:15}") int maxPeers) throws ConfigurationException {

        switch(networkId){
            case "mainnet":
                genesisBlock = Util.GENESIS_BLOCK_HEADER_MAINNET;
                protocolConfig = ProtocolConfigBuilder.get(new MainNetParams(Net.MAINNET)).toBuilder()
                        .minPeers(minPeers)
                        .maxPeers(maxPeers)
                        .build();
                break;

            case "stnnet":
                genesisBlock = Util.GENESIS_BLOCK_HEADER_STNNET;
                protocolConfig = ProtocolConfigBuilder.get(new STNParams(Net.STN)).toBuilder()
                        .minPeers(minPeers)
                        .maxPeers(maxPeers)
                        .build();
                break;

            case "testnet":
                genesisBlock = Util.GENESIS_BLOCK_HEADER_TESTNET;
                protocolConfig = ProtocolConfigBuilder.get(new TestNet3Params(Net.TESTNET3)).toBuilder()
                        .minPeers(minPeers)
                        .maxPeers(maxPeers)
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

    public HeaderReadOnly getGenesisBlock(){
        return genesisBlock;
    }

}
