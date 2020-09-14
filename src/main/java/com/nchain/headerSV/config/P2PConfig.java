package com.nchain.headerSV.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 05/06/2020
 */
@ConfigurationProperties(prefix = "headersv.p2p")
@Component
@Setter
@Getter
public class P2PConfig {
    private int maxPeers;
    private int minPeers;
    private boolean relayTxs;
}
