package de.fraunhofer.iais.eis.ids.broker.core.common.impl;


import de.fraunhofer.iais.eis.ids.connector.commons.broker.map.InfrastructureComponentMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation.MapValidationException;
import de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation.MapValidationStrategy;

import java.net.URL;

/**
 * This class provides validation mechanisms that the sender of a ConnectorUnavailableMessage is authorized to de-register the connector in question
 * TODO: This class does not do anything yet. Validation to be implemented. This should not be a ton of work...
 */
public class ConnectorUnavailableValidationStrategy implements MapValidationStrategy<InfrastructureComponentMAP> {

    @Override
    public void validate(InfrastructureComponentMAP map) throws MapValidationException {

        /*
         * Unregistration request is successful only if (1) the connector unregisters itself or (2) the request originates
         * from a connector that is already registered at the broker AND has the same maintainer participant as the connector
         * that should be unregistered
         */

        /*
        Message msg = map.getMessage();
        if (msg instanceof ConnectorUnavailableMessage) {
            URI issuer = msg.getIssuerConnector();
            URI affectedConnector = ((ConnectorUnavailableMessage) msg).getAffectedConnector();

            if (
                // connector indicates unavailability of itself
                payload.filter(component -> component.getId().equals(issuer)).isPresent() ||

                // unavailability is indicated by a connector that (i) is already registered and (ii) has the same maintainer
                // than the connector that should be indicated as unavailable.
                payload.filter(component ->
                        // (i)
                        requestingConnectorIsRegistered(issuer) &&

                        // (ii)
                        toBeUnregisteredConnectorHasMaintainer()));
        }

        map.getPayload();
        */
    }

    private boolean requestingConnectorIsRegistered(URL connector) {
        return false;
    }

    private boolean toBeUnregisteredConnectorHasMaintainer(URL unavailableConnector, URL maintainer) {
        return false;
    }


}
