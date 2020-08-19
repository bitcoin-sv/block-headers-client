package com.nchain.headerSV.config;


import com.nchain.jcl.network.config.NetworkConfig;
import com.nchain.jcl.network.config.NetworkConfigImpl;
import com.nchain.jcl.protocol.config.ProtocolConfig;
import com.nchain.jcl.protocol.config.provided.ProtocolBSVMainConfig;
import com.nchain.jcl.protocol.wrapper.P2P;
import com.nchain.jcl.protocol.wrapper.P2PBuilder;
import com.nchain.jcl.tools.config.RuntimeConfig;
import com.nchain.jcl.tools.config.RuntimeConfigImpl;
import com.nchain.jcl.tools.files.FileUtils;
import com.nchain.jcl.tools.files.FileUtilsBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

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

    /**
     * The Network Protocol Configuration
     */
    @Bean
    @Profile({"local-bsv-mainnet", "prod-bsv-mainnet"})
    ProtocolConfig protocolLocalMainConfig() {
        return new ProtocolBSVMainConfig().toBuilder().build();
    }

    /**
     * The FileUtils instance, to perform operations on the file system.
     * If one Data folder is specified, it returns a FileUtils that uses a OS temporary folder, otherwise
     * it uses the folders provided. In both cases, the folders are pre-filled with the data stored in the
     * equivalent folders in the classpath.
     */
    @Bean
    FileUtils fileUtils() throws IOException {
        return new FileUtilsBuilder().useClassPath().build();
    }
}
