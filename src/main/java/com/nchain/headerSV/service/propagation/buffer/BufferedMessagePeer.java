package com.nchain.headerSV.service.propagation.buffer;

import com.nchain.headerSV.domain.PeerInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 01/07/2020
 */
@Data
@AllArgsConstructor
public class BufferedMessagePeer implements BufferedMessage {
    PeerInfo peerInfo;

    @Override
    public String toString() {
        return "BufferedMessagePeer{" +
                "peerInfo=" + peerInfo +
                '}';
    }

}
