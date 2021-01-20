package com.nchain.headerSV.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2021 nChain Ltd
 * @date 15/01/2021
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlockHeaderStateDTO {
    private BlockHeaderDTO blockHeader;
    private ChainStateDTO chainState;

}
