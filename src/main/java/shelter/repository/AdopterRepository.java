package shelter.repository;

import shelter.domain.Adopter;

import java.util.List;
import java.util.Optional;

/**
 * Defines persistence operations for {@link Adopter} records.
 * Implementations are responsible for reading and writing adopter data to the underlying store,
 * without exposing any storage details to the service layer.
 */
public interface AdopterRepository {

    /**
     * Persists a new adopter or overwrites the existing record with the same ID.
     * Callers are responsible for ensuring the adopter is fully constructed before saving.
     *
     * @param adopter the adopter to save; must not be null
     */
    void save(Adopter adopter);

    /**
     * Returns the adopter with the given ID, or an empty optional if not found.
     * Does not throw if the ID is absent — callers decide how to handle the missing case.
     *
     * @param id the unique identifier to look up; must not be null or blank
     * @return an {@link Optional} containing the adopter, or empty if not found
     */
    Optional<Adopter> findById(String id);

    /**
     * Returns all adopters currently persisted in the store.
     * Returns an empty list if no adopters have been saved.
     *
     * @return a list of all adopters
     */
    List<Adopter> findAll();

    /**
     * Removes the adopter record with the given ID from the store.
     * Does nothing if no adopter with that ID exists.
     *
     * @param id the unique identifier of the adopter to delete; must not be null or blank
     */
    void delete(String id);
}
