package com.nchain.headerSV.config;

import com.nchain.headerSV.tools.Util;
import com.nchain.jcl.base.tools.config.RuntimeConfig;
import com.nchain.jcl.base.tools.config.provided.RuntimeConfigDefault;
import com.nchain.jcl.store.levelDB.blockChainStore.BlockChainStoreLevelDB;
import com.nchain.jcl.store.levelDB.blockChainStore.BlockChainStoreLevelDBConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2021 nChain Ltd
 * @date 11/01/2021
 */
@Configuration
public class StorageConfig {

    RuntimeConfig runtimeConfig = new RuntimeConfigDefault();


    BlockChainStoreLevelDBConfig dbConfig = BlockChainStoreLevelDBConfig.chainBuild()
            .runtimeConfig(runtimeConfig)
            .genesisBlock(Util.GENESIS_BLOCK_HEADER)
            .build();

    @Bean
    public BlockChainStoreLevelDB getBlockStore() {

        return BlockChainStoreLevelDB.chainBuilder()
                .config(dbConfig)
                .build();
    }

}
