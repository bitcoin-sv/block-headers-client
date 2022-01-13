package io.bitcoinsv.headerSV.rest.v1.client.rest;

import io.bitcoinsv.headerSV.rest.v1.client.domain.BlockHeaderDTO;
import io.bitcoinsv.headerSV.rest.v1.client.domain.ChainStateDTO;
import io.bitcoinsv.headerSV.rest.v1.client.domain.PeerAddressDTO;
import okhttp3.RequestBody;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

/**
 * @author i.fernandez@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 31/12/2021
 */
public interface HeaderSvRestClient {
    @GET(value = (HeaderSVRestEndpoints.URL_CHAIN + HeaderSVRestEndpoints.URL_CHAIN_TIPS))
    Call<List<ChainStateDTO>> getTips();

    @GET(value = (HeaderSVRestEndpoints.URL_CHAIN + HeaderSVRestEndpoints.URL_CHAIN_TIPS_PRUNE))
    Call<Void> pruneChain(@Path("hash") String hash);

    @GET(value = (HeaderSVRestEndpoints.URL_CHAIN_HEADER + HeaderSVRestEndpoints.URL_CHAIN_HEADER_HASH))
    Call<BlockHeaderDTO> getHeader(@Path("hash") String hash, @Header("Content-Type") MediaType contentType);

    @Deprecated
    @POST(value = (HeaderSVRestEndpoints.URL_CHAIN_HEADER + HeaderSVRestEndpoints.URL_CHAIN_HEADER_HASH_ANCESTORS_DEPRECATED))
    Call<List<BlockHeaderDTO>> getAncestorsDeprecated(@Path("hash") String hash, @Body RequestBody ancestorHash);

    @GET(value = (HeaderSVRestEndpoints.URL_CHAIN_HEADER + HeaderSVRestEndpoints.URL_CHAIN_HEADER_HASH_ANCESTORS))
    Call<List<BlockHeaderDTO>> getAncestors(@Path("hash") String hash, @Path("ancestorHash") String ancestorHash);

    @POST(value = (HeaderSVRestEndpoints.URL_CHAIN_HEADER + HeaderSVRestEndpoints.URL_CHAIN_HEADER_COMMON_ANCESTORS))
    Call<BlockHeaderDTO> getCommonAncestor(@Body List<String> blockHashes);

    @GET(value = (HeaderSVRestEndpoints.URL_CHAIN_HEADER + HeaderSVRestEndpoints.URL_CHAIN_HEADER_STATE))
    Call<ChainStateDTO> getHeaderDetails(@Path("hash") String hash);

    @GET(value = (HeaderSVRestEndpoints.URL_CHAIN_HEADER + HeaderSVRestEndpoints.URL_CHAIN_HEADER_BYHEIGHT))
    Call<List<BlockHeaderDTO>> getHeadersByHeight(@Query("height") Integer height, @Query("count") Integer count,
                               @Header("Accept") MediaType acceptContentType);

    @GET(value = (HeaderSVRestEndpoints.URL_NETWORK + HeaderSVRestEndpoints.URL_NETWORK_PEERS))
    Call<List<PeerAddressDTO>> getConnectedPeers();

    @GET(value = (HeaderSVRestEndpoints.URL_NETWORK + HeaderSVRestEndpoints.URL_NETWORK_PEERS_COUNT))
    Call<Integer> getConnectedPeersCount();

    }
