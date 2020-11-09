package de.fraunhofer.iais.eis.ids.broker.main;

import de.fraunhofer.iais.eis.ResourceCatalogBuilder;
import de.fraunhofer.iais.eis.ids.broker.core.common.impl.ConnectorUnavailableValidationStrategy;
import de.fraunhofer.iais.eis.ids.broker.core.common.persistence.RegistrationHandler;
import de.fraunhofer.iais.eis.ids.broker.core.common.persistence.ResourceMessageHandler;
import de.fraunhofer.iais.eis.ids.broker.core.common.persistence.ResourcePersistenceAndIndexing;
import de.fraunhofer.iais.eis.ids.broker.core.common.persistence.SelfDescriptionPersistenceAndIndexing;
import de.fraunhofer.iais.eis.ids.component.core.DefaultComponent;
import de.fraunhofer.iais.eis.ids.component.core.RequestType;
import de.fraunhofer.iais.eis.ids.component.core.SelfDescriptionProvider;
import de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps.DapsSecurityTokenVerifier;
import de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps.JWKSFromIssuer;
import de.fraunhofer.iais.eis.ids.component.interaction.multipart.MultipartComponentInteractor;
import de.fraunhofer.iais.eis.ids.component.interaction.validation.ShaclValidator;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.QueryHandler;
import de.fraunhofer.iais.eis.ids.index.common.main.AppConfigTemplate;
import de.fraunhofer.iais.eis.ids.index.common.persistence.ConstructQueryResultHandler;
import de.fraunhofer.iais.eis.ids.index.common.persistence.DescriptionProvider;
import de.fraunhofer.iais.eis.ids.index.common.persistence.DescriptionRequestHandler;
import de.fraunhofer.iais.eis.ids.index.common.persistence.RepositoryFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This class is used to start up a broker with appropriate settings and is only created once from the Main class
 */
public class AppConfig extends AppConfigTemplate {

    private final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    public AppConfig(SelfDescriptionProvider selfDescriptionProvider) {
        super(selfDescriptionProvider);
    }

    @Override
    public MultipartComponentInteractor build() {
        //Try to pre-initialize the SHACL validation shapes so that this won't slow us down during message handling
        //TODO: Do this in a separate thread
        if(performShaclValidation) {
            try {
                ShaclValidator.initialize();
            } catch (IOException e) {
                logger.warn("Failed to initialize Shapes for SHACL validation.", e);
            }
        }
        RepositoryFacade repositoryFacade = new RepositoryFacade(sparqlEndpointUrl);

        SelfDescriptionPersistenceAndIndexing selfDescriptionPersistence = new SelfDescriptionPersistenceAndIndexing(
                repositoryFacade, catalogUri);
        selfDescriptionPersistence.setIndexing(indexing);
        ResourcePersistenceAndIndexing resourcePersistenceAndIndexing = new ResourcePersistenceAndIndexing(
                repositoryFacade, catalogUri);
        resourcePersistenceAndIndexing.setIndexing(indexing);

        if (contextDocumentUrl != null && !contextDocumentUrl.isEmpty()) {
            selfDescriptionPersistence.setContextDocumentUrl(contextDocumentUrl);
            resourcePersistenceAndIndexing.setContextDocumentUrl(contextDocumentUrl);
            ConstructQueryResultHandler.contextDocumentUrl = contextDocumentUrl;
        }
        ConstructQueryResultHandler.catalogUri = (catalogUri == null) ? new ResourceCatalogBuilder().build().getId().toString() : catalogUri.toString();


        RegistrationHandler registrationHandler = new RegistrationHandler(selfDescriptionPersistence, selfDescriptionProvider.getSelfDescription(), securityTokenProvider, responseSenderAgent);
        registrationHandler.addMapValidationStrategy(new ConnectorUnavailableValidationStrategy());
        QueryHandler queryHandler = new QueryHandler(selfDescriptionProvider.getSelfDescription(), selfDescriptionPersistence, securityTokenProvider, responseSenderAgent);

        //TODO: Does the broker need to understand an ArtifactRequestMessage?
        //Remove this handler. Catalog should be retrieved via DescriptionRequestMessage
        //ArtifactHandler artifactHandler = new ArtifactHandler(selfDescriptionProvider.getSelfDescription(), new CatalogAsArtifactProvider(selfDescriptionPersistence, catalogUri), securityTokenProvider, responseSenderAgent);
        ResourceMessageHandler resourceHandler = new ResourceMessageHandler(resourcePersistenceAndIndexing, selfDescriptionProvider.getSelfDescription(), securityTokenProvider, responseSenderAgent);

        DefaultComponent component = new DefaultComponent(selfDescriptionProvider, securityTokenProvider, responseSenderAgent, false);
        //TODO: Does not work in the case that the catalog is empty
        DescriptionProvider descriptionProvider = new DescriptionProvider(selfDescriptionProvider.getSelfDescription(), repositoryFacade, catalogUri);
        DescriptionRequestHandler descriptionHandler = new DescriptionRequestHandler(descriptionProvider, securityTokenProvider, responseSenderAgent);
        component.addMessageHandler(descriptionHandler, RequestType.INFRASTRUCTURE);
        component.addMessageHandler(registrationHandler, RequestType.INFRASTRUCTURE);
        component.addMessageHandler(queryHandler, RequestType.INFRASTRUCTURE);
        //component.addMessageHandler(artifactHandler, RequestType.INFRASTRUCTURE);
        component.addMessageHandler(resourceHandler, RequestType.INFRASTRUCTURE);
//        component.setSecurityTokenProvider(securityTokenProvider);

        if (dapsValidateIncoming) {
            component.setSecurityTokenVerifier(new DapsSecurityTokenVerifier(new JWKSFromIssuer(trustedJwksHosts),
                    (claims) -> {/* don't check the claims for the moment */}));
        }

        return new MultipartComponentInteractor(component, securityTokenProvider, responseSenderAgent, performShaclValidation);
    }

}
