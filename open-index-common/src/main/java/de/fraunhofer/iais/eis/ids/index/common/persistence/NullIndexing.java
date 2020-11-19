package de.fraunhofer.iais.eis.ids.index.common.persistence;

import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class NullIndexing implements Indexing {

    final private Logger logger = LoggerFactory.getLogger(NullIndexing.class);

    @Override
    public void add(InfrastructureComponent selfDescription) {
        logger.info("Add to index IGNORED.");
    }

    @Override
    public void update(InfrastructureComponent selfDescription) {
        logger.info("Index update IGNORED.");
    }

    @Override
    public void delete(URI issuerConnector) {
        logger.info("Delete from index IGNORED.");
    }

    @Override
    public void add(Participant participant) {
        logger.info("Add to index IGNORED.");
    }

    @Override
    public void update(Participant participant) {
        logger.info("Index update IGNORED.");
    }

    @Override
    @Deprecated
    public void delete(Participant participant) {
        logger.info("Delete from index IGNORED.");
    }

    @Override
    public void recreateIndex(String indexName) {
        logger.info("Clear index IGNORED.");
    }

}
