package com.nchain.headerSV.config;

import com.nchain.bna.network.config.NetConfig;
import com.nchain.bna.network.config.NetLocalDevConfig;
import com.nchain.bna.protocol.config.ProtocolConfig;
import com.nchain.bna.protocol.config.provided.ProtocolBSVMainConfig;
import com.nchain.bna.tools.RuntimeConfig;
import com.nchain.bna.tools.RuntimeConfigAutoImpl;
import com.nchain.bna.tools.files.FileUtils;
import com.nchain.bna.tools.files.FileUtilsFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.util.OptionalInt;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 05/06/2020
 */
@SpringBootApplication
@Slf4j
public class HeaderSVConfig {

    @Autowired
    private FoldersConfig foldersConfig;

    @Autowired
    private ListenerConfig listenerConfig;

    /**
     * Runtime Configuration
     */
    @Bean
    RuntimeConfig runtimeConfig() {
        return RuntimeConfigAutoImpl.builder().build();
    }

    /**
     * The P2P Network Configuration
     */
    @Bean
    NetConfig networkConfig() {
        // A normal configuration based on a Local Development Environment (a medium-level laptop)
        // We raise the number of socket connections a little bit...
        return new NetLocalDevConfig().toBuilder()
                .maxSocketConnections(OptionalInt.of(100))
                .build();
    }

    /**
     * The Network Protocol Configuration
     */
    @Bean
    @Profile({"local-bsv-mainnet", "prod-bsv-mainnet"})
    ProtocolConfig protocolLocalMainConfig() {
        return new ProtocolBSVMainConfig().toBuilder()
                .handshakeMaxPeers(OptionalInt.of(listenerConfig.getMaxPeers()))
                .handshakeMinPeers(OptionalInt.of(listenerConfig.getMinPeers()))
                .handshakeUsingRelay(listenerConfig.isRelayTxs())
                .handshakeProtocolVersion(70015)
                .build();
    }

    /**
     * The FileUtils instance, to perform operations on the file system.
     * If one Data folder is specified, it returns a FileUtils that uses a OS temporary folder, otherwise
     * it uses the folders provided. In both cases, the folders are pre-filled with the data stored in the
     * equivalent folders in the classpath.
     */
    @Bean
    FileUtils fileUtils() throws IOException {
        log.info("Data folder: " + foldersConfig.data);
        log.info("Config folder: " + foldersConfig.config);
        FileUtils fileUtils = FileUtilsFactory.copyFromClasspath(this.getClass().getClassLoader(),
                foldersConfig.data, foldersConfig.config);
        return fileUtils;
    }

}
