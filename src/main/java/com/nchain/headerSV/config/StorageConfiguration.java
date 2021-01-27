package com.nchain.headerSV.config;

import com.nchain.jcl.base.tools.config.provided.RuntimeConfigDefault;
import com.nchain.jcl.store.blockChainStore.BlockChainStore;
import com.nchain.jcl.store.levelDB.blockChainStore.BlockChainStoreLevelDB;
import com.nchain.jcl.store.levelDB.blockChainStore.BlockChainStoreLevelDBConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2021 nChain Ltd
 * @date 14/01/2021
 */
@Configuration
public class StorageConfiguration {

    private BlockChainStore blockChainStore;


    public StorageConfiguration(NetworkConfiguration networkConfiguration,
                                @Value("${headersv.storage.pruning.fork.enabled:false}") boolean forkPruningEnabled,
                                @Value("${headersv.storage.pruning.fork.pruneAfterConfirmations:7}") int pruneAfterConfirmations,
                                @Value("${headersv.storage.pruning.orphan.enabled:false}") boolean orphanPruningEnabled,
                                @Value("${headersv.storage.pruning.orphan.pruneAfterIntervalSeconds:600}") int orphanPruneInterval){

        BlockChainStoreLevelDBConfig blockChainStoreLevelDBConfig = BlockChainStoreLevelDBConfig.chainBuild()
               .networkId(networkConfiguration.getProtocolConfig().getId())
               .runtimeConfig(new RuntimeConfigDefault())
               .genesisBlock(networkConfiguration.getGenesisBlock())
               .orphanPrunningBlockAge(Duration.ofSeconds(orphanPruneInterval))
               .forkPrunningHeightDifference(pruneAfterConfirmations)
               .build();

        blockChainStore = BlockChainStoreLevelDB.chainStoreBuilder()
                .config(blockChainStoreLevelDBConfig)
                .enableAutomaticForkPrunning(forkPruningEnabled)
                .enableAutomaticOrphanPrunning(orphanPruningEnabled)
                .orphanPrunningFrequency(Duration.ofSeconds(30))
                .forkPrunningFrequency(Duration.ofSeconds(30))
                .build();
    }

    @Bean
    public BlockChainStore getBlockStore() {
       return blockChainStore;
    }
}
