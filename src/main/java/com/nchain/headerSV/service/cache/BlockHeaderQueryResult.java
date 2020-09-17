package com.nchain.headerSV.service.cache;

import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author m.jose@nchain.com
  * Copyright (c) 2018-2020 nChain Ltd
 * @date 12/08/2020
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlockHeaderQueryResult {
    private BlockHeader blockHeader;
    private String state;
    private Double work;
    private Double cumulativeWork;
    private Integer height;
    private boolean bestChain;
    private Integer confirmations;
    private Long chainConfidence;
}
