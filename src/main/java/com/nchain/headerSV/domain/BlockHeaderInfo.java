package com.nchain.headerSV.domain;

import com.nchain.jcl.net.protocol.messages.BlockHeaderMsg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 21/07/2020
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class BlockHeaderInfo {

    List<BlockHeaderMsg> blockHeaderMsgList;


    @Override
    public String toString() {
        return "BlockHeaderInfo{" +
                "blockHeaderMsg size=" + blockHeaderMsgList.size() +

                '}';
    }
}
