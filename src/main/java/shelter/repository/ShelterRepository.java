package shelter.repository;

import shelter.domain.Shelter;

import java.util.List;
import java.util.Optional;

/**
 * Defines persistence operations for {@link Shelter} records.
 * Implementations are responsible for reading and writing shelter data to the underlying store,
 * without exposing any storage details to the service layer.
 */
public interface ShelterRepository {

    /**
     * Persists a new shelter or overwrites the existing record with the same ID.
     * Callers are responsible for ensuring the shelter is fully constructed before saving.
     *
     * @param shelter the shelter to save; must not be null
     */
    void save(Shelter shelter);

    /**
     * Returns the shelter with the given ID, or an empty optional if not found.
     * Does not throw if the ID is absent — callers decide how to handle the missing case.
     *
     * @param id the unique identifier to look up; must not be null or blank
     * @return an {@link Optional} containing the shelter, or empty if not found
     */
    Optional<Shelter> findById(String id);

    /**
     * Returns all shelters currently persisted in the store.
     * Returns an empty list if no shelters have been saved.
     *
     * @return a list of all shelters
     */
    List<Shelter> findAll();

    /**
     * Removes the shelter record with the given ID from the store.
     * Does nothing if no shelter with that ID exists.
     *
     * @param id the unique identifier of the shelter to delete; must not be null or blank
     */
    void delete(String id);
}
