package com.nchain.headerSV.config;

import com.nchain.jcl.base.domain.api.base.BlockHeader;
import com.nchain.jcl.net.protocol.config.*;

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
    private static int services;
    private static int port;
    private static int protocolVersion;
    private static String[] userAgentBlacklist;
    private static String[] userAgentWhitelist;
    private static String[] dns;
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
            .servicesSupported(services)
            .port(port)
            .protocolVersion(protocolVersion)
            .dns(dns)
            .userAgentBlacklist(userAgentBlacklist)
            .userAgentWhitelist(userAgentWhitelist)
            .build();


    public ProtocolBSVTestnetConfig() {
        super( null,
                null,
                null,
                genesisBlock,
                basicConfig,
                null,            // Default Network Config
                null,           // Default Message Config
                null,          // Default Gandshake Config
                null,           // Default PingPong Config
                null,           // Default Discovery Config
                null,            // Default Blacklist Config
                null);    // Default Block Downloader Config
    }

    public String getId() {
        return id;
    }

    static {
        services = ProtocolServices.NODE_BLOOM.getProtocolServices();
        port = 18333;
        protocolVersion = ProtocolVersion.CURRENT.getBitcoinProtocolVersion();
        userAgentBlacklist = new String[]{"Bitcoin ABC:", "BUCash:"};
        userAgentWhitelist = new String[]{"Bitcoin SV:", "/bitcoinj-sv:0.0.7/"};

        dns = new String[] {
                "testnet-seed.bitcoinsv.io",
                "testnet-seed.cascharia.com",
                "testnet-seed.bitcoincloud.net"
        };
    }
}
