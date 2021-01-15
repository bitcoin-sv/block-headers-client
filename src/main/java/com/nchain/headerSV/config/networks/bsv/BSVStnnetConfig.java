package com.nchain.headerSV.config.networks.bsv;

import com.nchain.headerSV.tools.Util;
import com.nchain.jcl.base.domain.api.base.BlockHeader;
import com.nchain.jcl.net.protocol.config.*;
import com.nchain.jcl.net.protocol.handlers.discovery.DiscoveryHandlerConfig;
import com.nchain.jcl.net.protocol.handlers.handshake.HandshakeHandlerConfig;

/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2021 nChain Ltd
 * @date 14/01/2021
 */
public class BSVStnnetConfig extends ProtocolConfigImpl implements ProtocolConfig {

    private static String id = "BSV [STN Net]";
    private static long magicPackage = 0xf9c4cefbL;
    private static int services = ProtocolServices.NODE_BLOOM.getProtocolServices();
    private static int port = 9333;
    private static int protocolVersion = ProtocolVersion.CURRENT.getBitcoinProtocolVersion();
    private static String[] userAgentBlacklist = new String[] {"Bitcoin ABC:", "BUCash:" };
    private static String[] userAgentWhitelist = new String[] {"Bitcoin SV:", HandshakeHandlerConfig.DEFAULT_USER_AGENT };
    private static String[] dns = new String[]{"stn-seed.bitcoinsv.io"};
    private static BlockHeader genesisBlock = Util.GENESIS_BLOCK_HEADER_STNNET;

    private static ProtocolBasicConfig basicConfig = ProtocolBasicConfig.builder().id(id).magicPackage(magicPackage).port(port).protocolVersion(protocolVersion).build();;

    public BSVStnnetConfig() {
        super(null, null, null, genesisBlock, basicConfig, null, null, HandshakeHandlerConfig.builder().userAgentBlacklist(userAgentBlacklist).userAgentWhitelist(userAgentWhitelist).servicesSupported(services).build(), null, DiscoveryHandlerConfig.builder().dns(dns).build(), null, null);
    }

    public String getId() {
        return id;
    }
}
