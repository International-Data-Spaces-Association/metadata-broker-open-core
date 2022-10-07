package de.fraunhofer.iais.eis.ids.index.common.util;

import de.fraunhofer.iais.eis.ids.index.common.persistence.RepositoryFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class ConnectionFactory {
    final private Logger logger = LoggerFactory.getLogger(ConnectionFactory.class);
    private final Integer retries;
    private String hostname = "";
    private String port = "";

    ConnectionFactory(String hostname, String port, Integer retries) {
        this.hostname = hostname;
        this.port = port;
        if (retries>0)
            this.retries = retries;
        else
            throw new IllegalArgumentException("Argument retries should be >0!");
    }

    ConnectionFactory(String hostname, String port) {
        this(hostname, port, 3);
    }

    abstract Object createConnection() throws Exception;

    public Object getConnection() {
        Integer counter = 0;
        Object connection=null;
        logger.info("Trying to establish a connection " + hostname + " with hostname " + hostname + " under port " + port);
        while (counter < retries) {
            try {
                connection = createConnection();
                break;
            } catch (Exception e) {
                if (counter < retries - 1) {
                    try {
                        logger.info("Unable to establish a connection. Retry to establish connection to " + hostname + " in 3 seconds");
                        System.out.println("Unable to establish a connection. Retry to establish connection to " + hostname + " in 3 seconds");
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                    counter += 1;
                } else {
                    logger.info("stop trying to establish connection and throw exception ... ");

                    System.exit(1);
                }
            }
        }
        logger.info("Connection successfully established...");
        return connection;
    }

    public Integer getRetries() {
        return retries;
    }

    public String getHostname() {
        return hostname;
    }


    public String getPort() {
        return port;
    }


}
