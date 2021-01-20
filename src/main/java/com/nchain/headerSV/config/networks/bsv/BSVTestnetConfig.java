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
    private static long magicPackage = 4109624820L; //0xF4F3E5F4
    private static int services = ProtocolServices.NODE_BLOOM.getProtocolServices();
    private static int port = 18333;
    private static int protocolVersion = ProtocolVersion.CURRENT.getBitcoinProtocolVersion();
    private static String[] userAgentBlacklist = new String[] {"Bitcoin ABC:", "BUCash:" };
    private static String[] userAgentWhitelist = new String[] {"Bitcoin SV:", HandshakeHandlerConfig.DEFAULT_USER_AGENT };
    private static String[] dns = new String[]{"testnet-seed.bitcoinsv.io", "testnet-seed.cascharia.com", "testnet-seed.bitcoincloud.net"};
    private static BlockHeader genesisBlock = Util.GENESIS_BLOCK_HEADER_STNNET;

    private static ProtocolBasicConfig basicConfig = ProtocolBasicConfig.builder().id(id).magicPackage(magicPackage).port(port).protocolVersion(protocolVersion).build();;

    public BSVTestnetConfig() {
        super(null, null, null, genesisBlock, basicConfig, null, null, HandshakeHandlerConfig.builder().userAgentBlacklist(userAgentBlacklist).userAgentWhitelist(userAgentWhitelist).servicesSupported(services).build(), null, DiscoveryHandlerConfig.builder().dns(dns).build(), null, null);
    }

    public String getId() {
        return id;
    }
}
