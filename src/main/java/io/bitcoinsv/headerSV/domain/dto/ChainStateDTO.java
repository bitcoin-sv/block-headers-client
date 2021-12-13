package io.bitcoinsv.headerSV.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChainStateDTO {
    private BlockHeaderDTO header;
    private String state;
    private BigInteger chainWork;
    private Integer height;
    private Integer confirmations;
}
