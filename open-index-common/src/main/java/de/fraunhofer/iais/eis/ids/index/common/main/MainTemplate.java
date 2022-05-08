package de.fraunhofer.iais.eis.ids.index.common.main;

import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.SelfDescriptionProvider;
import de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps.DapsSecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps.tokenrenewal.DapsTokenExpirationChecker;
import de.fraunhofer.iais.eis.ids.component.interaction.multipart.MultipartComponentInteractor;
import de.fraunhofer.iais.eis.ids.component.protocol.http.server.ComponentInteractorProvider;
import de.fraunhofer.iais.eis.ids.index.common.impl.IndexSelfDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Template class for Main class of an index service.
 * This template should be used to reduce code redundancy and to improve maintainability of various index services
 */
@Configuration
@EnableAutoConfiguration(exclude = SolrAutoConfiguration.class)
@ComponentScan(basePackages = { "de.fraunhofer.iais.eis.ids.component.protocol.http.server"} )
public abstract class MainTemplate implements ComponentInteractorProvider {

    public String componentUri;
    public String componentMaintainer;
    public String componentCatalogUri;
    public String componentModelVersion;
    public String sslCertificatePath;
    public String elasticsearchHostname;
    public boolean refreshAtBeginning;
    public int refreshHours;

    public int limit;
    public int elasticsearchPort;
    public String keystorePassword, keystoreAlias;

    @Deprecated
    public String componentIdsId;
    public String dapsUrl;
    public boolean trustAllCerts, ignoreHostName;

    public MultipartComponentInteractor multipartComponentInteractor;

//    @Value("${ssl.javakeystore}")
//    public static String javaKeystorePath;
    public static FileInputStream javakeystore;

    Logger logger = LoggerFactory.getLogger(this.getClass());

/*    public MainTemplate(String javakeystorepath) {
        try {
            this.javakeystore = new FileInputStream(new File(javakeystorepath));
            logger.info("Found KeyStore at {}.", javakeystorepath);
        } catch (FileNotFoundException e) {
            logger.warn("Could not find a KeyStore at {}.", javakeystorepath);
        }
    }
*/

    /**
     * This function generates a default self-description and can be overridden by child classes
     * @return Self-description of this infrastructure component
     * @throws URISyntaxException if provided URIs are syntactically incorrect
     */
    public SelfDescriptionProvider createSelfDescriptionProvider() throws URISyntaxException {
        return new IndexSelfDescription(
                new URI(componentUri),
                new URI(componentMaintainer),
                new URI(componentCatalogUri),
                componentModelVersion,
                sslCertificatePath,
                new URI(componentUri));
    }

    /**
     * Creates a default DAPS Security Token Provider
     * @return Security Token Provider
     */
    public SecurityTokenProvider createSecurityTokenProvider()
    {
        return new DapsSecurityTokenProvider(
                javakeystore,
                keystorePassword,
                keystoreAlias,
                dapsUrl,
                trustAllCerts,
                ignoreHostName,
                new DapsTokenExpirationChecker());
    }


    /**
     * Entry point for Spring boot. To be implemented by child classes
     */
    public abstract void setUp();


    /**
     * Function is called just before exiting by Spring. To be implemented by child classes
     */
    public abstract void shutDown() throws IOException;


    /**
     * Utility function for retrieving the own component interactor object
     * @return own interactor as MultipartComponentInteractor
     */
    @Override
    public MultipartComponentInteractor getComponentInteractor() {
        return multipartComponentInteractor;
    }
}
