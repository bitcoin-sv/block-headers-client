package com.nchain.headerSV.service.cache.cached;

import com.nchain.headerSV.dao.postgresql.domain.BlockHeader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 07/08/2020
 */
@Builder
@AllArgsConstructor
@Getter
public class CachedHeader {
    private BlockHeader blockHeader;
    private String branchId;
    private double work;
    private int height;
}
