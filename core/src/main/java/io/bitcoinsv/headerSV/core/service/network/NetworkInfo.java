package io.bitcoinsv.headerSV.core.service.network;

import io.bitcoinsv.bitcoinjsv.params.Net;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author i.fernandez@nchain.com
 *
 * Info about the network returned by the NetworkService
 */
public interface NetworkInfo {
    Net net();
}
