package io.bitcoinsv.headerSV.rest.v1.client.rest;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author i.fernandez@nchain.com
 *
 * Definition of all the REST endpoints implemented by the "headersv-rest" module.
 * They are defined here in a single place so they can be used by both the "headersv-rest" and "headersv-rest-client"
 * modules.
 */
public class HeaderSVRestEndpoints {

    // Chain TIps Endpoints:
    public static final String URL_CHAIN                            = "/api/v1/chain";
    public static final String URL_CHAIN_TIPS                       = "/tips";
    public static final String URL_CHAIN_TIPS_PRUNE                 = "/tips/prune/{hash}";

    // Block Header Endpoints:
    public static final String URL_CHAIN_HEADER                     = "/api/v1/chain/header";
    public static final String URL_CHAIN_HEADER_HASH                = "/{hash}";
    public static final String URL_CHAIN_HEADER_HASH_ANCESTORS_DEPRECATED  = "/{hash}/ancestors";
    public static final String URL_CHAIN_HEADER_HASH_ANCESTORS      = "/{hash}/{ancestorHash}/ancestors";
    public static final String URL_CHAIN_HEADER_COMMON_ANCESTORS    = "/commonAncestor";
    public static final String URL_CHAIN_HEADER_STATE               = "/state/{hash}";
    public static final String URL_CHAIN_HEADER_BYHEIGHT            = "/byHeight";

    // Network Endpoints:
    public static final String URL_NETWORK                          = "/api/v1/network";
    public static final String URL_NETWORK_PEERS                    = "/peers";
    public static final String URL_NETWORK_PEERS_COUNT              = "/peers/count";
}
