# Change log of the MetaDataBroker open core

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