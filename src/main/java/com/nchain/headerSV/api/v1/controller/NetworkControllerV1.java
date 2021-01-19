package com.nchain.headerSV.api.v1.controller;

import com.nchain.headerSV.domain.dto.PeerAddressDTO;
import com.nchain.headerSV.api.HSVFacade;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author m.fletcher@nchain.com
 * Copyright (c) 2018-2021 nChain Ltd
 * @date 15/01/2021
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
