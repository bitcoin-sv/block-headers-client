# HeaderSV Client
This is a lightweight client app for the BitcoinSV Blockchain. The App will connect to the BitcoinSV network and manage whitelists and blacklists internally. 
The application ensures that it is always connected to at least the minimum number of peers configured before synchronizing any blocks.

A REST API is also provided by the application which will allow rapid lookups of chain and network state, as well as basic controls to control stored data.

The networks currently supported by the client are *Mainnet*, *Testnet* and *STNnet*. Additional networks can be added with ease.

## Version 2.x update

 Since version 2.0, the *HeaderSV Client* is distributed in a multi-modular fashion. So while the pre-existing 
 functionality remains the same, now it's more flexible and easy to integrate into third party projects if needed to.
 So, the *HeaderSV Client* is now a *Suite* of modules:

> **UPDATE:
> The previous versions of the *HeaderSV Client (v 1.x)* are equivalent now to the *headersv-app* module described below**

---

  * [**headerSV-app**](app/README.MD): This is equivalent to the *HeaderSV Client v.1.x*. It is a standalone application that can be deployed
  and it also provides a *REST API* we can use to retrieve information about the Chain.
  
    **If you are just upgrading from previous versions of *HeaderSV Client* and want things unchanged, this is the right module.**
 
---

 * [**headerSV-rest**](rest/README.MD): This module is a Java library (JAR) that provides the REST API mentioned above as a separate layer that 
  you can add to your pre-existing Application. 
  
  **If you already have an application and want to add *HeaderSV capabilities* 
 to it rather than deploy a separate app, this is the right module.**
 
---

 * [**headerSV-rest-client**](rest-client/README.MD): This module is a Java Library (JAR) that provides a client for the *REST API*. 
 
 **If you only want to 
 *consume* the info produced from *HeaderSV* which is running in a different machine, this is right module.**
 
---

 * [**headerSV-core**](core/README.MD): This module is a Java Library that implements the *core* functionality of *HeaderSV*: It connects to the 
 network, synchronizes with the Chain an API (not REST) that can be used to retrieve information from it. 
 
 **If you 
 want to synchronize with the chain and get updated in real-time about changes in it, this is the right module.**

