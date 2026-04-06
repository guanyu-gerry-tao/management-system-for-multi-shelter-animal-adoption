package shelter.repository;

import java.util.List;
import java.util.Optional;

import shelter.domain.RequestStatus;
import shelter.domain.TransferRequest;

/**
 * Defines persistence operations for {@link TransferRequest} records.
 * Implementations are responsible for reading and writing transfer request data to the underlying store,
 * without exposing any storage details to the service layer.
 */
public interface TransferRequestRepository {

    /**
     * Persists a new transfer request or overwrites the existing record with the same ID.
     * Callers are responsible for ensuring the request is fully constructed before saving.
     *
     * @param request the transfer request to save; must not be null
     */
    void save(TransferRequest request);

    /**
     * Returns the transfer request with the given ID, or an empty optional if not found.
     * Does not throw if the ID is absent — callers decide how to handle the missing case.
     *
     * @param id the unique identifier to look up; must not be null or blank
     * @return an {@link Optional} containing the request, or empty if not found
     */
    Optional<TransferRequest> findById(String id);

    /**
     * Returns all transfer requests currently persisted in the store.
     * Returns an empty list if no requests have been saved.
     *
     * @return a list of all transfer requests
     */
    List<TransferRequest> findAll();

    /**
     * Returns all transfer requests associated with the given animal ID.
     * Returns an empty list if no requests exist for that animal.
     *
     * @param animalId the animal ID to filter by; must not be null or blank
     * @return a list of transfer requests for the specified animal
     */
    List<TransferRequest> findByAnimalId(String animalId);

    /**
     * Returns all transfer requests originating from the given shelter ID.
     * Returns an empty list if no requests originated from that shelter.
     *
     * @param shelterId the source shelter ID to filter by; must not be null or blank
     * @return a list of transfer requests from the specified shelter
     */
    List<TransferRequest> findByFromShelterId(String shelterId);

    /**
     * Returns all transfer requests targeting the given shelter ID as the destination.
     * Returns an empty list if no requests are destined for that shelter.
     *
     * @param shelterId the destination shelter ID to filter by; must not be null or blank
     * @return a list of transfer requests targeting the specified shelter
     */
    List<TransferRequest> findByToShelterId(String shelterId);

    /**
     * Returns all transfer requests with the given status.
     * Returns an empty list if no requests match the given status.
     *
     * @param status the request status to filter by; must not be null
     * @return a list of transfer requests with the specified status
     */
    List<TransferRequest> findByStatus(RequestStatus status);

    /**
     * Returns all transfer requests involving the given shelter as either source or destination, with the given status.
     * Combines shelter and status filtering in one query to avoid secondary filtering in the service layer.
     * Returns an empty list if no matching requests are found.
     *
     * @param shelterId the shelter ID to filter by (matched against both source and destination); must not be null or blank
     * @param status    the request status to filter by; must not be null
     * @return a list of transfer requests involving the specified shelter with the specified status
     */
    List<TransferRequest> findByShelterIdAndStatus(String shelterId, RequestStatus status);

    /**
     * Returns all transfer requests for the given animal ID with the given status.
     * Combines animal and status filtering in one query to avoid secondary filtering in the service layer.
     * Returns an empty list if no matching requests are found.
     *
     * @param animalId the animal ID to filter by; must not be null or blank
     * @param status   the request status to filter by; must not be null
     * @return a list of transfer requests for the specified animal with the specified status
     */
    List<TransferRequest> findByAnimalIdAndStatus(String animalId, RequestStatus status);

    /**
     * Removes the transfer request record with the given ID from the store.
     * Does nothing if no request with that ID exists.
     *
     * @param id the unique identifier of the request to delete; must not be null or blank
     */
    void delete(String id);
}
