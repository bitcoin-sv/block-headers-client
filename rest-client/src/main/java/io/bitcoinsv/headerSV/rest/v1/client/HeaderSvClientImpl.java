package io.bitcoinsv.headerSV.rest.v1.client;

import io.bitcoinsv.headerSV.rest.common.RestServiceGenerator;
import io.bitcoinsv.headerSV.rest.v1.client.domain.BlockHeaderDTO;
import io.bitcoinsv.headerSV.rest.v1.client.domain.ChainStateDTO;
import io.bitcoinsv.headerSV.rest.v1.client.domain.PeerAddressDTO;
import io.bitcoinsv.headerSV.rest.v1.client.rest.HeaderSvRestClient;
import okhttp3.RequestBody;
import org.springframework.http.MediaType;
import retrofit2.Call;

import java.util.List;

/**
 * @author i.fernandez@nchain.com
 * Copyright (c) 2018-2020 nChain Ltd
 * @date 31/12/2021
 */
public class HeaderSvClientImpl implements HeaderSvClient {

    private String hsvApiUrl;
    private HeaderSvRestClient restClient;

    /** Constructor */
    public HeaderSvClientImpl(String hsvApiUrl) {
        this.hsvApiUrl = hsvApiUrl;
        this.restClient = RestServiceGenerator.createService(HeaderSvRestClient.class, hsvApiUrl);
    }

    @Override
    public List<ChainStateDTO> getTips() {
        return RestServiceGenerator.executeSync(restClient.getTips());
    }

    @Override
    public void pruneChain(String blockHash) {
        RestServiceGenerator.executeSync(restClient.pruneChain(blockHash));
    }

    @Override
    public BlockHeaderDTO getHeader(String blockHash) {
        return RestServiceGenerator.executeSync((Call<? extends BlockHeaderDTO>)restClient.getHeader(blockHash, MediaType.APPLICATION_JSON));
    }

    @Override
    public List<BlockHeaderDTO> getAncestors(String blockHash, String ancestorHash) {
        return RestServiceGenerator.executeSync(restClient.getAncestors(blockHash, ancestorHash));
    }

    @Override
    public BlockHeaderDTO getCommonAncestor(List<String> blockHashes) {
        return RestServiceGenerator.executeSync(restClient.getCommonAncestor(blockHashes));
    }

    @Override
    public ChainStateDTO getHeaderDetails(String blockHash) {
        return RestServiceGenerator.executeSync(restClient.getHeaderDetails(blockHash));
    }

    @Override
    public List<BlockHeaderDTO> getHeadersByHeight(int height, int count) {
        return RestServiceGenerator.executeSync((Call<List<BlockHeaderDTO>>)restClient.getHeadersByHeight(
                height,
                count,
                MediaType.APPLICATION_JSON));
    }

    @Override
    public List<PeerAddressDTO> getConnectedPeers() {
        return RestServiceGenerator.executeSync(restClient.getConnectedPeers());
    }

    @Override
    public int getConnectedPeersCount() {
        return RestServiceGenerator.executeSync(restClient.getConnectedPeersCount());
    }
}
