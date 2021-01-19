package com.nchain.headerSV.config;

import com.nchain.jcl.base.tools.config.provided.RuntimeConfigDefault;
import com.nchain.jcl.store.blockChainStore.BlockChainStore;
import com.nchain.jcl.store.levelDB.blockChainStore.BlockChainStoreLevelDB;
import com.nchain.jcl.store.levelDB.blockChainStore.BlockChainStoreLevelDBConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2021 nChain Ltd
 * @date 14/01/2021
 */
@Configuration
public class StorageConfiguration {

    private BlockChainStore blockChainStore;


    public StorageConfiguration(NetworkConfiguration networkConfiguration,
                                @Value("${headersv.storage.enableAutomaticPrunning:true}") boolean enableAutomaticPrunning){

        BlockChainStoreLevelDBConfig blockChainStoreLevelDBConfig = BlockChainStoreLevelDBConfig.chainBuild()
               .networkId(networkConfiguration.getProtocolConfig().getId())
               .runtimeConfig(new RuntimeConfigDefault())
               .genesisBlock(networkConfiguration.getGenesisBlock())
               .build();;

        blockChainStore = BlockChainStoreLevelDB.chainStoreBuilder()
                .config(blockChainStoreLevelDBConfig)
                .enableAutomaticPrunning(enableAutomaticPrunning)
                .build();
    }

    @Bean
    public BlockChainStore getBlockStore() {
       return blockChainStore;
    }
}
