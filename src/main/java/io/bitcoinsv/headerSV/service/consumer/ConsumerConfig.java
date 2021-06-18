package io.bitcoinsv.headerSV.service.consumer;

import lombok.Builder;
import lombok.Data;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
@Data
@Builder
public class ConsumerConfig {
    boolean sendDuplicates;
    boolean requiresMinimumPeers;
}
