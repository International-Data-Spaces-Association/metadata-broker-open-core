
# Change log of the MetaDataBroker open core
## [Release 5.0.1] - 2022-06-24
### Added
- Added a new method in the Abstract class for Elasticsearch Index called "addResourceAsJson". This method is used to **improve** the reindexing. With this method, reindexing can be done with the JSON-LD representation of the Resource.
- Added a new method called "getResourceIDandAsJSON" in the RepositoryFacade to get the JSON-LD represntation and the ID of the Resource.
## [Release 5.0.0] - 2022-05-23
### Added
- The display of Maintainer, Curator, Sovereign and Publisher can now be represented as URI or as an Object.
- Use more efficient methods to fetch resource from connectors.
- Add parameters to control the index recreation behavior.
- Externalize the parameter JWKS_TRUSTEDHOSTS to be in Dockerfile.
- Add a restriction parameter to control the size of IDS Connectors when Reindexing.
-  Add deleteResource method signature to Indexing. Update resource handing to only use update-/deleteResource method. Connector updates work with given Connector instead of querying the triple store for now
- ResourcePersistenceAndIndexing uses full connector object instead of the reduced connector

<!-- ### Changed -->

<!--  ### Deprecated -->

<!--  ### Removed -->

### Fixed
- Remove a breaking change introduced by Version 5.0.0-SNAPSHOT of the IDS Information Model Java Lib.
- Add Loggers to count the deletion time at the `ResourcePersistenceAndIndexing` to eliminate deletion delays.
- Reconstruct "Update" SPARQL query.
- Upgrade JUnit to version 5.

  
<!--  ### Security -->



## [feature/retryConnectionFuseki] - 12.05.2022
- Retry connection to Fuseki
  - If the property sparqlUrl of the `RepositoryFacade` is not an empty string of set to null, the broker assumes that an external 
  Fuseki server should be used for persistence. So it will throw an exception if it in unable to establish a valid connection after
  three retries. The property can be set via `sparql.url` in the `application.propertier`.
  - Changed the member functions `getNewReadOnlyConnectionToFuseki` and `getNewWritableConnection` of the `RespositoryFacade`
  to check the existence of a valid connection to the fuseki server.
  - The broker tries to establish the connection three time in a row. In between it waits for 5 seconds. This is not 
  parameterizable. It is hard coded.
  - If the property `sparqlUrl` of the `RepositoryFacade` is set to null or is empty an in-mermory db is created and used.  
  - Testing: 
    1. Without a Fuseki server running -> QueryHttpException was thrown as expected
    2. With a Fuseki server running -> connection established and broker initializing of the Fuseki
