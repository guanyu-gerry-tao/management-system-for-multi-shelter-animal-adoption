package shelter.service;

import shelter.domain.Animal;
import shelter.domain.Shelter;
import shelter.domain.TransferRequest;

import java.util.List;

/**
 * Handles inter-shelter animal transfers, including creating and resolving transfer requests.
 * Implementations must verify animal availability and update shelter records upon approval.
 */
public interface TransferService {

    /**
     * Creates and submits a transfer request to move an animal from one shelter to another.
     * Throws an exception if the animal is not present in the source shelter or the destination is full.
     *
     * @param animal the animal to transfer
     * @param from   the source shelter currently holding the animal
     * @param to     the destination shelter to receive the animal
     * @return the created transfer request
     */
    TransferRequest requestTransfer(Animal animal, Shelter from, Shelter to);

    /**
     * Approves a pending transfer request and moves the animal between shelters.
     * Throws an exception if the request is not in a pending state.
     *
     * @param request the transfer request to approve
     */
    void approve(TransferRequest request);

    /**
     * Rejects a pending transfer request without moving the animal.
     * Throws an exception if the request is not in a pending state.
     *
     * @param request the transfer request to reject
     */
    void reject(TransferRequest request);

    /**
     * Dismisses a pending transfer request without approving or rejecting it.
     * Throws an exception if the request is not in a pending state.
     *
     * @param request the transfer request to dismiss
     */
    void dismiss(TransferRequest request);

    /**
     * Returns all pending transfer requests involving the given shelter, either as source or destination.
     * Returns an empty list if the shelter has no pending transfer requests.
     *
     * @param shelter the shelter to query
     * @return a list of pending transfer requests involving the shelter
     */
    List<TransferRequest> getPendingRequests(Shelter shelter);

    /**
     * Returns every transfer request currently persisted in the system.
     * Returns an empty list if no transfers have been requested.
     * Used by the presentation layer to render a full snapshot of the transfer queue.
     *
     * @return a list of all transfer requests
     */
    List<TransferRequest> listAll();
}
