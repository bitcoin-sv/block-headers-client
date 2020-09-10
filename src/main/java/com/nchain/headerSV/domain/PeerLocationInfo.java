package com.nchain.headerSV.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author i.fernandez@nchain.com
 * Copyright (c) 2018-2019 Bitcoin Association
  * Copyright (c) 2018-2020 nChain Ltd
 * @date 2019-10-10 15:44
 *
 * Peer Geo Location information.
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class PeerLocationInfo {
    private String city;
    private String country;
    private String zipcode;

    @Override
    public String toString() {
        return city + "[" + zipcode + "], " + country;
    }

}
