# HeaderSV-rest-client module

The **HeaderSV-rest-client** module provides a client of the *REST Api* exposed by the modules *headerSV-app* and 
*headerSV-rest*. This client can be used so code is not replicated and the underlying details of the HTTP 
communication are hidden. This module is distributed as a Java library (JAR) which can be added to your project if you 
need access to the HeaderSV API in a remote or local machine.

## How to use *headerSV-rest-client* (gradle)

### Declare the dependencies

*headerSV-rest* has dependencies on both BitcoinJ-SV and the Java Component Library (JCL), so the best approach is to
define the version numbers of each one in your `gradle.properties` file:

```
# BitcoinJ dependency:
bitcoinJVersion=1.0.4

# JCL dependency:
JCLVersion=2.3.2

# headerSV dependency:
headerSVVersion=2.1.0
```
, and then you reference them in your *build.gradle* file:

```
// bitcoinJ modules
implementation "io.bitcoinsv.bitcoinjsv:bitcoinj-base:$bitcoinJVersion"

// jcl Modules
implementation "io.bitcoinsv.jcl:jcl-net:$JCLVersion"
implementation "io.bitcoinsv.jcl:jcl-tools:$JCLVersion"

// HeaderSV Modules
implementation "io.bitcoinsv.headerSV:headerSV-rest-client:$headerSVVersion"

```

### Run the HeaderSV Client:

The *HeaderSV client* is a Service that only needs to configured with the *URL* of the machine where the *REST API* 
is exposed:

```
HeaderSvClient client = new HeaderSvClientImpl("http://192.168.3.1:8080");
```

, and then you can use the different methods that it provides, each one is mapped to one *endpont* in the *REST API*:

```
public interface HeaderSvClient {
    List<ChainStateDTO> getTips();
    void pruneChain(String blockHash);
    BlockHeaderDTO getHeader(String blockHash);
    List<BlockHeaderDTO> getAncestors(String blockHash, String ancestorHash);
    BlockHeaderDTO getCommonAncestor(List<String> blockHashes);
    ChainStateDTO getHeaderDetails(String blockHash);
    List<BlockHeaderDTO> getHeadersByHeight(int height, int count);
    List<PeerAddressDTO> getConnectedPeers();
    int getConnectedPeersCount();
}
```