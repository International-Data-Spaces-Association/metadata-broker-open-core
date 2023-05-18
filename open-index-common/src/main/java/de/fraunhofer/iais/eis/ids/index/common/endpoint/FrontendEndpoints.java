package de.fraunhofer.iais.eis.ids.index.common.endpoint;


import de.fraunhofer.iais.eis.ids.index.common.persistence.RepositoryFacade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.env.Environment;


/**
 * This class adds endpoints to send SPARQL (update) queries to the triplestore
 */
@RestController
public class FrontendEndpoints {

    public static RepositoryFacade repositoryFacade;
    //private String sparqlUrl = "http://broker-core:3030/connectorData";

    @PostMapping("/updateQuery")
    public String updateQuery(@RequestBody String queryMessage) {
        repositoryFacade.getNewWritableConnection().update(queryMessage);
        return "Query ran successfully";
    }

    /**
     * This function provides read access to the triplestore by executing a SPARQL SELECT query
     * @param queryMessage The SELECT query to be executed
     * @return Query binding in TSV format
     */
    @PostMapping("/selectQuery")
    public String readQuery(@RequestBody String queryMessage) {
        return repositoryFacade.selectQueryReturnTSV(queryMessage);
    }
}