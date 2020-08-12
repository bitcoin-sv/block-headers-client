package com.nchain.headerSV;

import com.nchain.headerSV.service.cache.BlockHeaderCacheService;
import com.nchain.headerSV.service.network.NetworkService;
import com.nchain.headerSV.service.propagation.PropagationDBService;
import com.nchain.headerSV.service.sync.BlockHeadersSyncServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.PreDestroy;

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
	private BlockHeadersSyncServiceImpl blockHeadersSyncService;


	@EventListener
	public void onStart(ApplicationReadyEvent event) {
		propagationDBService.start();
		networkService.start();
		blockHeaderCacheService.start();
		blockHeadersSyncService.start();
	}

	@PreDestroy
	public void onStop() {
		networkService.stop();
		propagationDBService.stop();
		blockHeaderCacheService.stop();
		blockHeadersSyncService.stop();
	}

}
