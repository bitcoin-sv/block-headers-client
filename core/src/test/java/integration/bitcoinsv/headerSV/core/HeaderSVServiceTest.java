package integration.bitcoinsv.headerSV.core;


import io.bitcoinsv.bitcoinjsv.params.Net;
import io.bitcoinsv.headerSV.core.HeaderSvService;
import io.bitcoinsv.headerSV.core.config.HeaderSvConfig;
import io.bitcoinsv.headerSV.core.config.HeaderSvConfigMainnet;
import io.bitcoinsv.headerSV.core.service.storage.StorageService;
import io.bitcoinsv.headerSV.core.service.storage.impl.StorageConfiguration;
import io.bitcoinsv.headerSV.core.service.network.NetworkService;
import io.bitcoinsv.headerSV.core.service.network.impl.NetworkConfiguration;
import io.bitcoinsv.headerSV.core.service.network.impl.NetworkServiceImpl;
import io.bitcoinsv.headerSV.core.service.storage.impl.StorageServiceImpl;
import io.bitcoinsv.headerSV.core.service.HeadersSvServiceImpl;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javax.naming.ConfigurationException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author i.fernandez@nchain.com
 *
 * An integration test that uses the HeaderSV Service to conntect to the network and synchornizes to the whole
 * blockchain. We verify that the TIPS_UPDATED and CHAIN_SYNCHRONIZED are both triggered
 */
public class HeaderSVServiceTest {

    // Network Configuration for MAINNET
    private final NetworkConfiguration getNetworkConfigMainnet() throws ConfigurationException  {
        return NetworkConfiguration.builder()
                .net(Net.MAINNET)
                .minPeers(10)
                .maxPeers(15)
                .build();

    }
    // Storage Config
    private StorageConfiguration getStorageConfig(NetworkConfiguration networkConfig) {
        return StorageConfiguration.builder()
                .net(Net.MAINNET)
                .forkPrunningEnabled(true)
                .pruneAfterConfirmations(7)
                .build();
    }

    //@Test
    public void testMainNet() {
        try {

            HeaderSvConfig config = HeaderSvConfigMainnet.builder()
                    .timeoutToTriggerSyncComplete(Duration.ofSeconds(30))
                    .build();

            // We keep track of the events being triggered:
            AtomicInteger numTipsUpdates = new AtomicInteger();
            AtomicBoolean chainSynchronized = new AtomicBoolean();

            // Network and Storage Services configuration:
            NetworkConfiguration networkConfiguration = getNetworkConfigMainnet();
            NetworkService networkService = new NetworkServiceImpl(networkConfiguration);
            StorageConfiguration storageConfiguration = getStorageConfig(networkConfiguration);
            StorageService store = new StorageServiceImpl(networkConfiguration, storageConfiguration);

            // HeaderSVService Configuration and Events subscribing:
            HeaderSvService headerSVService = new HeadersSvServiceImpl(config, networkService, store);

            headerSVService.EVENTS().TIPS_UPDATED().forEach(e -> {
                System.out.println("****** TIPS_UPDATED Event Received: " + e.getUpdatedTips().size() + " tips, " + e.getNewHeaders().size() + " new headers");
                numTipsUpdates.incrementAndGet();
            });

            headerSVService.EVENTS().CHAIN_SYNCHRONIZED().forEach(e -> {
                System.out.println("****** CHAIN_SYNCHRONIZED Event Received");
                chainSynchronized.set(true);
            });

            // We start the Service. We only finish after we reach the TIP of the chain:
            headerSVService.start();
            while (!chainSynchronized.get()) {
                Thread.sleep(1_000);
            }
            System.out.println("Chain Synchronized. Stopping the Test...");
            headerSVService.stop();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
