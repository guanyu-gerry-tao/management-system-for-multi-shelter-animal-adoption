package shelter.repository;

import shelter.domain.VaccinationRecord;

import java.util.List;
import java.util.Optional;

/**
 * Defines persistence operations for {@link VaccinationRecord} records.
 * Implementations are responsible for reading and writing vaccination history to the underlying store,
 * without exposing any storage details to the service layer.
 */
public interface VaccinationRecordRepository {

    /**
     * Persists a new vaccination record or overwrites the existing record with the same ID.
     * Callers are responsible for ensuring the record is fully constructed before saving.
     *
     * @param record the vaccination record to save; must not be null
     */
    void save(VaccinationRecord record);

    /**
     * Returns the vaccination record with the given ID, or an empty optional if not found.
     * Does not throw if the ID is absent — callers decide how to handle the missing case.
     *
     * @param id the unique identifier to look up; must not be null or blank
     * @return an {@link Optional} containing the record, or empty if not found
     */
    Optional<VaccinationRecord> findById(String id);

    /**
     * Returns all vaccination records for the given animal ID.
     * Returns an empty list if the animal has no vaccination history.
     *
     * @param animalId the animal ID to filter by; must not be null or blank
     * @return a list of vaccination records for the specified animal
     */
    List<VaccinationRecord> findByAnimalId(String animalId);

    /**
     * Returns all vaccination records currently persisted in the store.
     * Returns an empty list if no records have been saved.
     *
     * @return a list of all vaccination records
     */
    List<VaccinationRecord> findAll();

    /**
     * Removes the vaccination record with the given ID from the store.
     * Does nothing if no record with that ID exists.
     *
     * @param id the unique identifier of the record to delete; must not be null or blank
     */
    void delete(String id);
}
