package de.fraunhofer.iais.eis.ids.index.common.persistence;

import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;

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
        return getElementAsJsonLd(requestedElement, 0);
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
        StringBuilder queryString = new StringBuilder();
        queryString.append("PREFIX ids: <https://w3id.org/idsa/core/> \n");

        //Check if we are at the root. This top path, which is the catalog URI to the outside, is not persisted as such in the triple store, but generated upon request
        //If the root URI has been requested, we need to create specific SPARQL CONSTRUCT queries to serve a connector catalog
        boolean atRoot = false;

        if(requestedElement.equals(catalogUri) || (requestedElement.toString() + "/").equals(catalogUri.toString()))
        {
            logger.info("Catalog has been requested (with depth: " + depth + "): " + requestedElement);
            atRoot = true;
            queryString.append("CONSTRUCT { <").append(catalogUri).append("> a ids:ConnectorCatalog ; ids:listedConnector ?s0 . ?s0 ?p0 ?o0 .");
        }
        else
        {
            logger.info("Custom element has been requested (with depth  " + depth + "): " + requestedElement);
            queryString.append("CONSTRUCT { ?s0 ?p0 ?o0 . ");
        }

        for(int i = 0; i < depth; i++)
        {
            queryString.append("?o").append(i).append(" ?p").append(i + 1).append(" ?o").append(i + 1).append(" . ");
        }

        //Close CONSTRUCT brackets
        queryString.append(" } ");
        repositoryFacade.getActiveGraphs().forEach(graphName -> queryString.append("FROM NAMED <").append(graphName).append("> "));
        queryString.append("WHERE { ");

        if(!atRoot)
        {
            //first ?s0 ?p0 ?o0 NOT in optional block. If unknown resource is requested, error should be thrown
            queryString.append("BIND(<").append(requestedElement.toString()).append("> AS ?s0) . GRAPH ?g { ?s0 ?p0 ?o0 . ");
        }


        else
        {
            //First ?s0 ?p0 ?o0 IS in optional block. If catalog is empty, no error should be thrown
            //At this stage, the ontology class hierarchy is not respected in queries. Therefore, we will list all Connector types
            queryString.append("GRAPH ?g { OPTIONAL { ?s0 ?p0 ?o0 . ?s0 a ?s0type . FILTER( ?s0type IN ( ids:BaseConnector, ids:TrustedConnector, ids:Connector ) ) . ");
        }

        for(int i = 0; i < depth; i++)
        {
            queryString.append("OPTIONAL { ?o").append(i).append(" ?p").append(i + 1).append(" ?o").append(i + 1).append(" ");
        }
        queryString.append("} ".repeat(Math.max(0, depth)));

        queryString.append("} }"); //Brackets from graph and where

        if(atRoot)
        {
            //At root, there is one more OPTIONAL
            queryString.append(" }");
        }

        //Fire construct query against triple store
        Model result = repositoryFacade.constructQuery(queryString.toString());

        //Check if requested element exists in our persistence
        if(result.isEmpty())
        {
            //Result is empty, throw exception. This will result in a RejectionMessage being sent
            throw new RejectMessageException(RejectionReason.NOT_FOUND, new NullPointerException("The requested resource was not found"));
        }

        //Turn the result into a string and return
        return ConstructQueryResultHandler.graphToString(result);

    }

    public String getTypeOfRequestedElement(URI requestedElement) throws RejectMessageException {
        if(requestedElement == null || requestedElement.equals(selfDescription.getId()))
        {
            //TODO: More specific subclasses
            return "https://w3id.org/idsa/core/Connector";
        }
        if(requestedElement.equals(catalogUri))
        {
            //TODO: ConnectorCatalog or ResourceCatalog?
            return "https://w3id.org/idsa/core/Catalog";
        }
        StringBuilder queryString = new StringBuilder();
        queryString.append("SELECT ?type ");
        for(String activeGraph : repositoryFacade.getActiveGraphs())
        {
            queryString.append("FROM NAMED <").append(activeGraph).append("> ");
        }
        queryString.append(" WHERE { GRAPH ?g { ").append("BIND(<").append(requestedElement.toString()).append("> AS ?s) . ?s a ?type . } } ");
        ArrayList<QuerySolution> result = repositoryFacade.selectQuery(queryString.toString());
        if(result == null || result.isEmpty())
        {
            throw new RejectMessageException(RejectionReason.NOT_FOUND, new NullPointerException("Could not retrieve type of " + requestedElement));
        }

        if(result.size() > 1)
        {
            RejectMessageException e = new RejectMessageException(RejectionReason.TOO_MANY_RESULTS, new Exception("Could not determine type of " + requestedElement + " (multiple options)"));
            logger.error("Could not determine the type of a requested element.", e);
            throw e;
        }
        return result.get(0).get("type").toString();
    }

}
