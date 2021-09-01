package io.bitcoinsv.headerSV.config;

import io.bitcoinsv.jcl.store.blockChainStore.BlockChainStore;
import io.bitcoinsv.jcl.store.blockChainStore.validation.RuleConfigBuilder;
import io.bitcoinsv.jcl.store.levelDB.blockChainStore.BlockChainStoreLevelDB;
import io.bitcoinsv.jcl.store.levelDB.blockChainStore.BlockChainStoreLevelDBConfig;
import io.bitcoinsv.jcl.tools.config.provided.RuntimeConfigDefault;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
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
               .ruleConfig(RuleConfigBuilder.get(networkConfiguration.getNetworkParams().getNet().params()))
               .build();

        blockChainStore = BlockChainStoreLevelDB.chainStoreBuilder()
                .config(blockChainStoreLevelDBConfig)
                .enableAutomaticForkPrunning(forkPruningEnabled)
                .enableAutomaticOrphanPrunning(orphanPruningEnabled)
                .orphanPrunningFrequency(Duration.ofSeconds(180))
                .forkPrunningFrequency(Duration.ofSeconds(180))
                .build();

    }

    @Bean
    public BlockChainStore getBlockStore() {
       return blockChainStore;
    }
}
