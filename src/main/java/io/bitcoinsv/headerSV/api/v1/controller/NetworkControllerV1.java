package io.bitcoinsv.headerSV.api.v1.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.bitcoinsv.headerSV.api.HSVFacade;
import io.bitcoinsv.headerSV.domain.dto.PeerAddressDTO;

import java.util.List;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2021 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 */
@RestController
@RequestMapping("/api/v1/network")
public class NetworkControllerV1 {

    HSVFacade hsvFacade;

    public NetworkControllerV1(HSVFacade blockChainFacade) {
        this.hsvFacade = blockChainFacade;
    }

    @RequestMapping("/peers")
    public List<PeerAddressDTO> getConnectedPeers() {
        return hsvFacade.getConnectedPeers();
    }

    @RequestMapping("/peers/count")
    public int getConnectedPeersCount() {
        return hsvFacade.getConnectedPeers().size();
    }

}
