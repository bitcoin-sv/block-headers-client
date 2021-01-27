package com.nchain.headerSV.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2021 nChain Ltd
 * @date 15/01/2021
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChainStateDTO {
    private BlockHeaderDTO header;
    private String state;
    private BigInteger chainWork;
    private int height;
    private int confirmations;
    private String genesis;
}
