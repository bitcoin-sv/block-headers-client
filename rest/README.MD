# HeaderSV-rest module

The **HeaderSV-rest** module provides a *REST Api* that can be added to your current *SpringBoot Application*, so
you can extend its current functionalities and add the *HeaderSV* ones on top of it.


> This module can NOT be run as a standalone application on its own. Its meant to be used as an *extension* that 
> you can add to your existing *SpringBoot App*. 
> If you do NOT have a current *SpringBoot App*, and you want to run it as a standalone Application, checkout the 
> [**headerSV-app**](app/README.MD) module instead.


## How to use *headerSV-rest* (gradle)

The way to use of **headerSV-rest** depends on your specific scenario, and a little technical description is needed before we can continue:

*headerSV-rest* uses a **NetworkService** to connect to the blockchain network and send/receive messages from other Peers. This service relies on another low-level Service called **P2P**, which is part of the **jcl-net** package. 

In most cases, this information is irrelevant, but in some occasions, the *SpringBoot* App where you want to include **headerSV-rest** is already using the *P2P* Service for other purposes. In that specific sceneario, if you also include *headerSV-rest* you will end up with 2 *P2P* services running in your app at the same time. That's fine and it works (as long as you take care that both *P2P* services are listening to different port numbers), but in same cases you might want to just use one. In that case, you'll have to **SHARE** the *P2P* Service.

So we have 2 Scenarios:

* **Scenario 1**: Your current *SpringBoot* app is NOT currently using the JCL *P2P* Service, or even if its using it, you are fine with 2 *P2P* Services running in the same machine.

* **Scenario 2**: Your current *SpringBoot* is already using a *P2P* Service and you only want one of these services, so in this case you need to *share* it. 

### Scenario 1: 

#### Declare the dependencies

*headerSV-rest* has dependencies on both BitcoinJ-SV and the Java Component Library (JCL), so the best approach is to
define the version numbers of each one in your `gradle.properties` file:

```
# BitcoinJ dependency:
bitcoinJVersion=1.0.2

# JCL dependency:
JCLVersion=2.0.0

# headerSV dependency:
headerSVVersion=2.0.0
```
, and then you reference them in your *build.gradle* file:

```
// HeaderSV Modules
implementation "io.bitcoinsv.headerSV:headerSV-core:$headerSVVersion"
implementation "io.bitcoinsv.headerSV:headerSV-rest:$headerSVVersion"

```

#### Define the Configuration:

The Configuration is defined in 2 levels:

 * In *JAVA* code, to include the new *Beans* provided by *headerSV-rest*
 * In *yaml* config files, to set the value of the configuration properties


In the *Java* code, and assuming that the pre-existing *SpringBoot* app is called "myApp", we need to edit our 
current *Spring Configuration* and add a reference to the *HeaderSvRestConfig*:

```
@Configuration
@Import(HeaderSvRestConfig.class)
@ComponentScan("io.bitcoinsv.headerSV.rest.v1.controller")
public class MyAppConfig {
}
```

The 
*yaml* configuration files should be stored in `src/main/resources/application.yml`. The general structure of the config 
file is like follows:

> **NOTE**: 
> In this scenario, the value of the properly ```headersv.netwrkService.shareP2P```has to be ```"no"```


```
# Header SV Configuration:
headersv:
  general:
    timeoutToTriggerSyncCompleteInSecs: 56
    headersToIgnore: 000000000000000000afe19d2ba3afbbc2627b1a6d7ee2425f998ddabd6134ed, 00000000000000000019f1679932c8a69051fca08d0934ac0c6cad56077d0c66, 0000000000000000004626ff6e3b936941d341c5932ece4357eeccac44e6d56c, 0000000000000000055de705ef722c11d90c1a52de52c21aa646c6bb46de3770
  network:
    networkId: "mainnet"
    shareP2P: "no"
    minPeers: 15
    maxPeers: 30
    initialConnections:
  storage:
    pruning:
      fork:
        enabled: true
        pruneAfterConfirmations: 7
      orphan:
        enabled: true
        pruneAfterInterval: 600
```

### Scenario 2: 

#### Declare the dependencies

*headerSV-rest* has dependencies on both BitcoinJ-SV and the Java Component Library (JCL), so the best approach is to
define the version numbers of each one in your `gradle.properties` file:

```
# BitcoinJ dependency:
bitcoinJVersion=1.0.2

# JCL dependency:
JCLVersion=2.0.0

# headerSV dependency:
headerSVVersion=2.0.0
```
, and then you reference them in your *build.gradle* file:

```
// bitcoinJ modules
implementation "io.bitcoinsv.bitcoinjsv:bitcoinj-base:$bitcoinJVersion"

// jcl Modules
implementation "io.bitcoinsv.jcl:jcl-net:$JCLVersion"
implementation "io.bitcoinsv.jcl:jcl-tools:$JCLVersion"
    
// HeaderSV Modules
implementation "io.bitcoinsv.headerSV:headerSV-core:$headerSVVersion"
implementation "io.bitcoinsv.headerSV:headerSV-rest:$headerSVVersion"

```

#### Define the Configuration:

The Configuration is defined in 2 levels:

 * In *JAVA* code, to include the new *Beans* provided by *headerSV-rest*
 * In *yaml* config files, to set the value of the configuration properties


In the *Java* code, and assuming that the pre-existing *SpringBoot* app is called "myApp", we need to edit our 
current *Spring Configuration* and add a reference to the *HeaderSvRestConfig*.

Since in this case we are **sharing** the P2P used by **heasderSV-rest** and our *App*, we need to expose the *P2P* as a *Bean* in the Spring Context. The recommended way is to use the already present *NetworkConfiguration* imported from *headerSV-config* to initialize it:

```
@Configuration
@Import(HeaderSvRestConfig.class)
@ComponentScan("io.bitcoinsv.headerSV.rest.v1.controller")
public class MyAppConfig {
    @Bean
    P2P getP2P(NetworkConfiguration networkConfiguration) {
        return  P2P.builder(networkConfiguration.getProtocolConfig().getId())
                .config(networkConfiguration.getProtocolConfig())
                .config(networkConfiguration.getNetworkConfig())
                .build();
    }
}
```

You can add all the low-level configuration you need in that method, so you make sure that the *P2P* Service is properly configured.

The 
*yaml* configuration files should be stored in `src/main/resources/application.yml`. The general structure of the config 
file is like follows:

> **NOTE**:
> In this scenario, the value of the properly ```headersv.netwrkService.shareP2P```has to be ```"yes"```

```
# Header SV Configuration:
headersv:
  general:
    timeoutToTriggerSyncCompleteInSecs: 56
    headersToIgnore: 000000000000000000afe19d2ba3afbbc2627b1a6d7ee2425f998ddabd6134ed, 00000000000000000019f1679932c8a69051fca08d0934ac0c6cad56077d0c66, 0000000000000000004626ff6e3b936941d341c5932ece4357eeccac44e6d56c, 0000000000000000055de705ef722c11d90c1a52de52c21aa646c6bb46de3770
  network:
    networkId: "mainnet"
    shareP2P: "yes"
    minPeers: 15
    maxPeers: 30
    initialConnections:
  storage:
    pruning:
      fork:
        enabled: true
        pruneAfterConfirmations: 7
      orphan:
        enabled: true
        pruneAfterInterval: 600
```

### Start the Service

After setting the configuration above, the *HeaderSVService* should be avaiable within the *Spring Context*, so now we
can start/stop it along with the main Application. 

Again, the way we starr/stop the Service depends on our specific scenario:

* **Scenario1**:

The simple one, we just start the Service when the application starts, and stops it when the application shutsdown.

```
@SpringBootApplication
public class MyApp {

	public static void main(String[] args) {
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
```

* **Scenario 2**:

In this case we are **sharing** the **P2P** Service, so the right thing to do is to start/stop this service externally:

```
@SpringBootApplication
public class MyApp {

	public static void main(String[] args) {
		SpringApplication.run(HeaderSVApplication.class, args);
	}

    @Autowired
    private P2P p2p;
    
	@Autowired
	private HeaderSvService headerSvService;


	@EventListener
	public void onStart(ApplicationReadyEvent event) {
	   p2p.start();
		headerSvService.start();
	}

	@PreDestroy
	public void onStop() {
		headerSvService.stop();
		p2p.stop();
	}

}
```

Using the code above, the *HeaderSV Service* will be running in the background, and a *REST API* will be exposed to
get information about the Blockchain:

## HeaderSV REST API | V1

The *REST API* exposed by the *headerSV-rest* (and *headersv-app*) modules provides the following endpoints:

### Chain

#### Query Chain Tips
Returns the latest headers for the tip of the chain and any forks
```
/api/v1/chain/tips
```
Returns the the specified header for the tip of the chain and any forks

#### Prune Fork
Prunes a specified fork

```
/api/v1/chain/tips/prune/{hash}

```

ID | TYPE | Description |
---|------|-------------|
hash | STRING | The Header hash


#### Query Headers
Retrieves the header with the given hash
```
/api/v1/chain/header/{hash}
```
The content type: ```application/octet-stream``` can be provided in the requst header to return the headers raw bytes.

#### Query Headers By Height (batched)
Retrieves the header with the given hash
```
/api/v1/chain/header/byHeight?height=<start_height>&count=<count>
```
The Accept type: ```application/octet-stream``` can be provided in the requst header to return the headers as a
stream of raw bytes.

The query parameter: *height* is required. The count of headers requested is optional (default = 1)

#### Query Headers State
Retrieves the header with the given hash along with it's state relative to the chain

```
/api/v1/chain/header/state/{hash}

```
ID | TYPE | Description |
---|------|-------------|
hash | STRING | The Header hash

#### Response
Returns a list of block headers and their state if specified.
```
{
    "header": {
        "hash": "00000000dfd5d65c9d8561b4b8f60a63018fe3933ecb131fb37f905f87da951a",
        "version": 1,
        "prevBlockHash": "00000000a1496d802a4a4074590ec34074b76a8ea6b81c1c9ad4192d3c2ea226",
        "merkleRoot": "10f072e631081ad6bcddeabb90bc34d787fe7d7116fe0298ff26c50c5e21bfea",
        "creationTimestamp": 1233046715,
        "difficultyTarget": 486604799,
        "nonce": 2999858432,
        "transactionCount": 0,
        "work": 4295032833
    },
    "state": "STALE",
    "chainWork": 65537,
    "height": 2000,
    "confirmations": 4000
}
```

ID | TYPE | VALUES |
---|------|-------------|
state | ENUM | LONGEST_CHAIN, ORPHAN or STALE

#### Query Network
Returns a list of peers that are connected to the application
```
/api/v1/network/peers
```
Returns the number of peers connected to the application
```
/api/v1/network/peers/count
```
