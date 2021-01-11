package com.nchain.headerSV.config;

import com.nchain.jcl.base.domain.api.base.BlockHeader;
import com.nchain.jcl.net.protocol.config.*;
import com.nchain.jcl.net.protocol.handlers.discovery.DiscoveryHandlerConfig;
import com.nchain.jcl.net.protocol.handlers.handshake.HandshakeHandlerConfig;

/*
 *   @author m.jose
 *
 *  Copyright (c) 2018-2020 nChain Ltd
 *  @date 14/09/2020, 12:02
 */
public class ProtocolBSVTestnetConfig  extends ProtocolConfigImpl implements ProtocolConfig  {

    private static String id = "BSV [Test Net]";

    // NOTE: The magic Number in BitcoinJ is specified in Reverse order as it's sent over the wire. In JCL, the value
    // is specified in the Java code in the SAME order as it traves throught the network. That's why, if we take the
    // value of the "magic2 number from BitcoinJ, we need to turn it around (in pairs, as this is hexadecimal and each
    // pair of characters represents 1 byte).
    private static long magicPackage = 0xf4f3e5f4L;
    private static int services = ProtocolServices.NODE_BLOOM.getProtocolServices();
    private static int port = 18333;
    private static int protocolVersion  = ProtocolVersion.CURRENT.getBitcoinProtocolVersion();

    private static String[] userAgentBlacklist = new String[]{"Bitcoin ABC:", "BUCash:"};;
    private static String[] userAgentWhitelist = new String[]{"Bitcoin SV:", "/bitcoinj-sv:0.0.7/"};
    private static String[] dns = new String[] {
            "testnet-seed.bitcoinsv.io",
            "testnet-seed.cascharia.com",
            "testnet-seed.bitcoincloud.net"
    };

    // Genesis Block for BSV-Main:
    private static BlockHeader genesisBlock = BlockHeader.builder()
            .difficultyTarget(0x1d00ffffL)
            .nonce(2083236893)
            .time(1231006505L)
            .build();

    // Basic Configuration:
    private static ProtocolBasicConfig basicConfig = ProtocolBasicConfig.builder()
            .id(id)
            .magicPackage(magicPackage)
            .port(port)
            .protocolVersion(protocolVersion)
            .build();

    private static HandshakeHandlerConfig handshakeHandlerConfig =HandshakeHandlerConfig.builder()
            .userAgentBlacklist(userAgentBlacklist)
            .userAgentWhitelist(userAgentWhitelist)
            .servicesSupported(services)
            .build();

    private static DiscoveryHandlerConfig discoveryHandlerConfig =   DiscoveryHandlerConfig.builder()
            .dns(dns)
            .build();

    public ProtocolBSVTestnetConfig() {
        super( null,
                null,
                null,
                genesisBlock,
                basicConfig,
                null,            // Default Network Config
                null,           // Default Message Config
                handshakeHandlerConfig,// Default Gandshake Config
                null,           // Default PingPong Config
                discoveryHandlerConfig,           // Default Discovery Config
                null,            // Default Blacklist Config
                null);    // Default Block Downloader Config
    }

    public String getId() {
        return id;
    }

}
