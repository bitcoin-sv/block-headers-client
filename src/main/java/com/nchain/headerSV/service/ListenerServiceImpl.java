package com.nchain.headerSV.service;

import com.nchain.bna.network.PeerAddress;
import com.nchain.bna.network.config.NetConfig;
import com.nchain.bna.protocol.config.ProtocolConfig;
import com.nchain.bna.protocol.handlers.SetupHandlersBuilder;
import com.nchain.bna.protocol.listeners.PeerHandshakeAcceptedListener;
import com.nchain.bna.protocol.messages.VersionMsg;
import com.nchain.bna.tools.RuntimeConfig;
import com.nchain.bna.tools.files.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 03/06/2020
 */
@Service
public class ListenerServiceImpl implements ListenerService {

    // Basic Configuration to connect to the P2P Network and use the Bitcoin Protocol:
    private final RuntimeConfig runtimeConfig;
    private final NetConfig netConfig;
    private final ProtocolConfig protocolConfig;
    private final FileUtils fileUtils;

    private SetupHandlersBuilder.HandlersSetup protocolHandler;

//    @Autowired
//    public ListenerServiceImpl() {
//
//         runtimeConfig = RuntimeConfigAutoImpl.builder().build();
//         netConfig = new NetLocalDevConfig().toBuilder()
//                .maxSocketConnections(OptionalInt.of(100))
//                .build();
//
//         protocolConfig = new ProtocolBSVMainConfig().toBuilder()
//                .handshakeMaxPeers(OptionalInt.of(100))
//                .discoveryMethod(ProtocolConfig.DiscoveryMethod.DNS)
//                .discoveryCheckingPeerReachability(false)
//                .build();
//
//         fileUtils = new ClasspathFileUtils(this.getClass().getClassLoader());
//    }

    @Autowired
    protected ListenerServiceImpl(RuntimeConfig runtimeConfig,
                                  NetConfig netConfig, ProtocolConfig protocolConfig,
                                  FileUtils fileUtils) {
        this.runtimeConfig = runtimeConfig;
        this.netConfig = netConfig;
        this.protocolConfig = protocolConfig;
        this.fileUtils = fileUtils;

    }

    private void init() {
        // We connect all the Handlers together:
        protocolHandler = SetupHandlersBuilder.newSetup()
                .config()
                .id("HeaderSv")
                .runtime(runtimeConfig)
                .network(netConfig)
                .protocol(protocolConfig)
                .handlers()
                .useFileUtils(fileUtils)
                .startWithBlock(1)
                .custom()
                .addCallback((PeerHandshakeAcceptedListener) this::onPeerHandshaked)
                .done();


    }
    @Override
    public void start() {
        init();
        protocolHandler.start();
    }

    @Override
    public void stop() {
        protocolHandler.stop();
    }

    private void onPeerHandshaked(PeerAddress peerAddress, VersionMsg versionMsg) {
        System.out.println("IP :"+peerAddress.toString()+":"+ versionMsg.getUser_agent() +": Version :" + versionMsg.getVersion());
    }
}
