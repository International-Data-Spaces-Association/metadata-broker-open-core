package de.fraunhofer.iais.eis.ids.index.common.persistence;

import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.index.common.util.SparqlQueryRewriter;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

import java.io.ByteArrayOutputStream;

public class GenericQueryEvaluator {

    public final RepositoryFacade repositoryFacade;

    public GenericQueryEvaluator(RepositoryFacade repositoryFacade)
    {
        this.repositoryFacade = repositoryFacade;
    }

    public String getResults(String queryString) throws RejectMessageException {
        //Evaluate the reformulated query
        String reformulatedQuery = SparqlQueryRewriter.reformulate(queryString, repositoryFacade);
        Query originalQuery = QueryFactory.create(queryString);
        if(originalQuery.isSelectType())
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            repositoryFacade.selectQuery(reformulatedQuery, outputStream);
            return new String(outputStream.toByteArray());
        }
        if(originalQuery.isDescribeType())
        {
            return ConstructQueryResultHandler.graphToString(repositoryFacade.describeQuery(reformulatedQuery));
        }
        if(originalQuery.isConstructType())
        {
            return ConstructQueryResultHandler.graphToString(repositoryFacade.constructQuery(reformulatedQuery));
        }
        if(originalQuery.isAskType())
        {
            return String.valueOf(repositoryFacade.booleanQuery(reformulatedQuery));
        }
        throw new RejectMessageException(RejectionReason.BAD_PARAMETERS, new Exception("Could not determine query type from SPARQL query (ASK, SELECT, CONSTRUCT, DESCRIBE)"));
    }
}
