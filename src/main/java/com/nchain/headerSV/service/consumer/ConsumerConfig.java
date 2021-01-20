package com.nchain.headerSV.service.consumer;

import lombok.Builder;
import lombok.Data;

/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2021 nChain Ltd
 * @date 18/01/2021
 */
@Data
@Builder
public class ConsumerConfig {
    boolean sendDuplcates;
    boolean requiresMinimumPeers;
}
