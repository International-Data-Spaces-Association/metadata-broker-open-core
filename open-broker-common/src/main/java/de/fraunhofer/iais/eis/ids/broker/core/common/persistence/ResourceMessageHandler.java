package de.fraunhofer.iais.eis.ids.broker.core.common.persistence;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.TokenRetrievalException;
import de.fraunhofer.iais.eis.ids.component.core.map.DefaultSuccessMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.SameOriginResourceMapValidationStrategy;
import de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation.ValidatingMessageHandler;
import de.fraunhofer.iais.eis.ids.connector.commons.resource.map.ResourceMAP;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

/**
 * This class is a message handler for messages about the status of resources,
 * such as ResourceAvailableMessages, ResourceUpdateMessages, and ResourceUnavailableMessages
 */
public class ResourceMessageHandler extends ValidatingMessageHandler<ResourceMAP, DefaultSuccessMAP> {

    private final ResourceStatusHandler resourceStatusHandler;
    private final InfrastructureComponent infrastructureComponent;
    private final SecurityTokenProvider securityTokenProvider;
    private final URI responseSenderAgent;

    /**
     * Constructor
     * @param resourceStatusHandler The component which then takes care of persisting the changes
     * @param infrastructureComponent The broker as infrastructure component, such that appropriate responses can be sent
     * @param securityTokenProvider A security token provider for sending responses with a DAT
     * @param responseSenderAgent The "senderAgent" which should show in automatic response messages
     */
    public ResourceMessageHandler(ResourceStatusHandler resourceStatusHandler, InfrastructureComponent infrastructureComponent, SecurityTokenProvider securityTokenProvider, URI responseSenderAgent)
    {
        this.resourceStatusHandler = resourceStatusHandler;
        this.infrastructureComponent = infrastructureComponent;
        this.addMapValidationStrategy(new SameOriginResourceMapValidationStrategy());
        this.securityTokenProvider = securityTokenProvider;
        this.responseSenderAgent = responseSenderAgent;
    }

    /**
     * This function takes care of an inbound message which can be handled by this class
     * @param messageAndPayload The message to be handled
     * @return MessageProcessedNotification wrapped in a DefaultSuccessMAP, if the message has been processed properly
     * @throws RejectMessageException thrown, if the message could not be processed properly
     */
    @Override
    public DefaultSuccessMAP handleValidated(ResourceMAP messageAndPayload) throws RejectMessageException {
        Message msg = messageAndPayload.getMessage();
        try {
            /*if (msg instanceof ResourceAvailableMessage) {
                if (((ResourceAvailableMessage) msg).getAffectedResource() != null && messageAndPayload.getPayload().isPresent()) {
                    resourceStatusHandler.available(messageAndPayload.getPayload().get(), msg.getIssuerConnector());
                } else {
                    throw new RejectMessageException(RejectionReason.BAD_PARAMETERS, new NullPointerException("Affected Resource is null or payload is missing"));
                }
            } else */
            if (msg instanceof ResourceUpdateMessage) {
                if (((ResourceUpdateMessage) msg).getAffectedResource() != null && messageAndPayload.getPayload().isPresent()) {
                    resourceStatusHandler.updated(messageAndPayload.getPayload().get(), msg.getIssuerConnector());
                } else {
                    throw new RejectMessageException(RejectionReason.BAD_PARAMETERS, new NullPointerException("Affected Resource is null or payload is missing"));
                }
            } else if (msg instanceof ResourceUnavailableMessage) {
                if (((ResourceUnavailableMessage) msg).getAffectedResource() != null) {
                    resourceStatusHandler.unavailable(((ResourceUnavailableMessage) msg).getAffectedResource(), msg.getIssuerConnector());
                } else {
                    throw new RejectMessageException(RejectionReason.BAD_PARAMETERS, new NullPointerException("Affected Resource is null"));
                }
            }


        } catch (Exception e) {
            if (e instanceof RejectMessageException) {
                throw (RejectMessageException) e;
            }
            //For some reason, ConnectExceptions sometimes do not provide an exception message.
            //This causes a NullPointerException and returns an HTTP 500
            e.printStackTrace();
            if (e.getMessage() == null) {
                e = new Exception(e.getClass().getName() + " with empty message.");
            }
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }
        try {
            return new DefaultSuccessMAP(infrastructureComponent.getId(), infrastructureComponent.getOutboundModelVersion(), messageAndPayload.getMessage().getId(), securityTokenProvider.getSecurityTokenAsDAT(), responseSenderAgent);
        }
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
        return Arrays.asList(ResourceUpdateMessage.class, ResourceUnavailableMessage.class);
    }
}
