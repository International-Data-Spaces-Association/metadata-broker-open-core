package de.fraunhofer.iais.eis.ids.index.common.main;

import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.SelfDescriptionProvider;
import de.fraunhofer.iais.eis.ids.component.interaction.multipart.MultipartComponentInteractor;
import de.fraunhofer.iais.eis.ids.index.common.persistence.Indexing;
import de.fraunhofer.iais.eis.ids.index.common.persistence.NullIndexing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collection;

/**
 * Template class for AppConfig of an index service.
 * This template should be used to reduce code redundancy and to improve maintainability of various index services
 */
public abstract class AppConfigTemplate {

    final private Logger logger = LoggerFactory.getLogger(AppConfigTemplate.class);

    public String sparqlEndpointUrl = "";
    public String contextDocumentUrl;
    public URI catalogUri;
    public SelfDescriptionProvider selfDescriptionProvider;
    public Indexing indexing = new NullIndexing();
    public SecurityTokenProvider securityTokenProvider = new SecurityTokenProvider() {
        @Override
        public String getSecurityToken() {
            return "";
        }
    };
    public Collection<String> trustedJwksHosts;
    public boolean dapsValidateIncoming;
    public URI responseSenderAgent;
    public boolean performShaclValidation;

    public AppConfigTemplate(SelfDescriptionProvider selfDescriptionProvider) {
        this.selfDescriptionProvider = selfDescriptionProvider;
    }

    public AppConfigTemplate sparqlEndpointUrl(String sparqlEndpointUrl) {
        this.sparqlEndpointUrl = sparqlEndpointUrl;
        logger.info("SPARQL endpoint set to " +sparqlEndpointUrl);
        return this;
    }


    public AppConfigTemplate contextDocumentUrl(String contextDocumentUrl) {
        this.contextDocumentUrl = contextDocumentUrl;
        logger.info("Context document URL set to " +contextDocumentUrl);
        return this;
    }

    public AppConfigTemplate catalogUri(URI catalogUri) {
        this.catalogUri = catalogUri;
        logger.info("Catalog URI set to " + catalogUri.toString());
        return this;
    }

    public AppConfigTemplate performShaclValidation(boolean performValidation)
    {
        this.performShaclValidation = performValidation;
        logger.info("Perform SHACL Validation is set to " + performValidation);
        return this;
    }

    public AppConfigTemplate securityTokenProvider(SecurityTokenProvider securityTokenProvider) {
        this.securityTokenProvider = securityTokenProvider;
        return this;
    }

    public AppConfigTemplate trustedJwksHosts(Collection<String> trustedJwksHosts) {
        this.trustedJwksHosts = trustedJwksHosts;
        return this;
    }

    public AppConfigTemplate responseSenderAgent(URI responseSenderAgent) {
        this.responseSenderAgent = responseSenderAgent;
        return this;
    }

    public AppConfigTemplate dapsValidateIncoming(boolean dapsValidateIncoming) {
        this.dapsValidateIncoming = dapsValidateIncoming;
        logger.info("Incoming messages DAPS token verification enabled: " +dapsValidateIncoming);
        return this;
    }

    public abstract MultipartComponentInteractor build();


}
