package de.fraunhofer.iais.eis.ids.index.common.persistence;

import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.Participant;

import java.io.IOException;
import java.net.URI;

/**
 * Interface for providing indexing functionality for infrastructure components and participants
 * Note that Resources are handled by the ResourceStatusHandler instead, as it is broker specific and not common with the ParIS
 */
public interface Indexing {

    /**
     * Function for adding an infrastructure component, such as a connector, to the index
     * @param infrastructureComponent The infrastructure component to be indexed
     * @throws IOException may be thrown on error
     */
    void add(InfrastructureComponent infrastructureComponent) throws IOException;

    /**
     * Function for updating an already indexed infrastructure component
     * @param infrastructureComponent The infrastructure component in its current form
     * @throws IOException may be thrown if the infrastructure component could not be updated, e.g. because it was not found
     */
    void update(InfrastructureComponent infrastructureComponent) throws IOException;

    /**
     * Function for removing an indexed infrastructure component OR participant from the index
     * @param componentId A reference to the infrastructure component to be removed
     * @throws IOException if the infrastructure component could not be deleted, e.g. because it was not found
     */
    void delete(URI componentId) throws IOException;

    /**
     * Function for adding a participant to the index
     * @param participant The participant to be indexed
     * @throws IOException may be thrown on error
     */
    void add(Participant participant) throws IOException;

    /**
     * Function for updating an already indexed participant
     * @param participant The participant in its current form
     * @throws IOException may be thrown if the participant could not be updated, e.g. because it was not found
     */
    void update(Participant participant) throws IOException;

    /**
     * Function for removing an indexed participant from the index
     * Note that this function is NEVER USED as a ParticipantUnavailableMessage only contains a URI of the participant to be deleted
     * Use delete(URI componentId) instead
     * @param participant The participant to be removed
     * @throws IOException if the participant could not be deleted, e.g. because it was not found
     */
    @Deprecated
    void delete(Participant participant) throws IOException;

    /**
     * Function for recreating the entire index from the current state of the repository (triple store). This helps keeping database and index in sync
     * @throws IOException may be thrown if an exception occurs during the dropping or recreation of the index
     */
    void recreateIndex() throws IOException;

}
