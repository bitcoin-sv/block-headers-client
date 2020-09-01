package com.nchain.headerSV.service.propagation.buffer;

import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;
import java.util.List;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 21/07/2020
 */
@Data
@AllArgsConstructor
public class BufferedBlockHeaders implements BufferedMessage {
    List<BlockHeader> blockHeaders;

    @Override
    public String toString() {
        return "BufferedBlockHeader{" +
                "blockHeaderInfo=" + blockHeaders.toString() +
                '}';
    }
}
