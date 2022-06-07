package io.bitcoinsv.headerSV.core.service.storage.impl;

import io.bitcoinsv.bitcoinjsv.bitcoin.api.base.HeaderReadOnly;
import io.bitcoinsv.bitcoinjsv.params.Net;
import io.bitcoinsv.bitcoinjsv.params.NetworkParameters;
import io.bitcoinsv.headerSV.core.service.network.impl.NetworkConfiguration;
import io.bitcoinsv.headerSV.core.service.network.impl.NetworkUtils;
import io.bitcoinsv.headerSV.core.service.storage.StorageService;
import io.bitcoinsv.jcl.net.protocol.config.ProtocolConfigBuilder;
import io.bitcoinsv.jcl.store.blockChainStore.validation.RuleConfigBuilder;
import io.bitcoinsv.jcl.store.levelDB.blockChainStore.BlockChainStoreLevelDB;
import io.bitcoinsv.jcl.store.levelDB.blockChainStore.BlockChainStoreLevelDBConfig;
import io.bitcoinsv.jcl.tools.config.provided.RuntimeConfigDefault;

import java.time.Duration;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author i.fernandez@nchain.com
 *
 * Implementation of the StorageService. An extension of the current BlockchainStore from JCL (LevelDB implementation)
 */
public class StorageServiceImpl extends BlockChainStoreLevelDB implements StorageService {

    /** Constructor */
    public StorageServiceImpl(NetworkConfiguration networkConfig, StorageConfiguration storageConfig) {

        super (
                BlockChainStoreLevelDBConfig.chainBuild()
                .id("header-sv")
                .networkId(networkConfig.getProtocolConfig().getId())
                .runtimeConfig(new RuntimeConfigDefault())
                .genesisBlock(networkConfig.getGenesisBlock())
                .orphanPrunningBlockAge(Duration.ofSeconds(storageConfig.getOrphanPruneInterval()))
                .forkPrunningHeightDifference(storageConfig.getPruneAfterConfirmations())
                .ruleConfig(RuleConfigBuilder.get(networkConfig.getNetworkParams().getNet().params()))
                .build(),
                false,
                false,
                null,
                null,
                null,
                storageConfig.isForkPruningEnabled(),
                Duration.ofSeconds(180),
                true,
                Duration.ofSeconds(180)
                );
    }

    /** Constructor */
    public StorageServiceImpl(NetworkParameters networkParameters, StorageConfiguration storageConfig) {
        super (
                BlockChainStoreLevelDBConfig.chainBuild()
                        .id("header-sv")
                        .networkId(ProtocolConfigBuilder.get(networkParameters).getId())
                        .runtimeConfig(new RuntimeConfigDefault())
                        .genesisBlock(getGenesisBlock(networkParameters.getNet()))
                        .orphanPrunningBlockAge(Duration.ofSeconds(storageConfig.getOrphanPruneInterval()))
                        .forkPrunningHeightDifference(storageConfig.getPruneAfterConfirmations())
                        .ruleConfig(RuleConfigBuilder.get(networkParameters))
                        .build(),
                false,
                false,
                null,
                null,
                null,
                storageConfig.isForkPruningEnabled(),
                Duration.ofSeconds(180),
                true,
                Duration.ofSeconds(180)
        );
    }

    // Returns the Genesis block according to the Network
    private static HeaderReadOnly getGenesisBlock(Net net) {
        switch (net) {
            case MAINNET:   return NetworkUtils.GENESIS_BLOCK_HEADER_MAINNET;
            case TESTNET3:  return NetworkUtils.GENESIS_BLOCK_HEADER_TESTNET;
            case STN:       return NetworkUtils.GENESIS_BLOCK_HEADER_STNNET;
            case REGTEST:   return NetworkUtils.GENESIS_BLOCK_HEADER_REGTEST;
        }
        throw new RuntimeException("No Genesis Bloc found for the Network " + net);
    }
}
