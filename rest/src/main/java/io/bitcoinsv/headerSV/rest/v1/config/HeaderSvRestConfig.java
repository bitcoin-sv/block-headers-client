package io.bitcoinsv.headerSV.rest.v1.config;

import io.bitcoinsv.headerSV.core.HeaderSvService;
import io.bitcoinsv.headerSV.core.api.HeaderSvApi;
import io.bitcoinsv.headerSV.core.config.HeaderSvConfig;
import io.bitcoinsv.headerSV.core.config.HeaderSvConfigMainnet;
import io.bitcoinsv.headerSV.core.service.HeadersSvServiceImpl;
import io.bitcoinsv.headerSV.core.service.network.NetworkService;
import io.bitcoinsv.headerSV.core.service.network.impl.NetworkConfiguration;
import io.bitcoinsv.headerSV.core.service.network.impl.NetworkServiceImpl;
import io.bitcoinsv.headerSV.core.service.storage.StorageService;
import io.bitcoinsv.headerSV.core.service.storage.impl.StorageConfiguration;
import io.bitcoinsv.headerSV.core.service.storage.impl.StorageServiceImpl;
import io.bitcoinsv.jcl.net.protocol.wrapper.P2P;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.naming.ConfigurationException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author i.fernandez@nchain.com
 *
 * Spring Configuration of all the HeaderSV Services needed to run the REST API. This configuration relies on
 * several properties defined in the application.yaml file (or application[-spring-profile].yaml.
 * Please check the documentation (README.MD file)
 */
@Configuration
public class HeaderSvRestConfig {

    @Bean
    public NetworkConfiguration getNetworkConfiguration(
            @Value("${headersv.network.networkId:}") String networkId,
            @Value("${headersv.network.minPeers:5}") int minPeers,
            @Value("${headersv.network.maxPeers:15}") int maxPeers,
            @Value("${headersv.network.port:-1}") int port,
            @Value("${headersv.network.dns:[]}") String[] dns,
            @Value("${headersv.network.initialConnections}") String[] initialConnections) throws ConfigurationException {
        return new NetworkConfiguration(networkId, minPeers, maxPeers, port, dns, initialConnections);
    }

    @Bean
    public StorageConfiguration getStorageConfiguration(
            NetworkConfiguration networkConfiguration,
            @Value("${headersv.storage.pruning.fork.enabled:false}") boolean forkPruningEnabled,
            @Value("${headersv.storage.pruning.fork.pruneAfterConfirmations:7}") int pruneAfterConfirmations,
            @Value("${headersv.storage.pruning.orphan.enabled:false}") boolean orphanPruningEnabled,
            @Value("${headersv.storage.pruning.orphan.pruneAfterIntervalSeconds:600}") int orphanPruneInterval) {
        return new StorageConfiguration(networkConfiguration.getNetworkParams().getNet(),
                forkPruningEnabled, pruneAfterConfirmations, orphanPruningEnabled, orphanPruneInterval);
    }

    @Bean
    public StorageService getStorageService(NetworkConfiguration networkConfiguration,
                                            StorageConfiguration storageConfiguration) {
        return new StorageServiceImpl(networkConfiguration, storageConfiguration);
    }

    @Bean
    @ConditionalOnProperty(name = "headersv.network.shareP2P", havingValue = "no")
    public NetworkService getNetworkServiceP2PStandalone(NetworkConfiguration networkConfiguration) {
        return new NetworkServiceImpl(networkConfiguration);
    }

    @Bean
    @ConditionalOnProperty(name = "headersv.network.shareP2P", havingValue = "yes")
    public NetworkService getNetworkServiceP2PShared(NetworkConfiguration networkConfiguration, P2P p2p) {
        return new NetworkServiceImpl(networkConfiguration, p2p);
    }

    @Bean
    public HeaderSvConfig getHeaderSvConfig(
            @Value("${headersv.general.timeoutToTriggerSyncCompleteInSecs:60}") int timeoutToTriggerSyncCompleteInSecs,
            @Value("${headersv.general.headersToIgnore}") List<String> headersToIgnore) {
        return HeaderSvConfig.builder()
                .timeoutToTriggerSyncComplete(Duration.ofSeconds(timeoutToTriggerSyncCompleteInSecs))
                .headersToIgnore(headersToIgnore.stream().collect(Collectors.toSet()))
                .build();
    }

    @Bean
    public HeaderSvService getHeaderSvService(HeaderSvConfig headerSvConfig,
                                              NetworkService networkService,
                                              StorageService storageService) {
        return new HeadersSvServiceImpl(headerSvConfig, networkService, storageService);
    }

    @Bean
    public HeaderSvApi getHeaderSvApi(HeaderSvService headerSvService) {
        return headerSvService.API();
    }
}
