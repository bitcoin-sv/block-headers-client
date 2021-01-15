package com.nchain.headerSV.config.networks.bsv;

import com.nchain.headerSV.tools.Util;
import com.nchain.jcl.base.domain.api.base.BlockHeader;
import com.nchain.jcl.base.tools.crypto.Sha256Wrapper;
import com.nchain.jcl.net.protocol.config.*;
import com.nchain.jcl.net.protocol.handlers.discovery.DiscoveryHandlerConfig;
import com.nchain.jcl.net.protocol.handlers.handshake.HandshakeHandlerConfig;

/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2021 nChain Ltd
 * @date 14/01/2021
 */
public class BSVTestnetConfig extends ProtocolConfigImpl implements ProtocolConfig {

    private static String id = "BSV [Test Net]";
    private static long magicPackage = 3908297187L;
    private static int services;
    private static int port;
    private static int protocolVersion;
    private static String[] userAgentBlacklist;
    private static String[] userAgentWhitelist;
    private static String[] dns;
    private static BlockHeader genesisBlock;
    private static ProtocolBasicConfig basicConfig;

    public BSVTestnetConfig() {
        super(null, null, null, genesisBlock, basicConfig, null, null, HandshakeHandlerConfig.builder().userAgentBlacklist(userAgentBlacklist).userAgentWhitelist(userAgentWhitelist).servicesSupported(services).build(), null, DiscoveryHandlerConfig.builder().dns(dns).build(), null, null);
    }

    public String getId() {
        return id;
    }

    static {
        services = ProtocolServices.NODE_BLOOM.getProtocolServices();
        port = 8333;
        protocolVersion = ProtocolVersion.CURRENT.getBitcoinProtocolVersion();
        userAgentBlacklist = new String[]{"Bitcoin ABC:", "BUCash:"};
        userAgentWhitelist = new String[]{"Bitcoin SV:", "/bitcoinj-sv:0.0.7/"};
        dns = new String[]{"testnet-seed.bitcoinsv.io", "testnet-seed.cascharia.com", "testnet-seed.satoshivision.network"};
        genesisBlock = Util.GENESIS_BLOCK_HEADER_TESTNET;
        basicConfig = ProtocolBasicConfig.builder().id(id).magicPackage(magicPackage).port(port).protocolVersion(protocolVersion).build();
    }
}
