package shelter.application;

import shelter.domain.TransferRequest;

import java.util.List;

/**
 * Application service for the inter-shelter transfer request lifecycle.
 * Orchestrates availability checks, capacity validation, notifications, and audit logging
 * for each step of the transfer workflow (request, approve, reject, cancel).
 */
public interface TransferApplicationService {

    /**
     * Initiates a transfer request to move an available animal from one shelter to another.
     * Throws an exception if the animal is not found in the source shelter, is not available,
     * or if the destination shelter is at capacity.
     *
     * @param animalId      the ID of the animal to transfer; must not be null or blank
     * @param fromShelterId the ID of the source shelter; must not be null or blank
     * @param toShelterId   the ID of the destination shelter; must not be null or blank
     * @return the newly created {@link TransferRequest}
     */
    TransferRequest requestTransfer(String animalId, String fromShelterId, String toShelterId);

    /**
     * Approves a pending transfer request and moves the animal to the destination shelter.
     * Throws an exception if the request is not found or is not in a pending state.
     *
     * @param requestId the ID of the transfer request to approve; must not be null or blank
     */
    void approveTransfer(String requestId);

    /**
     * Rejects a pending transfer request, leaving the animal in the source shelter.
     * Throws an exception if the request is not found or is not in a pending state.
     *
     * @param requestId the ID of the transfer request to reject; must not be null or blank
     */
    void rejectTransfer(String requestId);

    /**
     * Cancels a pending transfer request that has not yet been reviewed.
     * Throws an exception if the request is not found or is not in a pending state.
     *
     * @param requestId the ID of the transfer request to cancel; must not be null or blank
     */
    void cancelTransfer(String requestId);

    /**
     * Returns every transfer request currently in the system.
     * Returns an empty list if no transfers have been requested.
     * Used by the {@code shelter print} command to render a system-wide snapshot.
     *
     * @return a list of all transfer requests
     */
    List<TransferRequest> listAllTransfers();
}
