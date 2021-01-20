package com.nchain.headerSV.domain.dto;

import com.nchain.jcl.base.domain.api.base.BlockHeader;
import com.nchain.jcl.net.network.PeerAddress;
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
public class PeerAddressDTO {
    private String ip;
    private int port;

    public static PeerAddressDTO of(PeerAddress peerAddress) {
        return PeerAddressDTO.builder()
                .ip(peerAddress.getIp().toString())
                .port(peerAddress.getPort())
                .build();
    }
}
