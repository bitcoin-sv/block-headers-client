package com.nchain.headerSV.domain;

import com.nchain.jcl.network.PeerAddress;
import com.nchain.jcl.protocol.messages.VersionMsg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Optional;

/**
 * @author i.fernandez@nchain.com
 * Copyright (c) 2018-2019 Bitcoin Association
 * Distributed under the Open BSV software license, see the accompanying file LICENSE.
 * @date 2019-10-10 15:39
 *
 * Information to store for each Peer in the P2P Network.
 * This is a real-time information.
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class PeerInfo {
    private PeerAddress peerAddress;
    private VersionMsg versionMsg;
    private Optional<PeerLocationInfo> location;
    private boolean  peerConnectedStatus;

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(peerAddress.toString());
        result.append((location.isPresent())? location : "(location not available)");
        result.append("connectionstatus:"+this.peerConnectedStatus);
        return result.toString();
    }
}
