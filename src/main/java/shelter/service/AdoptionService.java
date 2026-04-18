package shelter.service;

import shelter.domain.AdoptionRequest;
import shelter.domain.Adopter;
import shelter.domain.Animal;
import shelter.domain.Shelter;

import java.time.LocalDate;
import java.util.List;

/**
 * Manages the full lifecycle of adoption requests, from initial submission
 * through approval or rejection. Implementations are responsible for validating
 * requests and updating request state accordingly.
 */
public interface AdoptionService {

    /**
     * Submits a new adoption request into the system for review.
     * Throws an exception if the request is invalid or the animal is unavailable.
     *
     * @param request the adoption request to submit
     */
    void submit(AdoptionRequest request);

    /**
     * Approves a pending adoption request and marks the animal as adopted.
     * Throws an exception if the request is not in a pending state.
     *
     * @param request the adoption request to approve
     */
    void approve(AdoptionRequest request);

    /**
     * Rejects a pending adoption request with no further action on the animal.
     * Throws an exception if the request is not in a pending state.
     *
     * @param request the adoption request to reject
     */
    void reject(AdoptionRequest request);

    /**
     * Cancels a pending adoption request at the adopter's request.
     * Throws an exception if the request is not in a pending state.
     *
     * @param request the adoption request to cancel
     */
    void cancel(AdoptionRequest request);

    /**
     * Returns all adoption requests submitted by the given adopter.
     * Returns an empty list if the adopter has no requests on record.
     *
     * @param adopter the adopter whose requests to retrieve
     * @return a list of adoption requests belonging to the adopter
     */
    List<AdoptionRequest> getRequestsByAdopter(Adopter adopter);

    /**
     * Returns all adoption requests associated with animals in the given shelter.
     * Returns an empty list if the shelter has no adoption requests.
     *
     * @param shelter the shelter to query
     * @return a list of adoption requests for animals in the shelter
     */
    List<AdoptionRequest> getRequestsByShelter(Shelter shelter);

    /**
     * Returns all adoption requests for the given animal across all adopters.
     * Returns an empty list if no requests exist for the animal.
     *
     * @param animal the animal to query
     * @return a list of adoption requests targeting the animal
     */
    List<AdoptionRequest> getRequestsByAnimal(Animal animal);

    /**
     * Returns all adoption requests submitted after the given date.
     * Returns an empty list if no requests exist after that date.
     *
     * @param date the cutoff date (exclusive)
     * @return a list of adoption requests submitted after the date
     */
    List<AdoptionRequest> getRequestsAfter(LocalDate date);

    /**
     * Returns all approved adoption requests where approval occurred after the given date.
     * Returns an empty list if no approvals exist after that date.
     *
     * @param date the cutoff date (exclusive)
     * @return a list of approved adoption requests after the date
     */
    List<AdoptionRequest> getApprovedAfter(LocalDate date);

    /**
     * Returns every adoption request currently persisted in the system.
     * Returns an empty list if no requests have been submitted.
     * Used by the presentation layer to render a full snapshot of the adoption queue.
     *
     * @return a list of all adoption requests
     */
    List<AdoptionRequest> listAll();
}
