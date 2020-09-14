package com.nchain.headerSV.config;


import com.nchain.jcl.base.tools.files.FileUtils;
import com.nchain.jcl.base.tools.files.FileUtilsBuilder;
import com.nchain.jcl.net.protocol.config.ProtocolConfig;
import com.nchain.jcl.net.protocol.config.provided.ProtocolBSVMainConfig;
import com.nchain.jcl.net.protocol.config.provided.ProtocolBSVStnConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 05/06/2020
 */
@SpringBootApplication
@Slf4j
public class HeaderSVConfig {

    /**
     * The Network Protocol Configuration
     */
    @Bean
    @Profile({"local-bsv-mainnet", "docker-bsv-mainnet"})
    ProtocolConfig protocolLocalMainConfig() {
        return new ProtocolBSVMainConfig().toBuilder().build();
    }

    /**
     * The Network Protocol Configuration
     */
    @Bean
    @Profile({"local-bsv-stnnet", "docker-bsv-stnnet"})
    ProtocolConfig protocolLocalStnConfig() {
        return new ProtocolBSVStnConfig().toBuilder().build();
    }

    /**
     * The FileUtils instance, to perform operations on the file system.
     * If one Data folder is specified, it returns a FileUtils that uses a OS temporary folder, otherwise
     * it uses the folders provided. In both cases, the folders are pre-filled with the data stored in the
     * equivalent folders in the classpath.
     */
    @Bean
    FileUtils fileUtils() throws IOException {
        return  new FileUtilsBuilder().useClassPath().build(this.getClass().getClassLoader());
    }

}
