package io.bitcoinsv.headerSV.rest.v1.controller;

import io.bitcoinsv.headerSV.core.api.HeaderSvApi;
import io.bitcoinsv.headerSV.rest.v1.client.rest.HeaderSVRestEndpoints;
import io.bitcoinsv.headerSV.rest.v1.client.domain.PeerAddressDTO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
@RestController
//@RequestMapping("/api/v1/network")
@RequestMapping(value = HeaderSVRestEndpoints.URL_NETWORK)
public class NetworkControllerV1 {

    HeaderSvApi headerSvApi;

    public NetworkControllerV1(HeaderSvApi blockChainFacade) {
        this.headerSvApi = blockChainFacade;
    }

    //@RequestMapping("/peers")
    @RequestMapping(value = HeaderSVRestEndpoints.URL_NETWORK_PEERS)
    public List<PeerAddressDTO> getConnectedPeers() {
        return headerSvApi.getConnectedPeers().stream().map(PeerAddressDTO::of).collect(Collectors.toList());
    }

    //@RequestMapping("/peers/count")
    @RequestMapping(value = HeaderSVRestEndpoints.URL_NETWORK_PEERS_COUNT)
    public int getConnectedPeersCount() {
        return headerSvApi.getConnectedPeers().size();
    }

}
