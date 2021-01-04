package de.fraunhofer.iais.eis.ids.index.common.persistence;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.MessageHandler;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.TokenRetrievalException;
import de.fraunhofer.iais.eis.ids.component.core.map.DescriptionRequestMAP;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

/**
 * Message handler class, forwarding the request to the DescriptionProvider and responding with a DescriptionResponseMessage
 * @author maboeckmann
 */
public class DescriptionRequestHandler implements MessageHandler<DescriptionRequestMAP, DescriptionResultMAP> {

    private final Logger logger = LoggerFactory.getLogger(DescriptionRequestHandler.class);
    private final DescriptionProvider descriptionProvider;
    private final SecurityTokenProvider securityTokenProvider;
    private final URI responseSenderAgentUri;
    public static int maxDepth = 10;

    /**
     * Constructor
     * @param descriptionProvider Instance of DescriptionProvider class to retrieve descriptions of the requested objects from triple store
     * @param securityTokenProvider Instance of SecurityTokenProvider to retrieve a DAT for the response messages
     * @param responseSenderAgentUri The ids:senderAgent which should be provided in response messages
     */
    public DescriptionRequestHandler(DescriptionProvider descriptionProvider, SecurityTokenProvider securityTokenProvider, URI responseSenderAgentUri) {
        this.descriptionProvider = descriptionProvider;
        this.securityTokenProvider = securityTokenProvider;
        this.responseSenderAgentUri = responseSenderAgentUri;
    }

    /**
     * The actual handle function which is called from the components, if the incoming request can be handled by this class
     * @param messageAndPayload The incoming request
     * @return A DescriptionResponseMessage, if the request could be handled successfully
     * @throws RejectMessageException thrown if an error occurs during the retrieval process, e.g. if the requested object could not be found
     */
    @Override
    public DescriptionResultMAP handle(DescriptionRequestMAP messageAndPayload) throws RejectMessageException {
        String payload;

        //Can we come up with a neater way than using a hardcoded URI? This is a custom header not defined elsewhere
        //Depth determines whether we should only return information about this object, or also about child objects up to a certain hop limit
        if(messageAndPayload.getMessage().getProperties() != null && messageAndPayload.getMessage().getProperties().containsKey("https://w3id.org/idsa/core/depth"))
        {
            //0 is the default value, meaning absolutely no child objects are expanded. Only their URI is shown, which one can dereference to obtain further information
            int depth = 0;
            try {
                //Check out whether a custom depth is provided (in valid format)
                String propertyValue = messageAndPayload.getMessage().getProperties().get("https://w3id.org/idsa/core/depth").toString();
                if(propertyValue.contains("^^")) //expecting something like: 0^^xsd:integer
                {
                    //Only take numeric part, plus quotation marks
                    propertyValue = propertyValue.substring(0, propertyValue.indexOf("^^"));
                }
                //Remove quotation marks
                propertyValue = propertyValue.replace("\"", "");
                //Rest should be a number. Try to parse. If it fails, we use default depth, see caught exception
                depth = Integer.parseInt(propertyValue);
                if(depth > maxDepth)
                {
                    //Only allow up to a certain depth
                    depth = maxDepth;
                }
            }
            catch (NumberFormatException e)
            {
                //Invalid depth provided. For debugging, we are printing this, but otherwise ignoring the parameter, using the default value instead
                logger.warn("Failed to parse depth header of incoming message to a number.", e);
            }
            //Retrieve object with custom depth
            payload = descriptionProvider.getElementAsJsonLd(messageAndPayload.getMessage().getRequestedElement(), depth);
        }
        else {
            //Retrieve object with default depth
            payload = descriptionProvider.getElementAsJsonLd(messageAndPayload.getMessage().getRequestedElement());
        }
        try {
            //If this point is reached, the retrieval of the requestedElement was successful (otherwise RejectMessageException is thrown)
            //For REST interface, it is useful to know the class of the requested element
            String typeOfRequestedElement;
            if(messageAndPayload.getMessage().getRequestedElement() != null)
            {
                typeOfRequestedElement = descriptionProvider.getTypeOfRequestedElement(messageAndPayload.getMessage().getRequestedElement());
            }
            else
            {
                //No requested element means the root (self-description) was requested
                typeOfRequestedElement = descriptionProvider.selfDescription.getClass().getSimpleName();
            }

            //Wrap result in IDS message - these are just the headers
            DescriptionResponseMessage descriptionResponseMessage = new DescriptionResponseMessageBuilder()
                    ._correlationMessage_(messageAndPayload.getMessage().getId())
                    ._issued_(CalendarUtil.now())
                    ._issuerConnector_(descriptionProvider.selfDescription.getId())
                    ._modelVersion_(descriptionProvider.selfDescription.getOutboundModelVersion())
                    ._securityToken_(securityTokenProvider.getSecurityTokenAsDAT())
                    ._senderAgent_(responseSenderAgentUri)
                    .build();

            //Attach a custom property, containing the type of the returned element
            descriptionResponseMessage.setProperty("elementType", typeOfRequestedElement);

            //Wrap the result in a DescriptionResult MessageAndPayload
            return new DescriptionResultMAP(descriptionResponseMessage,
                    payload //Payload is the JSON-LD representation of the requested element
            );
        }
        catch (TokenRetrievalException e) //occurs if we cannot fetch our own security token
        {
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }
    }

    /**
     * Determines whether an incoming request can be handled by this class
     * @return true, if the message is of an applicable type (DescriptionRequestMessage only)
     */
    @Override
    public Collection<Class<? extends Message>> getSupportedMessageTypes() {
        return Collections.singletonList(DescriptionRequestMessage.class);
    }

}
