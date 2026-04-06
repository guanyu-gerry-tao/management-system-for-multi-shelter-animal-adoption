package shelter.repository;

import java.util.List;
import java.util.Optional;

import shelter.domain.AdoptionRequest;
import shelter.domain.RequestStatus;

/**
 * Defines persistence operations for {@link AdoptionRequest} records.
 * Implementations are responsible for reading and writing adoption request data
 * to the underlying store, without exposing any storage details to the service
 * layer.
 */
public interface AdoptionRequestRepository {

    /**
     * Persists a new adoption request or overwrites the existing record with
     * the same ID. Callers are responsible for ensuring the request is fully
     * constructed before saving.
     *
     * @param request the adoption request to save; must not be null
     */
    void save(AdoptionRequest request);

    /**
     * Returns the adoption request with the given ID, or an empty optional if
     * not found. Does not throw if the ID is absent — callers decide how to
     * handle the missing case.
     *
     * @param id the unique identifier to look up; must not be null or blank
     * @return an {@link Optional} containing the request, or empty if not found
     */
    Optional<AdoptionRequest> findById(String id);

    /**
     * Returns all adoption requests currently persisted in the store. Returns
     * an empty list if no requests have been saved.
     *
     * @return a list of all adoption requests
     */
    List<AdoptionRequest> findAll();

    /**
     * Returns all adoption requests associated with the given adopter ID.
     * Returns an empty list if the adopter has no requests on record.
     *
     * @param adopterId the adopter ID to filter by; must not be null or blank
     * @return a list of adoption requests for the specified adopter
     */
    List<AdoptionRequest> findByAdopterId(String adopterId);

    /**
     * Returns all adoption requests for animals currently housed in the given
     * shelter. Shelter membership is determined by the animal's
     * {@code shelterId} field. Returns an empty list if no requests exist for
     * animals in that shelter.
     *
     * @param shelterId the shelter ID to filter by; must not be null or blank
     * @return a list of adoption requests for animals in the specified shelter
     */
    List<AdoptionRequest> findByShelterId(String shelterId);

    /**
     * Returns all adoption requests with the given status. Returns an empty
     * list if no requests match the given status.
     *
     * @param status the request status to filter by; must not be null
     * @return a list of adoption requests with the specified status
     */
    List<AdoptionRequest> findByStatus(RequestStatus status);

    /**
     * Returns all adoption requests associated with the given adopter ID and
     * status. Returns an empty list if no requests match the given criteria.
     *
     * @param adopterId the adopter ID to filter by; must not be null or blank
     * @param status the request status to filter by; must not be null
     * @return a list of adoption requests for the specified adopter with the
     * specified status
     */
    List<AdoptionRequest> findByAdopterIdAndStatus(String adopterId, RequestStatus status);

    /**
     * Returns all adoption requests for animals in the given shelter that match
     * the given status. Combines shelter and status filtering in one query to
     * avoid secondary filtering in the service layer. Returns an empty list if
     * no matching requests are found.
     *
     * @param shelterId the shelter ID to filter by; must not be null or blank
     * @param status the request status to filter by; must not be null
     * @return a list of adoption requests for the specified shelter with the
     * specified status
     */
    List<AdoptionRequest> findByShelterIdAndStatus(String shelterId, RequestStatus status);

    /**
     * Removes the adoption request record with the given ID from the store.
     * Does nothing if no request with that ID exists.
     *
     * @param id the unique identifier of the request to delete; must not be
     * null or blank
     */
    void delete(String id);
}
