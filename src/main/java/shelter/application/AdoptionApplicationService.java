package shelter.application;

import shelter.domain.AdoptionRequest;

/**
 * Application service for the adoption request lifecycle.
 * Orchestrates validation, state transitions, notifications, and audit logging
 * for each step of the adoption workflow (submit, approve, reject, cancel).
 */
public interface AdoptionApplicationService {

    /**
     * Submits a new adoption request on behalf of the given adopter for the given animal.
     * Throws an exception if the adopter or animal is not found, or if the animal is not available.
     *
     * @param adopterId the ID of the adopter submitting the request; must not be null or blank
     * @param animalId  the ID of the animal being requested; must not be null or blank
     * @return the newly created {@link AdoptionRequest}
     */
    AdoptionRequest submitRequest(String adopterId, String animalId);

    /**
     * Approves a pending adoption request, marking the animal as adopted and notifying the adopter.
     * Throws an exception if the request is not found or is not in a pending state.
     *
     * @param requestId the ID of the adoption request to approve; must not be null or blank
     */
    void approveRequest(String requestId);

    /**
     * Rejects a pending adoption request, leaving the animal available for other requests.
     * Throws an exception if the request is not found or is not in a pending state.
     *
     * @param requestId the ID of the adoption request to reject; must not be null or blank
     */
    void rejectRequest(String requestId);

    /**
     * Cancels a pending adoption request before it has been reviewed.
     * Throws an exception if the request is not found or is not in a pending state.
     *
     * @param requestId the ID of the adoption request to cancel; must not be null or blank
     */
    void cancelRequest(String requestId);
}
