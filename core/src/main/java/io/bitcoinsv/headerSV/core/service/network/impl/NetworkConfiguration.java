package io.bitcoinsv.headerSV.core.service.network.impl;

import io.bitcoinsv.bitcoinjsv.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinsv.bitcoinjsv.bitcoin.bean.base.HeaderBean;
import io.bitcoinsv.bitcoinjsv.core.Utils;
import io.bitcoinsv.bitcoinjsv.params.*;
import io.bitcoinsv.jcl.net.network.PeerAddress;
import io.bitcoinsv.jcl.net.network.config.NetworkConfig;
import io.bitcoinsv.jcl.net.network.config.provided.NetworkDefaultConfig;
import io.bitcoinsv.jcl.net.protocol.config.ProtocolConfig;
import io.bitcoinsv.jcl.net.protocol.config.ProtocolConfigBuilder;


import javax.naming.ConfigurationException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */

public class NetworkConfiguration {

    /* The JCL attempts to connect to the peers in batches. If 1/10 peers is a good peer, then it will take a long time to connect to 30
       peers if this number is low. */
    private final int NUMBER_OF_PEERS_TO_CONNECT_TO_EACH_BATCH = 300;
    private final String[] dns;
    private final List<PeerAddress> initialConnections = new ArrayList<>();

    private final ProtocolConfig protocolConfig;
    private final HeaderReadOnly genesisBlock;
    private final AbstractBitcoinNetParams networkParams;
    private final NetworkConfig jclNetworkConfig;

    /** Constructor */
    public NetworkConfiguration(String networkId,
                                int minPeers,
                                int maxPeers,
                                int port,
                                String[] dns,
                                String[] initialConnections,
                                String genesisHeaderHex) throws ConfigurationException {

        try {
            switch (networkId.toLowerCase()) {
                case "mainnet":
                    genesisBlock = NetworkUtils.GENESIS_BLOCK_HEADER_MAINNET;
                    networkParams = new MainNetParams(Net.MAINNET);
                    break;

                case "testnet":
                case "testnet3":
                    genesisBlock = NetworkUtils.GENESIS_BLOCK_HEADER_TESTNET;
                    networkParams = new TestNet3Params(Net.TESTNET3);
                    break;

                case "stn":
                case "stnnet":
                    genesisBlock = NetworkUtils.GENESIS_BLOCK_HEADER_STNNET;
                    networkParams = new STNParams(Net.STN);
                    break;

                case "regtest":
                    //We can override the genesis block to start at a later point in the chain
                    if(genesisHeaderHex != null && !genesisHeaderHex.isBlank()){
                        genesisBlock = new HeaderBean(Utils.HEX.decode(genesisHeaderHex));
                    } else {
                        genesisBlock = NetworkUtils.GENESIS_BLOCK_HEADER_REGTEST;
                    }
                    networkParams = new RegTestParams(Net.REGTEST);
                    break;

                default:
                    throw new ConfigurationException("Invalid configuration 'networkId'. Either 'mainnet', 'stnnet', 'regtest' or 'testnet'");

            }

            List<String> dnsList = (dns != null) ?  new ArrayList<>(Arrays.asList(dns)) : new ArrayList<>();

            //if there's any default dns, add them
            if (networkParams.getDnsSeeds() != null) {
                dnsList.addAll(Arrays.asList(networkParams.getDnsSeeds()));
            }

            //We might want to override the port if connecting to a custom network
            ProtocolConfig defaultConfig = ProtocolConfigBuilder.get(networkParams);
            int requiredPort = port == -1 ? defaultConfig.getBasicConfig().getPort() : port;

            protocolConfig = defaultConfig.toBuilder()
                    .minPeers(minPeers)
                    .maxPeers(maxPeers)
                    .port(requiredPort)
                    .discoveryConfig(defaultConfig.getDiscoveryConfig().toBuilder()
                            .recoveryHandshakeFrequency(Optional.of(Duration.ofSeconds(30)))
                            .recoveryHandshakeThreshold(Optional.of(Duration.ofSeconds(30)))
                            .dns(dnsList.toArray(new String[dnsList.size()]))
                            .build())
                    .build();

            jclNetworkConfig = new NetworkDefaultConfig()
                    .toBuilder()
                    .maxSocketConnectionsOpeningAtSameTime(NUMBER_OF_PEERS_TO_CONNECT_TO_EACH_BATCH)
                    .build();

            this.dns = dns;

            // We configure the list of initial Connections:
            if (initialConnections != null) {
                for (String peerAddress : initialConnections) {
                    this.initialConnections.add(PeerAddress.fromIp(peerAddress.trim()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ConfigurationException(e.getMessage());
        }

    }

    public ProtocolConfig getProtocolConfig()           { return protocolConfig; }
    public NetworkConfig getNetworkConfig()             { return jclNetworkConfig; }
    public HeaderReadOnly getGenesisBlock()             { return genesisBlock; }
    public AbstractBitcoinNetParams getNetworkParams()  { return networkParams; }
    public String[] getDns()                            { return dns; }
    public List<PeerAddress> getInitialConnections()    { return this.initialConnections; }

    public static NetworkConfigurationBuilder builder() {
        return new NetworkConfigurationBuilder();
    }

    /**
     * Builder
     */
    public static class NetworkConfigurationBuilder {
        private String networkId;
        private int minPeers;
        private int maxPeers;
        private int port= -1; // default
        private String[] dns;
        private String[] initialConnections;
        private String genesisHeaderHex;

        public NetworkConfigurationBuilder net(Net net) {
            this.networkId = net.name().toLowerCase();
            return this;
        }

        public NetworkConfigurationBuilder minPeers(int minPeers) {
            this.minPeers = minPeers;
            return this;
        }

        public NetworkConfigurationBuilder maxPeers(int maxPeers) {
            this.maxPeers = maxPeers;
            return this;
        }

        public NetworkConfigurationBuilder port(int port) {
            this.port = port;
            return this;
        }

        public NetworkConfigurationBuilder dsn(String[] dns) {
            this.dns = dns;
            return this;
        }

        public NetworkConfigurationBuilder initialConnections(String[] initialConnections) {
            this.initialConnections = initialConnections;
            return this;
        }

        public NetworkConfigurationBuilder genesisHeaderHex(String genesisHeaderHex) {
            this.genesisHeaderHex = genesisHeaderHex;
            return this;
        }

        public NetworkConfiguration build() throws ConfigurationException {
            return new NetworkConfiguration(networkId, minPeers, maxPeers, port, dns, initialConnections, genesisHeaderHex);
        }

    }
}
