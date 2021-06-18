package io.bitcoinsv.headerSV;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import io.bitcoinsv.headerSV.service.network.NetworkService;
import io.bitcoinsv.headerSV.service.sync.BlockHeaderSyncService;

import javax.annotation.PreDestroy;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.jose@nchain.com
 */
@SpringBootApplication
public class HeaderSVApplication {

	public static void main(String[] args) {
		System.out.println("HeaderSV Listener 1.0.");
		SpringApplication.run(HeaderSVApplication.class, args);
	}

	@Autowired
	private NetworkService networkService;


	@Autowired
	private BlockHeaderSyncService blockHeadersSyncService;


	@EventListener
	public void onStart(ApplicationReadyEvent event) {
		blockHeadersSyncService.start();
		networkService.start();
	}

	@PreDestroy
	public void onStop() {
		networkService.stop();
		blockHeadersSyncService.stop();
	}

}
