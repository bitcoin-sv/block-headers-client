package com.nchain.headerSV.config;

import com.nchain.headerSV.tools.Util;
import com.nchain.jcl.net.network.config.NetworkConfig;
import com.nchain.jcl.net.network.config.provided.NetworkDefaultConfig;
import com.nchain.jcl.net.protocol.config.ProtocolConfig;
import com.nchain.jcl.net.protocol.config.ProtocolConfigBuilder;
import io.bitcoinj.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinj.params.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.naming.ConfigurationException;


/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2021 nChain Ltd
 * @date 14/01/2021
 */
@Configuration
public class NetworkConfiguration {

    /* The JCL attempts to connect to the peers in batches. If 1/10 peers is a good peer, then it will take a long time to connect to 30
       peers if this number is low. */
    private final int NUMBER_OF_PEERS_TO_CONNECT_TO_EACH_BATCH = 300;

    private final ProtocolConfig protocolConfig;
    private final HeaderReadOnly genesisBlock;
    private final AbstractBitcoinNetParams networkParams;
    private final NetworkConfig jclNetworkConfig;


    public NetworkConfiguration(@Value("${headersv.network.networkId:}") String networkId,
                                @Value("${headersv.network.minPeers:5}") int minPeers,
                                @Value("${headersv.network.maxPeers:15}") int maxPeers) throws ConfigurationException {

        switch (networkId) {
            case "mainnet":
                genesisBlock = Util.GENESIS_BLOCK_HEADER_MAINNET;
                networkParams = new MainNetParams(Net.MAINNET);
                break;

            case "testnet":
                genesisBlock = Util.GENESIS_BLOCK_HEADER_TESTNET;
                networkParams = new TestNet3Params(Net.TESTNET3);
                break;

            case "stnnet":
            default:
                throw new ConfigurationException("Invalid configuration 'networkId'. Either 'mainnet' or 'testnet'");

        }

        jclNetworkConfig = new NetworkDefaultConfig()
                .toBuilder()
                .maxSocketConnectionsOpeningAtSameTime(NUMBER_OF_PEERS_TO_CONNECT_TO_EACH_BATCH)
                .build();

        protocolConfig = ProtocolConfigBuilder.get(networkParams).toBuilder()
                .minPeers(minPeers)
                .maxPeers(maxPeers)
                .build();

    }


    public ProtocolConfig getProtocolConfig(){
        return protocolConfig;
    }

    public NetworkConfig getJCLNetworkConfig(){
        return jclNetworkConfig;
    }

    public HeaderReadOnly getGenesisBlock(){
        return genesisBlock;
    }

    public AbstractBitcoinNetParams getNetworkParams() { return networkParams; }

}
