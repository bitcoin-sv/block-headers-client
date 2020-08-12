package com.nchain.headerSV.service.propagation.persistence;


import com.nchain.headerSV.dao.model.PeerDTO;
import com.nchain.headerSV.domain.PeerInfo;
import com.nchain.headerSV.service.geolocation.GeolocationService;
import com.nchain.headerSV.service.propagation.buffer.BufferedBlockHeaders;
import com.nchain.headerSV.service.propagation.buffer.BufferedMessage;
import com.nchain.headerSV.service.propagation.buffer.BufferedMessagePeer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
 * Copyright (c) 2018-2020 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 */
@Service
@AllArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class BufferedMessagePersistenceService {
    public final PeerPersistence peerPersistence;
    public final GeolocationService geoService;
    public final BlockHeaderPersistence blockHeaderPersistence;

    public void persist(BufferedMessage bufferedMessage) {
        if (bufferedMessage instanceof BufferedMessagePeer) {
            process((BufferedMessagePeer) bufferedMessage);

        } else if(bufferedMessage instanceof BufferedBlockHeaders) {
            process((BufferedBlockHeaders) bufferedMessage);
        }
    }

    private void process(BufferedMessagePeer bufferedMessage) {
        PeerInfo peerInfo = bufferedMessage.getPeerInfo();
        // Geo locate
        peerInfo.setLocation(geoService.geoLocate(peerInfo.getPeerAddress()));
        final PeerDTO peerDTO = PeerDTO.builder()
                .address(peerInfo.getPeerAddress().toStringWithoutPort())
                .port(peerInfo.getPeerAddress().getPort())
                .connectionStatus(peerInfo.isPeerConnectedStatus())
                .build();
        if (peerInfo.getVersionMsg() != null) {

            peerDTO.setProtocolVersion((int) peerInfo.getVersionMsg().getVersion());
            peerDTO.setUserAgent(peerInfo.getVersionMsg().getUser_agent().getStr());
            peerDTO.setServices(peerInfo.getVersionMsg().getServices());
        }

        peerInfo.getLocation().ifPresent(location -> {
            peerDTO.setCity(location.getCity());
            peerDTO.setCountry(location.getCountry());
            peerDTO.setZipcode(location.getZipcode());
        });
        peerPersistence.persist(peerDTO);
    }

    private void process(BufferedBlockHeaders bufferedBlockHeader) {
      bufferedBlockHeader.getBlockHeaderAddrInfos().forEach(blockHeaderPersistence::persist);
    }


    public void stop() {
        peerPersistence.flush();
        blockHeaderPersistence.flush();
    }
}
