package com.nchain.headerSV;

import com.nchain.headerSV.service.listener.ListenerService;
import com.nchain.headerSV.service.propagation.PropagationDBService;
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
	private ListenerService listenerService;

	@Autowired
	private PropagationDBService propagationDBService;

	@EventListener
	public void onStart(ApplicationReadyEvent event) {
		propagationDBService.start();
		listenerService.start();

	}

	@PreDestroy
	public void onStop() {
		listenerService.stop();
		propagationDBService.stop();
	}

}
