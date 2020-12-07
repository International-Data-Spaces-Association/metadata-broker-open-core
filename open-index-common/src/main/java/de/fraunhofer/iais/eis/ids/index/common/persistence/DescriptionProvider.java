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
        logger.info("Custom element has been requested (with depth  " + depth + "): " + requestedElement);

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

        //Turn the result into a string and return
        return ConstructQueryResultHandler.graphToString(result);

    }

}
