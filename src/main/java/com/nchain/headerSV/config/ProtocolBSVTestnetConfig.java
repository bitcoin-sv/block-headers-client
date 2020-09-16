package com.nchain.headerSV.config;

import com.nchain.jcl.net.network.config.NetworkConfig;
import com.nchain.jcl.net.protocol.config.*;
import com.nchain.jcl.net.protocol.handlers.blacklist.BlacklistHandlerConfig;
import com.nchain.jcl.net.protocol.handlers.block.BlockDownloaderHandlerConfig;
import com.nchain.jcl.net.protocol.handlers.discovery.DiscoveryHandlerConfig;
import com.nchain.jcl.net.protocol.handlers.handshake.HandshakeHandlerConfig;
import com.nchain.jcl.net.protocol.handlers.message.MessageHandlerConfig;
import com.nchain.jcl.net.protocol.handlers.pingPong.PingPongHandlerConfig;

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


    public ProtocolBSVTestnetConfig() {
        super(id, magicPackage, services, port, protocolVersion, userAgentBlacklist, userAgentWhitelist, dns, (Integer)null, (Integer)null, (new ProtocolBasicConfig()).toBuilder().id(id).magicPackage(magicPackage).servicesSupported(services).port(port).protocolVersion(protocolVersion).build(), (NetworkConfig)null, (MessageHandlerConfig)null, (new HandshakeHandlerConfig()).toBuilder().userAgentBlacklistPatterns(userAgentBlacklist).userAgentWhitelistPatterns(userAgentWhitelist).build(), (PingPongHandlerConfig)null, (new DiscoveryHandlerConfig()).toBuilder().dnsSeeds(dns).build(), (BlacklistHandlerConfig)null, (BlockDownloaderHandlerConfig)null);
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
