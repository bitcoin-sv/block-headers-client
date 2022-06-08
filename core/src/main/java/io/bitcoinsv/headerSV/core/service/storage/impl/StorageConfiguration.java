package io.bitcoinsv.headerSV.core.service.storage.impl;

import io.bitcoinsv.bitcoinjsv.params.Net;
import io.bitcoinsv.jcl.net.protocol.config.ProtocolConfig;
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
 * @author m.fletcher@nchain.com
 * @author i.fernandez@nchain.com
 *
 * Storage Configuration.
 */

public class StorageConfiguration {

    private Net net;
    private boolean forkPruningEnabled;
    private int pruneAfterConfirmations;
    private boolean orphanPruningEnabled;
    private int orphanPruneInterval;

    /** Constructor */
    public StorageConfiguration(Net net,
                                boolean forkPruningEnabled,
                                int pruneAfterConfirmations,
                                boolean orphanPruningEnabled,
                                int orphanPruneInterval) {

        this.net = net;
        this.forkPruningEnabled = forkPruningEnabled;
        this.pruneAfterConfirmations = pruneAfterConfirmations;
        this.orphanPruningEnabled = orphanPruningEnabled;
        this.orphanPruneInterval = orphanPruneInterval;
    }

    public Net getNet()                     { return this.net;}
    public boolean isForkPruningEnabled()   { return this.forkPruningEnabled;}
    public int getPruneAfterConfirmations() { return this.pruneAfterConfirmations;}
    public boolean isOrphanPruningEnabled() { return this.orphanPruningEnabled;}
    public int getOrphanPruneInterval()     { return this.orphanPruneInterval;}

    public static StorageConfigurationBuilder builder() {
        return new StorageConfigurationBuilder();
    }

    /**
     * Builder
     */
    public static class StorageConfigurationBuilder {
        private Net net;
        private boolean forkPruningEnabled;
        private int pruneAfterConfirmations;
        private boolean orphanPruningEnabled;
        private int orphanPruneInterval;

        public StorageConfigurationBuilder net(Net net) {
            this.net = net;
            return this;
        }

        public StorageConfigurationBuilder forkPrunningEnabled(boolean forkPruningEnabled) {
            this.forkPruningEnabled = forkPruningEnabled;
            return this;
        }

        public StorageConfigurationBuilder pruneAfterConfirmations(int pruneAfterConfirmations) {
            this.pruneAfterConfirmations = pruneAfterConfirmations;
            return this;
        }

        public StorageConfigurationBuilder orphanPrunningEnabled(boolean orphanPrunningEnabled) {
            this.orphanPruningEnabled = orphanPrunningEnabled;
            return this;
        }

        public StorageConfigurationBuilder orphanPruneInterval(int orphanPruneInterval) {
            this.orphanPruneInterval = orphanPruneInterval;
            return this;
        }

        public StorageConfiguration build() {
            return new StorageConfiguration(net, forkPruningEnabled, pruneAfterConfirmations, orphanPruningEnabled, orphanPruneInterval);
        }
    }
}
