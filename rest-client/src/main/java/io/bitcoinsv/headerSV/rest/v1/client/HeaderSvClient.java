package io.bitcoinsv.headerSV.rest.v1.client;

import io.bitcoinsv.headerSV.rest.v1.client.domain.BlockHeaderDTO;
import io.bitcoinsv.headerSV.rest.v1.client.domain.ChainStateDTO;
import io.bitcoinsv.headerSV.rest.v1.client.domain.PeerAddressDTO;

import java.util.List;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author i.fernandez@nchain.com
 *
 * HeaderSV Client Interface. Implements a HTTP Proxy over the HeaderSV REST API v1.
 * NOTE: All the "hash" parameters in this inrerface are in HEX human-readable format.
 */
public interface HeaderSvClient {

    /**
     * Return the list of the current Tips of the Chain. There might be more than one if there are forks
     * @return List of Tips (or an empty one)
     */
    List<ChainStateDTO> getTips();

    /**
     * It prunes a branch/fork, starting from the block hash given
     * @param blockHash parent hash of the branch to remove
     */
    void pruneChain(String blockHash);

    /**
     * Returns info about a Block, or nul if it doesn't exist (or the service hasn't fully synchronized yet).
     * If the result is NULL it might mean 2 different scenarios:
     *  - The block is not in the chain (hash might be wrong, or it might belong to another chain/network)
     *  - The HeaderSV Service is still synchronizing and this block is new, so the service hasn't caught u with it
     *    yet. This situation is rare and it should resolve in the next seconds.
     *
     * @param blockHash Block Hash
     * @return details about the Blocks, or null.
     */
    BlockHeaderDTO getHeader(String blockHash);

    /**
     * It returns a list of ancestor sit between 2 blocks
     * @param blockHash
     * @param ancestorHash
     */
    List<BlockHeaderDTO> getAncestors(String blockHash, String ancestorHash);

    /**
     * It returns the common ancestors (root) of the blocks given
     * @param blockHashes List of blocks
     * @return  commons ancestor or null if there is none.
     */
    BlockHeaderDTO getCommonAncestor(List<String> blockHashes);

    /**
     * Get details about a Blocks and information about its place in the chain.
     * If the result is NULL it might mean 2 different scenarios:
     *  - The block is not in the chain (hash might be wrong, or it might belong to another chain/network)
     *  - The HeaderSV Service is still synchronizing and this block is new, so the service hasn't caught u with it
     *    yet. This situation is rare and it should resolve in the next seconds.
     *
     * @param blockHash block Hash
     * @return block details, or null.
     */
    ChainStateDTO getHeaderDetails(String blockHash);

    /**
     * Returns a list of block, starting from the height given and up to "counter" (2000 nax).
     *
     * @param height starting height
     * @param count number of Blocks to return
     * @return List of Block Headers info.
     */
    List<BlockHeaderDTO> getHeadersByHeight(int height, int count);

    /**
     * It returns the complete list of Peers the HeaderSV Service is currently connected to.
     * @return List of Peer info
     */
    List<PeerAddressDTO> getConnectedPeers();

    /**
     * Returns the total number of current Peers the HEaerSV Service is connected to
     * @return number of Peers
     */
    int getConnectedPeersCount();
}
