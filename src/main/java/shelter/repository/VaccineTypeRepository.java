package shelter.repository;

import shelter.domain.Species;
import shelter.domain.VaccineType;

import java.util.List;
import java.util.Optional;

/**
 * Defines persistence operations for {@link VaccineType} records.
 * Implementations are responsible for reading and writing vaccine type catalog data to the underlying store,
 * without exposing any storage details to the service layer.
 */
public interface VaccineTypeRepository {

    /**
     * Persists a new vaccine type or overwrites the existing record with the same ID.
     * Callers are responsible for ensuring the vaccine type is fully constructed before saving.
     *
     * @param vaccineType the vaccine type to save; must not be null
     */
    void save(VaccineType vaccineType);

    /**
     * Returns the vaccine type with the given ID, or an empty optional if not found.
     * Does not throw if the ID is absent — callers decide how to handle the missing case.
     *
     * @param id the unique identifier to look up; must not be null or blank
     * @return an {@link Optional} containing the vaccine type, or empty if not found
     */
    Optional<VaccineType> findById(String id);

    /**
     * Returns the vaccine type with the given name, or an empty optional if not found.
     * Name matching is case-insensitive to prevent near-duplicate entries in the catalog.
     *
     * @param name the vaccine type name to look up; must not be null or blank
     * @return an {@link Optional} containing the vaccine type, or empty if not found
     */
    Optional<VaccineType> findByName(String name);

    /**
     * Returns all vaccine types applicable to the given species.
     * Returns an empty list if no vaccine types are defined for that species.
     *
     * @param species the {@link Species} to filter by; must not be null
     * @return a list of vaccine types applicable to the specified species
     */
    List<VaccineType> findByApplicableSpecies(Species species);

    /**
     * Returns all vaccine types currently persisted in the store.
     * Returns an empty list if the catalog is empty.
     *
     * @return a list of all vaccine types
     */
    List<VaccineType> findAll();

    /**
     * Removes the vaccine type record with the given ID from the store.
     * Does nothing if no vaccine type with that ID exists.
     *
     * @param id the unique identifier of the vaccine type to delete; must not be null or blank
     */
    void delete(String id);
}
