package de.fraunhofer.iais.eis.ids.broker.core.common.persistence;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.TokenRetrievalException;
import de.fraunhofer.iais.eis.ids.component.core.map.DefaultSuccessMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.InfrastructureComponentStatusHandler;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.SameOriginInfrastructureComponentMapValidationStrategy;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.map.InfrastructureComponentMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation.ValidatingMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

/**
 * This class is a message handler for messages about the status of infrastructure components,
 * such as ConnectorAvailableMessages, ConnectorUpdateMessages, ConnectorInactiveMessages, and ConnectorUnavailableMessages
 */
public class RegistrationHandler extends ValidatingMessageHandler<InfrastructureComponentMAP, DefaultSuccessMAP> {

    private final InfrastructureComponent infrastructureComponent;
    private final InfrastructureComponentStatusHandler infrastructureComponentStatusHandler;
    private final SecurityTokenProvider securityTokenProvider;
    private final URI responseSenderUri;

    Logger logger = LoggerFactory.getLogger(RegistrationHandler.class);

    /**
     * Constructor
     * @param infrastructureComponentStatusHandler The component which then takes care of persisting the changes
     * @param infrastructureComponent The broker as infrastructure component, such that appropriate responses can be sent
     * @param securityTokenProvider A security token provider for sending responses with a DAT
     * @param responseSenderUri The "senderAgent" which should show in automatic response messages
     */
    public RegistrationHandler(InfrastructureComponentStatusHandler infrastructureComponentStatusHandler,
                               InfrastructureComponent infrastructureComponent,
                               SecurityTokenProvider securityTokenProvider,
                               URI responseSenderUri)
    {
        this.infrastructureComponent = infrastructureComponent;
        this.infrastructureComponentStatusHandler = infrastructureComponentStatusHandler;
        this.securityTokenProvider = securityTokenProvider;
        this.responseSenderUri = responseSenderUri;

        addMapValidationStrategy(new SameOriginInfrastructureComponentMapValidationStrategy());
    }

    /**
     * This function takes care of an inbound message which can be handled by this class
     * @param messageAndPayload The message to be handled
     * @return MessageProcessedNotification wrapped in a DefaultSuccessMAP, if the message has been processed properly
     * @throws RejectMessageException thrown, if the message could not be processed properly
     */
    @Override
    public DefaultSuccessMAP handleValidated(InfrastructureComponentMAP messageAndPayload) throws RejectMessageException {
        Message msg = messageAndPayload.getMessage();
        try {
            if (msg instanceof ConnectorUpdateMessage) {
                if(messageAndPayload.getPayload().isPresent()) {
                    infrastructureComponentStatusHandler.updated(messageAndPayload.getPayload().get());

                }
                else
                {
                    throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE, new NullPointerException("Missing Payload in ConnectorUpdateMessage"));
                }
            }
            else if (msg instanceof ConnectorUnavailableMessage) {
                infrastructureComponentStatusHandler.unavailable(messageAndPayload.getMessage().getIssuerConnector());
            }
        }
        catch (Exception e) {
            if(e instanceof RejectMessageException)
            {
                //If it already is a RejectMessageException, throw it as-is
                throw (RejectMessageException) e;
            }
            //For some reason, ConnectExceptions sometimes do not provide an exception message.
            //This causes a NullPointerException and returns an HTTP 500
            logger.error("Failed to process message.", e);
            if(e.getMessage() == null)
            {
                try {
                    e = new Exception(e.getCause().getMessage());
                }
                catch (NullPointerException ignored)
                {
                    e = new Exception(e.getClass().getName() + " with empty message.");
                }
            }
            //Some unknown error has occurred, returning an internal error
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }

        try {
            //Let the connector know that the update was successful
            return new DefaultSuccessMAP(infrastructureComponent.getId(),
                    infrastructureComponent.getOutboundModelVersion(),
                    messageAndPayload.getMessage().getId(),
                    securityTokenProvider.getSecurityTokenAsDAT(),
                    responseSenderUri);
        }
        //Thrown in case the broker is unable to obtain its own security token from the DAPS
        catch (TokenRetrievalException e)
        {
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }
    }

    /**
     * This function provides a list of message types which are supported by this class
     * @return List of supported message types
     */
    @Override
    public Collection<Class<? extends Message>> getSupportedMessageTypes() {
        return Arrays.asList(
                ConnectorUnavailableMessage.class,
                ConnectorUpdateMessage.class);
    }
}
