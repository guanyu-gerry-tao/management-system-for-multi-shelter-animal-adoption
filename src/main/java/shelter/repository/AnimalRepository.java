package shelter.repository;

import shelter.domain.Animal;

import java.util.List;
import java.util.Optional;

/**
 * Defines persistence operations for {@link Animal} records.
 * Implementations are responsible for reading and writing animal data to the underlying store,
 * without exposing any storage details to the service layer.
 */
public interface AnimalRepository {

    /**
     * Persists a new animal or overwrites the existing record with the same ID.
     * Callers are responsible for ensuring the animal is fully constructed before saving.
     *
     * @param animal the animal to save; must not be null
     */
    void save(Animal animal);

    /**
     * Returns the animal with the given ID, or an empty optional if not found.
     * Does not throw if the ID is absent — callers decide how to handle the missing case.
     *
     * @param id the unique identifier to look up; must not be null or blank
     * @return an {@link Optional} containing the animal, or empty if not found
     */
    Optional<Animal> findById(String id);

    /**
     * Returns all animals currently persisted in the store.
     * Returns an empty list if no animals have been saved.
     *
     * @return a list of all animals
     */
    List<Animal> findAll();

    /**
     * Returns all animals whose shelter ID matches the given value.
     * Returns an empty list if no animals are associated with that shelter.
     *
     * @param shelterId the shelter ID to filter by; must not be null or blank
     * @return a list of animals in the specified shelter
     */
    List<Animal> findByShelterId(String shelterId);

    /**
     * Returns all animals that have been adopted by the adopter with the given ID.
     * Returns an empty list if the adopter has not adopted any animals.
     *
     * @param adopterId the adopter ID to filter by; must not be null or blank
     * @return a list of animals adopted by the specified adopter
     */
    List<Animal> findByAdopterId(String adopterId);

    /**
     * Removes the animal record with the given ID from the store.
     * Does nothing if no animal with that ID exists.
     *
     * @param id the unique identifier of the animal to delete; must not be null or blank
     */
    void delete(String id);
}
