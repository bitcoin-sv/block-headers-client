package io.bitcoinsv.headerSV.app;


import io.bitcoinsv.headerSV.core.HeaderSvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.PreDestroy;

/**
 * Distributed under the Open BSV software license, see the accompanying file LICENSE
 * Copyright (c) 2020 Bitcoin Association
 *
 * @author m.fletcher@nchain.com
 * @author i.fernandez@nchain.com
 *
 * Header SV Application. It runs as a standalone Spring Boot App. It only contains the Header Sv functionality
 * contained in the "headersv-core" module, and exposed as a rest APi by the "headersv-rest" module.
 */
@SpringBootApplication
public class HeaderSVApplication {

	public static void main(String[] args) {
		System.out.println("HeaderSV Listener 2.0.0");
		SpringApplication.run(HeaderSVApplication.class, args);
	}

	@Autowired
	private HeaderSvService headerSvService;


	@EventListener
	public void onStart(ApplicationReadyEvent event) {
		headerSvService.start();
	}

	@PreDestroy
	public void onStop() {
		headerSvService.stop();
	}
}
