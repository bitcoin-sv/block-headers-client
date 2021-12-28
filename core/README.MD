# HeaderSV-core module

The **HeaderSV-core** module contains the business logic of *HeaderSV*, and its distributed as a Java Library (JAR) 
which can be easily added to other project, so we can get real-time information about the headers in the blockchain 
and use that info in our code.

The functionalities provided by this module are:

 * Connection to the blockchain and automatic synchronization of headers in the background.
 * Streaming of Events with the possibility to *subscribe* to them, so we can get notified when
   the *Tips* of the chain have changed or whether we are already up-to-date with the latest block.
 * It provides an API we can use to retrieve information about the Chain, common ancestors to 
   specific Blocks, or basic information about the Peers we are connected to.
   

> this module does NOT include a *REST Api* or a webserver. Its meant to be used as another Service 
> that can be included in any application. If you also need a REST api to expose its API capabilities, 
> check out the [**headerSV-rest**](rest/README.MD) module
 

## How to use *headerSV-core* (gradle)

### Declare the dependencies 

*headerSV-core* has dependencies on both BitcoinJ-SV and the Java Component Library (JCL), so the best approach is to 
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
implementation "io.bitcoinsv.jcl:jcl-store:$JCLVersion"
implementation "io.bitcoinsv.jcl:jcl-store-keyValue:$JCLVersion"
implementation "io.bitcoinsv.jcl:jcl-store-levelDB:$JCLVersion"

// HeaderSV Modules
implementation "io.bitcoinsv.headerSV:headerSV-core:$headerSVVersion"

```

### Initialize the *HeaderSV Service*

The *headerSV-core* module is composed internally of 2 different services:

 * a **Network** service, which connects to the network, implements the bitcoin Protocol and deals with all the logic behind 
   messages exchange, connections and serialization
 * a **Storage** Service, which stores locally all the info retrieved, in this case the Block Headers and also metadata
   and other information about the blockchain.


Each of the Services above has a configuration class we need to configure first, afer than we can initialize the services:

```
// Network/blockchain we connect to:
Net net = Net.MAINNET;

// Services Configuration:
NetworkConfiguration networkConfig = NetworkConfiguration.builder()
                .net(Net.MAINNET)
                .minPeers(10)
                .maxPeers(15)
                .build();
StorageConfiguration storageConfig = StorageConfiguration.builder()
                .net(Net.MAINNET)
                .forkPrunningEnabled(true)
                .pruneAfterConfirmations(7)
                .build();
                
// General HeaderSV-core Configuration:
HeaderSvConfig headerSvConfig = HeaderSvConfig.of(net);

// We initialize and link the Services together:
NetworkService networkService = new NetworkServiceImpl(networkConfiguration);
StorageService storeService = new StorageServiceImpl(networkConfiguration, storageConfiguration);
HeaderSvService headerSVService = new HeadersSvServiceImpl(headerSvConfig, networkService, storeService);                
                                          
```

### Run the *HeaderSV Service*

After the *HeaderSV Service* is initialize, we can start and leave it running in the background, while we subscribe to
some events so we can react to them:

```
// Before starting the Service, we subscribe to some events:
headerSVService.EVENTS().TIPS_UPDATED().forEach(e -> {
   System.out.println("TIPS_UPDATED: " + e.getUpdatedTips().size() + " tips, " + e.getNewHeaders().size() + " new headers");
});

headerSVService.EVENTS().CHAIN_SYNCHRONIZED().forEach(e -> {
   System.out.println("CHAIN_SYNCHRONIZED");

});

// Now we start the Servie and keep it running for some time:
headerSVService.start();
Thread.sleep(10_000); // Events will be triggered during this time...
headerSVService.stop();

```

While the Service is running we can also access the API and get some information:

```
headerSVService.start();
headerSVService.API().getBlockHeader("000000000000000007f887a516edbe38aa0471d086eab7b916d429c9f6eac821");
headerSVService.API().getHeadersByHeight(8000, 500);
// etc...
headerSVService.stop();

```


