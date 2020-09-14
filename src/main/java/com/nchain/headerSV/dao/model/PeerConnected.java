package com.nchain.headerSV.dao.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 *   @author m.jose
 *
 *  Copyright (c) 2018-2020 nChain Ltd
 *  @date 14/09/2020, 17:03
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PeerConnected {
    Integer peerCount;
}
