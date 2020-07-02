package com.nchain.headerSV.service.listener;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 03/06/2020
 */
public interface ListenerService {
    /** Starts the Listener */
    void start();
    /** Stops the Listener */
    void stop();
}
