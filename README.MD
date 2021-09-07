# HeaderSV Client
This is a lightweight header client for the BitcoinSV Blockchain. The client will connect to the BitcoinSV network and manage whitelists and blacklists internally. The application ensures that it is always connected to at least the minimum number of peers configured before synchronizing any blocks.

A REST API is also provided by the application which will allow rapid lookups of chain and network state, as well as basic controls to control stored data.

The networks currently supported by the client are Mainnet, Testnet and STNnet. Additional networks can be added with ease.

### Dependencies
#### ** Note **
When building from source, HeaderSV is dependent on both BitcoinJ-SV and the Java Component Library (JCL), which are available on Maven Central under [/io/bitcoinsv/](https://repo.maven.apache.org/maven2/io/bitcoinsv/).

#### Java
You will need Java SDK to build this application on your machine. Get the latest version of Java at:
https://www.java.com/en/download/help/index_installing.html

#### Gradle

HeaderSV uses Gradle as its build tool. You can find the latest version of Gradle at:
https://gradle.org/install/

### Installation
To build the application, navigate to the root folder and run `./gradlew clean build`.

Run the application using `./gradlew run`. The Mainnet will be synchronized by default. This can be changed by switching profiles in the application.properties file.

### Docker
You can build the docker file by either running `docker build -t headersv .` from the root directory. Or running `docker-compose build`. Once built, you can run by either
`docker run headersv` or `docker-compose up`. Note: if you're not using docker compose, you'll also need to expose the port and mount a volume to expose the API and persist state.

## Verifying
To check your client application is serving REST API calls once running, open a browser at `http://localhost:8080/api/v1/chain/tips` (default port is specified in the `docker-compose` file)

## Configuration
The application has 4 default profiles:
- `bsv-mainnet`
- `bsv-testnet`
- `bsv-stnnet`
- `bsv-regtest`

You can switch between profiles by altering the value `spring.profiles.active=bsv-xxx` in `resources/application.yml`. Profiles can be amended to
alter the values such as `minPeers` and `maxPeers`.

###Docker
Profiles  can also be amended by overriding using environment variables, such as those in the `docker-compose` file.

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
