package integration.io.bitcoinsv.headerSV.rest.v1.client;

import io.bitcoinsv.headerSV.rest.v1.client.HeaderSvClient;
import io.bitcoinsv.headerSV.rest.v1.client.HeaderSvClientImpl;
import io.bitcoinsv.headerSV.rest.v1.client.domain.BlockHeaderDTO;
import io.bitcoinsv.headerSV.rest.v1.client.domain.ChainStateDTO;
import io.bitcoinsv.headerSV.rest.v1.client.domain.PeerAddressDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author i.fernandez@nchain.com
 *
 * Integration Test of the headersv-rest-client module. it tests that it can conncet to an external Headersv-app
 * application and al the endpoints work properly.
 *
 * IMPORTANT: This Test relies on an external headersv-App, which URL needs to be defined in the "hsvAppUrl" property.
 */
public class HeaderSvClientTest {

    // HeaderSv App URL
    private String hsvAppUrl = "http://localhost:8080";

    // Time to wait before we make the cal to HeaderSv app ( to gibe it some time to sync some headers):
    private Duration WAITING_TIME = Duration.ofSeconds(5);

    //@Test
    public void testGetTips() {
        try {
            HeaderSvClient client = new HeaderSvClientImpl(hsvAppUrl);
            // We just wait a few seconds, to give the HSV App some time to sync some headers...
            Thread.sleep(WAITING_TIME.toMillis());

            // We get info about the Tips
            List<ChainStateDTO> tips = client.getTips();

            assertTrue(tips != null);
            assertTrue(!tips.isEmpty());

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    //@Test
    public void testGetHeader() {
        try {
            HeaderSvClient client = new HeaderSvClientImpl(hsvAppUrl);
            // We just wait a few seconds, to give the HSV App some time to sync some headers...
            Thread.sleep(WAITING_TIME.toMillis());

            // We get info about the current tips
            List<ChainStateDTO> tips = client.getTips();
            assertTrue(tips != null);
            assertTrue(!tips.isEmpty());

            // and then we get detailed info for one of them:
            BlockHeaderDTO headerDTO = client.getHeader(tips.get(0).getHeader().getHash());
            assertTrue(headerDTO != null);
            assertTrue(headerDTO.getHash().equalsIgnoreCase(tips.get(0).getHeader().getHash()));

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    //@Test
    public void testGetHeaderDetails() {
        try {
            HeaderSvClient client = new HeaderSvClientImpl(hsvAppUrl);
            // We just wait a few seconds, to give the HSV App some time to sync some headers...
            Thread.sleep(WAITING_TIME.toMillis());

            // We get info about the current tips
            List<ChainStateDTO> tips = client.getTips();
            assertTrue(tips != null);
            assertTrue(!tips.isEmpty());

            // and then we get detailed info for one of them:
            ChainStateDTO chainStateDTO = client.getHeaderDetails(tips.get(0).getHeader().getHash());
            assertTrue(chainStateDTO != null);
            assertTrue(chainStateDTO.getHeader().getHash().equalsIgnoreCase(tips.get(0).getHeader().getHash()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void testGetHeadersByHeight() {
        try {
            HeaderSvClient client = new HeaderSvClientImpl(hsvAppUrl);
            // We just wait a few seconds, to give the HSV App some time to sync some headers...
            Thread.sleep(WAITING_TIME.toMillis());

            // We get info about the current tips
            List<ChainStateDTO> tips = client.getTips();
            assertTrue(tips != null);
            assertTrue(!tips.isEmpty());

            // and then we a list of headers based on height. We use a low heigh so we can be fairly sure that the
            // headerSv-App has sync to that point:
            int height = 6000;
            int count = 500;
            List<BlockHeaderDTO> headers = client.getHeadersByHeight(height, count);
            assertTrue(headers != null);
            assertTrue(headers.size() == count);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    //@Test
    public void testGetAncestors() {
        try {
            HeaderSvClient client = new HeaderSvClientImpl(hsvAppUrl);
            // We just wait a few seconds, to give the HSV App some time to sync some headers...
            Thread.sleep(WAITING_TIME.toMillis());

            // We get a Header (from one of the tips)
            List<ChainStateDTO> tips = client.getTips();
            BlockHeaderDTO parent = tips.get(0).getHeader();

            // Now we wait and we get another, which will be a "descendent" of the previous one:
            Thread.sleep(3_000);
            List<ChainStateDTO> tipsChildren = client.getTips();
            BlockHeaderDTO child = tipsChildren.get(0).getHeader();

            // Now we get the ancestors in between:
            List<BlockHeaderDTO> ancestors = client.getAncestors(child.getHash(), parent.getHash());

            assertTrue(ancestors != null);
            assertTrue(!ancestors.isEmpty());

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    // THIS TESTS NEEDS SOME RE-WORKING
    //@Test
    public void testGetCommonAncestors() {
        try {
            HeaderSvClient client = new HeaderSvClientImpl(hsvAppUrl);
            // We just wait a few seconds, to give the HSV App some time to sync some headers...
            Thread.sleep(WAITING_TIME.toMillis());

            // We get the COMMON Ancestor:
            List<ChainStateDTO> tips = client.getTips();
            BlockHeaderDTO parent = tips.get(0).getHeader();

            // We get several CHILDREN, each one separated by some delay, the COMMON ancestor of them all should be
            // the PARENT already captured above:
            final int NUM_DESCENDENTS = 3;
            List<String> descendentHashes = new ArrayList<>();
            for (int i = 0; i < NUM_DESCENDENTS; i++) {
                Thread.sleep(3_000);
                List<ChainStateDTO> tipsChildren = client.getTips();
                BlockHeaderDTO child = tipsChildren.get(0).getHeader();
                descendentHashes.add(child.getHash());
            }

            // Now we calculate the COMMON Ancestors:
            BlockHeaderDTO commonAncestorDTO = client.getCommonAncestor(descendentHashes);

            assertTrue(commonAncestorDTO != null);
            //assertTrue(commonAncestorDTO.getHash().equalsIgnoreCase(parent.getHash()));

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    //@Test
    public void testGetConnectedPeersAndCount() {
        try {
            HeaderSvClient client = new HeaderSvClientImpl(hsvAppUrl);
            // We just wait a few seconds, to give the HSV App some time to sync some headers...
            Thread.sleep(WAITING_TIME.toMillis());

            // We get info about the current connected Peers:
            List<PeerAddressDTO> peers = client.getConnectedPeers();
            int peersCount = client.getConnectedPeersCount();

            assertTrue(peers != null);
            assertTrue(peers.size() == peersCount);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
