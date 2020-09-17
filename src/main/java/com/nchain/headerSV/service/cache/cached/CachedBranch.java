package com.nchain.headerSV.service.cache.cached;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 06/08/2020
 */
@Builder
@AllArgsConstructor
@Getter
@Setter
public class CachedBranch {
    private String id;
    private String parentBranchId;
    private String leafNode;
    private Double work;
    private Integer height;
    private Long confidence;
}
