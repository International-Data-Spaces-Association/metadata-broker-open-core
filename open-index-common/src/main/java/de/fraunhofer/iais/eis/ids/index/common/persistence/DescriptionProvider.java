package de.fraunhofer.iais.eis.ids.index.common.persistence;

import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Class to provide IDS descriptions of known object, such as registered connectors, participants, a self description of the broker/ParIS itself or its catalog
 */
public class DescriptionProvider {

    final Logger logger = LoggerFactory.getLogger(DescriptionProvider.class);
    //For describing "foreign objects"
    RepositoryFacade repositoryFacade;
    //For describing itself
    InfrastructureComponent selfDescription;

    CatalogProvider catalogProvider;

    URI catalogUri;

    /**
     * Constructor
     * @param selfDescription Pass the infrastructure component representing the service provider (broker/ParIS). Required to provide a self description etc.
     * @param repositoryFacade Repository/Triplestore from which information about objects can be retrieved for generating descriptions
     * @param catalogUri The URI of the catalog of the broker (ParIS might not apply here). Required to recognize that the full catalog might have been requested
     */
    public DescriptionProvider(InfrastructureComponent selfDescription, RepositoryFacade repositoryFacade, URI catalogUri){
        this.selfDescription = selfDescription;
        this.repositoryFacade = repositoryFacade;
        if(catalogUri != null) {
            this.catalogProvider = new CatalogProvider(repositoryFacade, catalogUri.toString());
            this.catalogUri = catalogUri;
        }

    }

    /**
     * The single function to get the description of an element in JSON-LD
     * @param requestedElement The URI of the element to be described
     * @return A JSON-LD description of the object, if it is known to the broker/ParIS
     * @throws RejectMessageException thrown if the requested object is not known or could not be retrieved
     */
    public String getElementAsJsonLd(URI requestedElement) throws RejectMessageException {
        return getElementAsJsonLd(requestedElement, 1);
    }

    /**
     * The single function to get the description of an element in JSON-LD
     * @param requestedElement The URI of the element to be described
     * @param depth The depth to which we should show child elements
     * @return A JSON-LD description of the object, if it is known to the broker/ParIS
     * @throws RejectMessageException thrown if the requested object is not known or could not be retrieved
     */
    public String getElementAsJsonLd(URI requestedElement, int depth) throws RejectMessageException {

        //Check if a self description is requested
        if(requestedElement == null || requestedElement.equals(selfDescription.getId()))
        {
            logger.info("Self-description has been requested");
            return selfDescription.toRdf();
        }
        //Check if the catalog is requested (exact match or match except for a missing trailing slash)
        if(requestedElement.equals(catalogUri) || (requestedElement.toString() + "/").equals(catalogUri.toString()))
        {
            logger.info("Full catalog has been requested");
            return catalogProvider.generateCatalogFromTripleStore().toRdf();
        }
        logger.info("Custom element has been requested: " + requestedElement);

        //Something else is requested. Let's see if we can find it inside the triple store
        StringBuilder queryString = new StringBuilder();
        queryString.append("CONSTRUCT { ?s0 ?p0 ?o0 .");

        for(int i = 0; i < depth; i++)
        {
            queryString.append("?o").append(i).append(" ?p").append(i + 1).append(" ?o").append(i + 1).append(" . ");
        }
        queryString.append(" } ");
        //"?o ?p2 ?o2 . ?o2 ?p3 ?o3 . ?o3 ?p4 ?o4 . ?o4 ?p5 ?o5 . ?o5 ?p6 ?o6 . ?o6 ?p7 ?o7 . ?o7 ?p8 ?o8 . ?o8 ?p9 ?o9 . ?o9 ?p10 ?o10 . ?o10 ?p11 ?o11 . ?o11 ?p12 ?o12 . } ");
        repositoryFacade.getActiveGraphs().forEach(graphName -> queryString.append("FROM NAMED <").append(graphName).append("> "));
        queryString.append("WHERE { ");
        queryString.append("BIND(<").append(requestedElement.toString()).append("> AS ?s0) . GRAPH ?g { ?s0 ?p0 ?o0 . ");

        for(int i = 0; i < depth; i++)
        {
            queryString.append("OPTIONAL { ?o").append(i).append(" ?p").append(i + 1).append(" ?o").append(i + 1).append(" ");
        }
        queryString.append("} ".repeat(Math.max(0, depth)));

        queryString.append("} }"); //Brackets from graph and where

        //Fire construct query against triple store
        Model result = repositoryFacade.constructQuery(queryString.toString());

        //Check if requested element exists in our persistence
        if(result.isEmpty())
        {
            //Result is empty, throw exception. This will result in a RejectionMessage being sent
            throw new RejectMessageException(RejectionReason.NOT_FOUND, new NullPointerException("The requested resource was not found"));
        }

        return ConstructQueryResultHandler.graphToString(result);

        /*
        // Below is the "old" approach of trying to turn this into a Resource (contained in a catalog hosted by a connector)
        // or to a Connector etc. - Especially for REST, it seems more prudent to just return what has been asked for


        //If this is of type Connector, return this. If this is of type Resource, return this contained within a Connector
        //Whether a Resource or a Connector is queried, we always want to return a connector
        //Reason: A Resource does not have a link to a Connector, preventing proper follow-up queries

        //Testing whether the requested element is a Connector (or participant): Check if a named graph with this URI exists (and is non-passivated)
        if(repositoryFacade.graphIsActive(requestedElement.toString()))
        {
            //It is a connector. Return the full connector object as-is
            return ConstructQueryResultHandler.graphToString(result);
        }

        //At this stage, it must be a resource
        //Find the connector containing the Resource and grab all.

        StringBuilder getContainingConnector = new StringBuilder();
        getContainingConnector.append("SELECT DISTINCT ?g ?catalog ");
        repositoryFacade.getActiveGraphs().forEach(graphName -> getContainingConnector.append("FROM NAMED <").append(graphName).append("> "));
        getContainingConnector.append("WHERE { BIND(<").append(requestedElement.toString()).append("> AS ?resource) . GRAPH ?g { ?g <https://w3id.org/idsa/core/resourceCatalog> ?catalog . ?resource ?p ?o . } }");
        ArrayList<QuerySolution> containingConnectorResult = repositoryFacade.selectQuery(getContainingConnector.toString());
        if(containingConnectorResult.isEmpty()) {
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, new NullPointerException("Could not determine the connector containing the requested resource."));
        }

        QuerySolution binding = containingConnectorResult.get(0);
        String containingConnectorUri = binding.get("g").toString();
        String containingCatalogUri = binding.get("catalog").toString();
        logger.debug("Found that resource " + requestedElement.toString() + " belongs to graph " + containingConnectorUri);
        if(containingConnectorResult.size() > 1)
        {
            QuerySolution binding2 = containingConnectorResult.get(1);
            String containingConnectorUri2 = binding2.get("g").toString();
            String containingCatalogUri2 = binding2.get("catalog").toString();
            logger.warn("The requested resource " + requestedElement.toString() + " exists in multiple connectors. Returning first connector as result.");
            logger.warn("Catalog URIs and Connector URIs: " + containingCatalogUri + ", " + containingCatalogUri2 + " // " + containingConnectorUri + ", " + containingConnectorUri2);
        }

        //Now that we have the connector URI, we will return the resource plus meta information about the connector, but exclude further resources

        StringBuilder secondQueryString = new StringBuilder();
        secondQueryString.append("CONSTRUCT { ?g <https://w3id.org/idsa/core/resourceCatalog> <").append(containingCatalogUri).append("> . "); //Connector has catalog
        secondQueryString.append("<").append(containingCatalogUri).append("> a <https://w3id.org/idsa/core/ResourceCatalog> ; "); //Is of type catalog
        secondQueryString.append("<https://w3id.org/idsa/core/offeredResource> <").append(requestedElement.toString()).append("> ."); //Catalog contains resource
        //Everything except catalog
        secondQueryString.append("?g ?p ?o . ?o ?p2 ?o2 . ?o2 ?p3 ?o3 . ?o3 ?p4 ?o4 . ?o4 ?p5 ?o5 . ?o5 ?p6 ?o6 . ?o6 ?p7 ?o7 . ?o7 ?p8 ?o8 . ?o8 ?p9 ?o9 . ?o9 ?p10 ?o10 . ?o10 ?p11 ?o11 . ?o11 ?p12 ?o12 . } ");
        //Active graphs only
        repositoryFacade.getActiveGraphs().forEach(graphName -> secondQueryString.append("FROM NAMED <").append(graphName).append("> "));
        secondQueryString.append("WHERE { ");
        //Restrict to target graph (which must be active)
        secondQueryString.append("BIND(<").append(containingConnectorUri).append("> AS ?g) . ");
        //Get everything
        secondQueryString.append("GRAPH ?g { { ?g ?p ?o . OPTIONAL { ?o ?p2 ?o2 . OPTIONAL { ?o2 ?p3 ?o3 . OPTIONAL { ?o3 ?p4 ?o4 . OPTIONAL { ?o4 ?p5 ?o5 . OPTIONAL { ?o5 ?p6 ?o6 . OPTIONAL { ?o6 ?p7 ?o7 . OPTIONAL { ?o7 ?p8 ?o8 . OPTIONAL { ?o8 ?p9 ?o9 . OPTIONAL { ?o9 ?p10 ?o10 . OPTIONAL { ?o10 ?p11 ?o11 . OPTIONAL { ?o11 ?p12 ?o12 . } } } } } } } } } } } } ");
        //Except the catalog, which we already added manually above. This prevents resources other than the requested one from being included in the result
        secondQueryString.append("FILTER (?p != <https://w3id.org/idsa/core/resourceCatalog> ) . ");
        secondQueryString.append("} } "); //Brackets from graph and where
        Model connectorAsGraph = repositoryFacade.constructQuery(secondQueryString.toString());

        //Merge connector meta data with resource meta data from before
        result.add(connectorAsGraph);

        return ConstructQueryResultHandler.graphToString(connectorAsGraph);

         */

    }

}
