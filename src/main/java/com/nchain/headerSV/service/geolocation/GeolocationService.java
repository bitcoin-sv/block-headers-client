package com.nchain.headerSV.service.geolocation;


import com.nchain.headerSV.domain.PeerLocationInfo;
import com.nchain.jcl.net.network.PeerAddress;

import java.util.Optional;

/**
 * @author i.fernandez@nchain.com
 * Copyright (c) 2018-2019 Bitcoin Association
  * Copyright (c) 2018-2020 nChain Ltd
 * @date 2019-10-10 15:43
 *
 * A Service to retrieve gGeographical location from a Peer in the P2P Netwwork.
 */
public interface GeolocationService {
    /** Returns the GeoLocation of this Peer based on his Network aDdress, if possible */
    Optional<PeerLocationInfo> geoLocate(PeerAddress peerAddress);
}
