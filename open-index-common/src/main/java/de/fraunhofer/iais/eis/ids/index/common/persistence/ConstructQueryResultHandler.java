package de.fraunhofer.iais.eis.ids.index.common.persistence;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Participant;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Class which provides some utility for handling SPARQL construct query results, such as parsing the result to a Connector, Participant or Catalog
 */
public class ConstructQueryResultHandler {
    public static String contextDocumentUrl;
    public static String catalogUri;

    /**
     * Takes the result of a Construct Query targeted to retrieve triples about a participant
     * @param result The collection of triples representing the participant
     * @return The participant as a Java object
     * @throws RejectMessageException if the passed result is empty or the serializer encounters an exception
     */
    public static Participant GraphQueryResultToParticipant(Model result) throws RejectMessageException {

        Serializer s = new Serializer();

        try {
            return s.deserialize(graphToString(result), Participant.class);
        }
        catch (IOException e)
        {
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }

    }

    /**
     * Takes the result of a Construct Query targeted to retrieve triples about a connector
     * @param result The collection of triples representing the connector
     * @return The connector as a Java object
     * @throws RejectMessageException if the passed result is empty or the serializer encounters an exception
     */
    public static Connector GraphQueryResultToConnector(Model result) throws RejectMessageException {
        Serializer s = new Serializer();

        try {
            return s.deserialize(graphToString(result), Connector.class);
        }
        catch (IOException e)
        {
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }
    }

    /**
     * Utility function for turning an Apache Jena Model into a JSON-LD String
     * @param model Input model
     * @return Model as JSON-LD String
     */
    public static String graphToString(Model model)
    {
        RDFWriter writer = RDFWriter.create().format(org.apache.jena.riot.RDFFormat.JSONLD).source(model).build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        writer.output(os);
        return new String(os.toByteArray());
    }

}
