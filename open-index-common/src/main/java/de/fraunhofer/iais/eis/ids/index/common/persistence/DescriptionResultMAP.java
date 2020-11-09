package de.fraunhofer.iais.eis.ids.index.common.persistence;

import de.fraunhofer.iais.eis.DescriptionResponseMessage;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;

import java.util.Optional;

//Payload is JSON-LD of... A connector, a catalog, a resource, a participant

/**
 * Class to encapsulate Message and Payload for messages of type DescriptionResponseMessage
 */
public class DescriptionResultMAP implements MessageAndPayload<DescriptionResponseMessage, String> {

    private final DescriptionResponseMessage response;
    private final String payload;

    /**
     * Constructor
     * @param response The message header
     * @param payload The message payload
     */
    public DescriptionResultMAP(DescriptionResponseMessage response, String payload) {
        this.response = response;
        this.payload = payload;
    }

    /**
     * Accessor to the message header part
     * @return The message header part
     */
    @Override
    public DescriptionResponseMessage getMessage() {
        return response;
    }

    /**
     * Accessor to the message payload part
     * @return The message payload part
     */
    @Override
    public Optional<String> getPayload() {
        return Optional.of(payload);
    }

    /**
     * Accessor to the message payload part in serialized form
     * @return The message payload part in serialized form
     */
    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(payload.getBytes(), "application/ld+json", "selfdescription.rdf");
    }

}
