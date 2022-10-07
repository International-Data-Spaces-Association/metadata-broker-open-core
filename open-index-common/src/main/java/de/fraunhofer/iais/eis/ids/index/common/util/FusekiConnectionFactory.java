package de.fraunhofer.iais.eis.ids.index.common.util;

import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FusekiConnectionFactory extends ConnectionFactory{
    final private Logger logger = LoggerFactory.getLogger(FusekiConnectionFactory.class);
    // A simple query string that is used to validate the existence of a valid connection to a fuseki server
    private static final String TEST_CONNECTION_STRING = "ASK WHERE { GRAPH <test> {?s ?p ?o .} }";

    public FusekiConnectionFactory(String hostname) {
        super(hostname, "");
    }

    @Override
    Object createConnection() throws Exception {
        //read only endpoint: host:port/dataset/sparql
        RDFConnection connection =  RDFConnectionFactory.connectFuseki(this.getHostname());
        connection.queryAsk(this.TEST_CONNECTION_STRING);
        connection.close();
        connection.end();
        return RDFConnectionFactory.connectFuseki(this.getHostname());
    }
}
