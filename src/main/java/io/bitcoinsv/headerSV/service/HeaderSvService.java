package io.bitcoinsv.headerSV.service;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
public interface HeaderSvService {
    /** Starts the Listener */
    void start();
    /** Stops the Listener */
    void stop();
}
