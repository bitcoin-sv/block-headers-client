package io.bitcoinsv.headerSV.domain.dto;

import com.nchain.jcl.net.network.PeerAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
