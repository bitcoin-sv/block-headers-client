package io.bitcoinsv.headerSV.core.service.storage;

import io.bitcoinsv.jcl.store.blockChainStore.BlockChainStore;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 * @author i.fernandez@nchain.com
 *
 * Definition of the Operations provided by the StorageService. For simplicity, we just reuse the blockChainStore
 * service defined in JCL.
 */
public interface StorageService extends BlockChainStore {
}
