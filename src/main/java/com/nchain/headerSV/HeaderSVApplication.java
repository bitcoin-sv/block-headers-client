package com.nchain.headerSV;

import com.nchain.headerSV.service.ListenerService;
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

	@EventListener
	public void onStart(ApplicationReadyEvent event) {
		listenerService.start();
	}

	@PreDestroy
	public void onStop() {
		listenerService.stop();
	}

//	public void run(String... args) throws Exception {
//
//		RuntimeConfig runtimeConfig = RuntimeConfigAutoImpl.builder().build();
//		NetConfig netConfig = new NetLocalDevConfig().toBuilder()
//				.maxSocketConnections(OptionalInt.of(100))
//				.build();
//
//		ProtocolConfig protocolConfig = new ProtocolBSVMainConfig().toBuilder()
//				.handshakeMaxPeers(OptionalInt.of(100))
//				.discoveryMethod(ProtocolConfig.DiscoveryMethod.DNS)
//				.discoveryCheckingPeerReachability(false)
//				.build();
//		AtomicInteger numPeersConnected = new AtomicInteger();
//
////
////		// Server definition: Our Basic ProtocolHandler
////		ConnectionHandler server = new ConnectionHandlerImpl("myServer", runtimeConfig, netConfig, protocolConfig, true);
////
////
////		AtomicInteger numPeersConnected = new AtomicInteger();
////		server.addCallback((PeerConnectedListener) (peer -> numPeersConnected.incrementAndGet()) );
////
////
////		server.start();
////		for ( String dns:  protocolConfig.getDiscoveryDnsSeeds()) {
////			try {
////				PeerAddress[] peerAddresses = PeerAddress.fromHostName(dns, protocolConfig.getPort());
////				for (PeerAddress peerAddress : peerAddresses) server.connect(peerAddress);
////			} catch (UnknownHostException e) {}
////		}
////
////		// We wait a bit until the connections are established...
////		Thread.sleep(5000);
////		server.stop();
////
////		System.out.println("\n\nSummary:"+ numPeersConnected.get() +" Handshakes performed:");
////		System.out.println("");
//
//		// FileUtils:
//		FileUtils fileUtils = new ClasspathFileUtils(this.getClass().getClassLoader());
//
//
//
//		// Handlers configurations:
//
//		Map<PeerAddress, VersionMsg> handshakes = new HashMap<>();
//		List<PeerAddress> peerAddressList = new LinkedList<>();
////		SetupHandlersBuilder.HandlersSetup protocolHandler = SetupHandlersBuilder
////				.newSetup("myServer", runtimeConfig, netConfig, protocolConfig )
////				.handlers()
////				.useFileUtils(fileUtils)
////				.custom()
////				.addCallback((PeerHandshakeAcceptedListener) handshakes::put).done();
//
//		SetupHandlersBuilder.HandlersSetup protocolHandler = SetupHandlersBuilder.newSetup()
//				.config()
//				.id("HeaderSv")
//				.runtime(runtimeConfig)
//				.network(netConfig)
//				.protocol(protocolConfig)
//				.handlers()
//				.useFileUtils(fileUtils)
//				.custom()
//			//
//			//	.addCallback((PeerConnectedListener)  (peer -> peerAddressList.add(peer)))
//				.addCallback((PeerHandshakeAcceptedListener) handshakes::put)
//			.done();
//
//		protocolHandler.start();
//		//		// We wait a bit until the connections are established...
//		Thread.sleep(5000);
//		protocolHandler.stop();
//
//
//		System.out.println("\n\nSummary:"+ handshakes.size() +" Handshakes performed:");
//		System.out.println("");
//		handshakes.keySet().stream().forEach(
//				peerAddress -> {
//					VersionMsg versionMsg = handshakes.get(peerAddress);
//					System.out.println("IP :"+peerAddress.toString()+":"+versionMsg.getVersion()+":"+ versionMsg.getUser_agent());
//				}
//		);
//
//	}



}
