package de.fraunhofer.iais.eis.ids.broker.core.common.persistence;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.broker.core.common.impl.SelfDescriptionPersistenceAdapter;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.index.common.persistence.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * This class takes care of persisting and indexing any changes to connectors that are announced to the broker
 */
public class SelfDescriptionPersistenceAndIndexing extends SelfDescriptionPersistenceAdapter {

    private final Logger logger = LoggerFactory.getLogger(SelfDescriptionPersistenceAndIndexing.class);
    private final ConnectorModelCreator connectorModelCreator = new ConnectorModelCreator();

    private final RepositoryFacade repositoryFacade;
    private Indexing indexing = new NullIndexing();

    private final URI componentCatalogUri;

    /**
     * Constructor
     * @param repositoryFacade repository (triple store) to which the modifications should be stored
     */
    public SelfDescriptionPersistenceAndIndexing(RepositoryFacade repositoryFacade, URI componentCatalogUri) {
        this.repositoryFacade = repositoryFacade;
        this.componentCatalogUri = componentCatalogUri;
        Date date=new Date();
        Timer timer = new Timer();

        //Regularly recreate the index to keep index and triple store in sync
        //The triple store is considered as single source of truth, so the index is dropped and recreated from the triple store
        timer.schedule(new TimerTask(){
            public void run(){
                refreshIndex();
            }
        },date, 12*60*60*1000); //12*60*60*1000 add 12 hours delay between job executions.
    }

    /**
     * Setter for the indexing method
     * @param indexing indexing to be used
     */
    public void setIndexing(Indexing indexing) {
        this.indexing = indexing;
    }

    /**
     * Setter for the context document URL. Typically extracted from the application.properties
     * @param contextDocumentUrl the context document URL to be used
     */
    public void setContextDocumentUrl(String contextDocumentUrl) {
        connectorModelCreator.setContextFetchStrategy(JsonLdContextFetchStrategy.FROM_URL, contextDocumentUrl);
    }

    /**
     * Function to refresh the index. The index is dropped entirely and recreated from the triple store
     * This keeps the index and triple store in sync, while respecting the triple store as single source of truth
     */
    public void refreshIndex() {
        //Recreate the index to delete everything
        /* TODO enable again
        try {
            logger.info("Refreshing index.");
            indexing.recreateIndex();

            //Iterate over all active graphs, i.e. non-passivated and non-deleted graphs
            for (String graph : repositoryFacade.getActiveGraphs()) {
                //Add each connector to the index
                logger.info("Adding connector " + graph + " to index.");
                indexing.add(repositoryFacade.getConnectorFromTripleStore(new URI(graph)));
            }
        }
        catch (ConnectException ignored){} //Prevent startup error
        catch (IOException | URISyntaxException | RejectMessageException e)
        {
            logger.error("Failed to refresh index: ", e);
        }
         */
    }

    /**
     * Small utility function to replace URIs in a string
     * @param input String in which URI is to be replaced
     * @param oldURI old URI
     * @param newURI new URI
     * @return updated string
     */
    private String doReplace(String input, URI oldURI, URI newURI)
    {
        //Make sure that we replace only "full URIs" and don't replace the URI if it is only part of a longer URI
        return input.replace("\"" + oldURI + "\"", "\"" + newURI + "\"");
    }

    /**
     * This function replaces URIs of an infrastructure component (component + catalog + resources + representations + artifacts)
     * The new URIs match a scheme suitable for a RESTful API
     * @param infrastructureComponent original InfrastructureComponent
     * @return new InfrastructureComponent with different IDs
     * @throws IOException if parsing of the updated component fails
     * @throws URISyntaxException if an invalid URI is created during this process
     */
    private InfrastructureComponent replaceIds(InfrastructureComponent infrastructureComponent) throws IOException, URISyntaxException {
        //Collect all relevant IDs of IDS items (connector, catalogs, resources, representations, artifacts) replace them later
        //TODO: Replace even more URIs? Endpoints of Connectors/Resources? Contracts? Many more?

        //TODO: Ideally, use relative URIs: "./ + hashCode" instead, but Serializer (Jena) fails on that. We don't really want to store the full URI here, as that makes the broker un-portable
        URI infrastructureComponentUri = URI.create(componentCatalogUri.toString() + infrastructureComponent.getId().hashCode());
        String currentString = infrastructureComponent.toRdf();
        currentString = doReplace(currentString, infrastructureComponent.getId(), infrastructureComponentUri);
        //urisToReplace.add(infrastructureComponent.getId());
        //Replace infrastructureComponent ID with ./someString

        if(((Connector)infrastructureComponent).getResourceCatalog() != null) {
            for (ResourceCatalog resourceCatalog : ((Connector) infrastructureComponent).getResourceCatalog()) {
                URI catalogUri = new URI(infrastructureComponentUri + "/catalog/" + resourceCatalog.getId().hashCode());
                currentString = doReplace(currentString, resourceCatalog.getId(), catalogUri);

                //urisToReplace.add(resourceCatalog.getId());
                Map<Resource, Boolean> resourcesToHandle = new HashMap<>();
                if(resourceCatalog.getOfferedResource() != null)
                {
                    for(Resource r : resourceCatalog.getOfferedResource())
                    {
                        resourcesToHandle.put(r, true);
                    }
                }
                if(resourceCatalog.getRequestedResource() != null)
                {
                    for(Resource r : resourceCatalog.getRequestedResource())
                    {
                        resourcesToHandle.put(r, false);
                    }
                }
                for(Map.Entry<Resource, Boolean> currentResource : resourcesToHandle.entrySet())
                {
                    URI resourceUri;
                    if(currentResource.getValue())
                    {
                        resourceUri = new URI(catalogUri + "/offeredResource/" + currentResource.getKey().getId().hashCode());
                    }
                    else
                    {
                        resourceUri = new URI(catalogUri + "/requestedResource/" + currentResource.getKey().getId().hashCode());
                    }

                    if(currentResource.getKey().getContractOffer() != null && !currentResource.getKey().getContractOffer().isEmpty())
                    {
                        for(ContractOffer contractOffer : currentResource.getKey().getContractOffer())
                        {
                            URI contractOfferUri = new URI(resourceUri + "/contractOffer/" + contractOffer.getId().hashCode());
                            currentString = doReplace(currentString, contractOffer.getId(), contractOfferUri);
                            Map<Rule, URI> allRules = new HashMap<>();
                            if(contractOffer.getObligation() != null && !contractOffer.getObligation().isEmpty()) {
                                for (Duty duty : contractOffer.getObligation()) {
                                    allRules.put(duty, new URI(contractOfferUri.toString() + "/duty/" + duty.getId().hashCode()));
                                }
                            }
                            if(contractOffer.getPermission() != null && !contractOffer.getPermission().isEmpty()) {
                                for (Permission permission : contractOffer.getPermission()) {
                                    allRules.put(permission, new URI(contractOfferUri.toString() + "/permission/" + permission.getId().hashCode()));
                                    if(permission.getPreDuty() != null && !permission.getPreDuty().isEmpty())
                                    {
                                        for(Duty duty : permission.getPreDuty())
                                        {
                                            allRules.put(duty, new URI(contractOfferUri.toString() + "/permission/" + permission.getId().hashCode() + "/preDuty/" + duty.getId().hashCode()));
                                        }
                                    }
                                    if(permission.getPostDuty() != null && !permission.getPostDuty().isEmpty())
                                    {
                                        for(Duty duty : permission.getPostDuty())
                                        {
                                            allRules.put(duty, new URI(contractOfferUri.toString() + "/permission/" + permission.getId().hashCode() + "/postDuty/" + duty.getId().hashCode()));
                                        }
                                    }
                                }
                            }
                            if(contractOffer.getProhibition() != null && !contractOffer.getProhibition().isEmpty())
                            {
                                for (Prohibition prohibition : contractOffer.getProhibition()) {
                                    allRules.put(prohibition, new URI(contractOfferUri.toString() + "/prohibition/" + prohibition.getId().hashCode()));
                                }
                            }
                            if(!allRules.isEmpty()) {
                                for (Map.Entry<Rule, URI> ruleEntry : allRules.entrySet()) {
                                    currentString = doReplace(currentString, ruleEntry.getKey().getId(), ruleEntry.getValue());
                                    if(ruleEntry.getKey().getConstraint() != null && !ruleEntry.getKey().getConstraint().isEmpty())
                                    {
                                        for(Constraint constraint : ruleEntry.getKey().getConstraint())
                                        {
                                            currentString = doReplace(currentString, constraint.getId(), new URI(ruleEntry.getValue() + "/constraint/" + constraint.getId().hashCode()));
                                        }
                                    }
                                }
                            }

                            if(contractOffer.getContractDocument() != null)
                            {
                                currentString = doReplace(currentString, contractOffer.getContractDocument().getId(), new URI(contractOfferUri + "/contractDocument/" + contractOffer.getContractDocument().getId().hashCode()));
                            }

                        }
                    }

                    currentString = doReplace(currentString, currentResource.getKey().getId(), resourceUri);

                    if(currentResource.getKey().getResourceEndpoint() != null && !currentResource.getKey().getResourceEndpoint().isEmpty())
                    {
                        for(ConnectorEndpoint connectorEndpoint : currentResource.getKey().getResourceEndpoint())
                        {
                            URI endpointUri = new URI(resourceUri + "/resourceEndpoint/" + connectorEndpoint.getId().hashCode());
                            if(connectorEndpoint.getEndpointArtifact() != null)
                            {
                                currentString = doReplace(currentString, connectorEndpoint.getEndpointArtifact().getId(), new URI(connectorEndpoint.getEndpointArtifact().getId() + "/endpointArtifact/" + connectorEndpoint.getEndpointArtifact().getId().hashCode()));
                            }

                            currentString = doReplace(currentString, connectorEndpoint.getId(), endpointUri);
                        }

                    }

                    //urisToReplace.add(resource.getId());
                    if(currentResource.getKey().getRepresentation() != null)
                    {
                        for(Representation representation : currentResource.getKey().getRepresentation())
                        {
                            URI representationURI = new URI(resourceUri + "/representation/" + representation.getId().hashCode());
                            currentString = doReplace(currentString, representation.getId(), representationURI);
                            if(representation.getInstance() != null)
                            {
                                for(RepresentationInstance artifact : representation.getInstance())
                                {
                                    currentString = doReplace(currentString, artifact.getId(), new URI(representationURI + "/instance/" + artifact.getId().hashCode()));
                                    //urisToReplace.add(artifact.getId());
                                }
                            }
                        }
                    }
                }
            }
        }
        //TODO: Store "old" URIs in properties map
        //System.out.println(currentString);
        return new Serializer().deserialize(currentString, InfrastructureComponent.class);
    }


    /**
     * Function to persist and index modifications to an existing connector
     * @param infrastructureComponent The updated connector which was announced to the broker
     * @throws IOException thrown, if the connection to the repository could not be established
     * @throws RejectMessageException thrown, if the update is not permitted, e.g. because the connector was previously deleted, or if an internal error occurs
     */
    @Override
    public void updated(InfrastructureComponent infrastructureComponent) throws IOException, RejectMessageException {
        boolean wasActive = repositoryFacade.graphIsActive(infrastructureComponent.getId().toString());
        boolean existed = repositoryFacade.graphExists(infrastructureComponent.getId().toString());

        //Replace URIs in this infrastructureComponent with URIs matching our scheme. This is required for a RESTful API
        //TODO: Do the same for resources (or at ParIS, for participants)
        try {
            infrastructureComponent = replaceIds(infrastructureComponent);
        }
        catch (URISyntaxException e)
        {
            throw new IOException(e);
        }
        if(!existed)
        {
            logger.info("New connector registered: " + infrastructureComponent.getId().toString());
            addToTriplestore(infrastructureComponent.toRdf());
        }
        else {
            logger.info("Updating a connector which is already known to the broker: " + infrastructureComponent.getId().toString());
            updateTriplestore(infrastructureComponent.toRdf());
        }
        //We need to reflect the changes in the index.
        //If the connector was passive before, the document was deleted from the index, so we need to recreate it
        if(wasActive) { //Connector exists in index - update it
            try {
                indexing.update(infrastructureComponent);
            }
            catch (Exception e)
            {
                if(e.getMessage().contains("document_missing_exception")) {
                    indexing.add(infrastructureComponent);
                }
                else
                {
                    logger.error("ElasticsearchStatusException caught with message " + e.getMessage());
                    throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
                }
            }
        }
        else
        { //Connector does not exist in index - create it
            indexing.add(infrastructureComponent);
        }
    }

    /**
     * Internal function which should only be called from the available function. It applies the changes to the triple store
     * @param selfDescriptionJsonLD String representation of the connector to be added to triple store
     * @throws IOException thrown, if the changes could not be applied to the triple store
     * @throws RejectMessageException thrown, if the changes are illegal, or if an internal error has occurred
     */
    private void addToTriplestore(String selfDescriptionJsonLD) throws IOException, RejectMessageException {
        ConnectorModelCreator.InnerModel result = connectorModelCreator.toModel(selfDescriptionJsonLD);
        repositoryFacade.addStatements(result.getModel(), result.getNamedGraph().toString());
    }

    /**
     * Internal function which should only be called from the updated function. It applies the changes to the triple store
     * @param selfDescriptionJsonLD String representation of the connector which needs to be updated
     * @throws IOException thrown, if the changes could not be applied to the triple store
     */
    private void updateTriplestore(String selfDescriptionJsonLD) throws IOException, RejectMessageException {
        ConnectorModelCreator.InnerModel result = connectorModelCreator.toModel(selfDescriptionJsonLD);
        repositoryFacade.replaceStatements(result.getModel(), result.getNamedGraph().toString());
    }

    /**
     * Function to mark a given Connector as deleted/passivated in the triple store and delete the Connector from the index
     * @param issuerConnector A URI reference to the connector which is now inactive
     * @throws IOException if the connection to the triple store could not be established
     * @throws RejectMessageException if the operation is not permitted, e.g. because one is trying to passivate a Connector which was previously deleted or due to an internal error
     */
    @Override
    public void unavailable(URI issuerConnector) throws IOException, RejectMessageException {
        //Turn graph into a passive one
        if(repositoryFacade.graphIsActive(issuerConnector.toString()))
        {
            repositoryFacade.changePassivationOfGraph(issuerConnector.toString(), false);
        }
        else
        {
            throw new RejectMessageException(RejectionReason.NOT_FOUND, new NullPointerException("The connector you are trying to remove was not found"));
        }

        //Remove the passivated graph from indexing. Upon re-activating, this will be undone
        indexing.delete(issuerConnector);
    }

    /**
     * Internal function which should only be called from the unavailable function. It marks a connector as deleted.
     * Note that the connector is NEVER physically deleted from the triple store
     * @param issuerConnector URI of the connector to be removed from triple store
     * @throws RejectMessageException thrown, if the changes are illegal, or if an internal error has occurred
     */
    private void removeFromTriplestore(URI issuerConnector) throws RejectMessageException {
        if(repositoryFacade.graphExists(issuerConnector.toString()))
        {
            repositoryFacade.changePassivationOfGraph(issuerConnector.toString(), false);
        }
        else
        {
            throw new RejectMessageException(RejectionReason.NOT_FOUND, new NullPointerException("The connector you are trying to delete was not found"));
        }
    }


    /**
     * Utility function to evaluate a given query (in a re-formulated way, respecting passivation and hiding underlying structure of named graphs)
     * @param queryString Query to be evaluated
     * @return Query result in String format
     * @throws RejectMessageException, if the query is illegal or if the index is empty
     */
    @Override
    public String getResults(String queryString) throws RejectMessageException {
        return new GenericQueryEvaluator(repositoryFacade).getResults(queryString);
    }
}
