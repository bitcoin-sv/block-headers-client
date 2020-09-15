package com.nchain.headerSV;

import com.nchain.headerSV.service.cache.BlockHeaderCacheService;
import com.nchain.headerSV.service.network.NetworkService;
import com.nchain.headerSV.service.propagation.PropagationDBService;
import com.nchain.headerSV.service.sync.BlockHeaderSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.PreDestroy;

/**
 * @author m.jose@nchain.com
 * Copyright (c) 2009-2010 Satoshi Nakamoto
 * Copyright (c) 2009-2016 The Bitcoin Core developers
  * Copyright (c) 2018-2020 nChain Ltd
 * @date 10/09/2020
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
	private PropagationDBService propagationDBService;

	@Autowired
	private BlockHeaderCacheService blockHeaderCacheService;

	@Autowired
	private BlockHeaderSyncService blockHeadersSyncService;


	@EventListener
	public void onStart(ApplicationReadyEvent event) {
		propagationDBService.start();
		blockHeaderCacheService.start();
		blockHeadersSyncService.start();
		networkService.start();
	}

	@PreDestroy
	public void onStop() {
		networkService.stop();
		blockHeaderCacheService.stop();
		blockHeadersSyncService.stop();
		propagationDBService.stop();
	}

}
