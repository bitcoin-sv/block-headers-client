# HeaderSV-App module

The *HeaderSV-app* module is the highest-level module in the HeaderSV Suite. It is a standalone application that 
connects to the blockchain, synchronizes the Block Headers while running also validations on them, and making sure that it's 
always connected to enough Peers.

> The *headerSV-app* module provides the same functionality as the **HeaderSV Client** in previous versions (1.*)

A REST API is also provided by the application which will allow rapid lookups of chain and network state, as well as basic controls to control stored data.

The networks currently supported by the client are **Mainnet**, **Testnet** and **STNnet**. Additional networks can be added with ease.

### Dependencies
#### ** Note **
When building from source, HeaderSV-app is dependent on both BitcoinJ-SV and the Java Component Library (JCL), which are available on Maven Central under [/io/bitcoinsv/](https://repo.maven.apache.org/maven2/io/bitcoinsv/).

#### Java
You will need Java SDK to build this application on your machine. Get the latest version of Java at:
https://www.java.com/en/download/help/index_installing.html

#### Gradle

HeaderSV uses Gradle as its build tool. You can find the latest version of Gradle at:
https://gradle.org/install/

### Installation

#### Pre-requisites (must be on system PATH):
- Open Java JDK 11 from: https://jdk.java.net/java-se-ri/11

To build the application, navigate to the "*/app*" folder and run `gradlew clean build`.  To Run the application using `gradlew run`. 
The *Mainnet* will be synchronized by default. This can be changed by switching profiles in the ```application.properties``` file.

### Standalone Spring Boot build
To build a standalone, bundled application navigate to the root folder and run
`./gradlew assemble`. This will create a `headerSV-boot-xxx` Spring Boot application.
The `.jar` file is runnable with a recent enough Java runtime environment via:

     java.exe -jar headerSV-xxx.jar

Or you can use the scripts in the `/bin` directory if the java runtime is on the system PATH.

See the configuration section below as well as:
`https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#deployment` for details on configuration options.

### Docker
You can build the docker file by either running `docker build -t headersv-app .` from the root directory. Or running `docker-compose build`. Once built, you can run by either
`docker run headersv-app` or `docker-compose up`. Note: if you're not using docker compose, you'll also need to expose the port and mount a volume to expose the API and persist state. This will build
a docker image from source. There is also a public image available within dockerhub, which can be downloaded by specifying the `docker-compose-prebuilt-image.yml` when running docker-compose.

## Verifying
To check your client application is serving REST API calls once running, open a browser at `http://localhost:8001/api/v1/chain/tips` (default port is specified in the `docker-compose` file)

## Configuration
The application has 4 default profiles:

- `bsv-mainnet`
- `bsv-testnet`
- `bsv-stnnet`
- `bsv-regtest`

You can switch between profiles by altering the value `spring.profiles.active=bsv-xxx` in `resources/application.yml`. Profiles can be amended to
alter the values such as `minPeers` and `maxPeers`.

### Docker
Profiles can also be amended by overriding using environment variables, such as those in the `docker-compose` file. Alternatively, you can use the `SPRING_PROFILES_ACTIVE="bsv-xxx"` 
environment variable to switch profiles. To change the default server port (8080 by default) you can use the `SERVER_PORT=12345` environment variable.

## API | V1

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

The query parameter: height is required. The count of headers requested is optional (default = 1)

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
