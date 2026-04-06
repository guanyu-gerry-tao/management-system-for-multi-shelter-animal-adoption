package shelter.application;

import shelter.domain.Species;
import shelter.domain.VaccineType;
import shelter.service.model.OverdueVaccination;

import java.time.LocalDate;
import java.util.List;

/**
 * Application service for vaccination and vaccine type management use cases.
 * Orchestrates vaccine catalog lookups, record creation, overdue checks,
 * and audit logging for all vaccination-related operations.
 */
public interface VaccinationApplicationService {

    /**
     * Records that an animal received a specific vaccine on the given date.
     * Throws an exception if the animal is not found, the vaccine type name is not in the catalog,
     * or the vaccine is not applicable to the animal's species.
     *
     * @param animalId        the ID of the animal that was vaccinated; must not be null or blank
     * @param vaccineTypeName the name of the vaccine type administered; must not be null or blank
     * @param date            the date the vaccine was administered; must not be null
     */
    void recordVaccination(String animalId, String vaccineTypeName, LocalDate date);

    /**
     * Returns a list of vaccinations that are overdue for the given animal.
     * Throws an exception if the animal is not found.
     * Returns an empty list if all required vaccines are current.
     *
     * @param animalId the ID of the animal to check; must not be null or blank
     * @return a list of {@link OverdueVaccination} records, ordered by due date ascending
     */
    List<OverdueVaccination> getOverdueVaccinations(String animalId);

    /**
     * Adds a new vaccine type to the catalog with the given name, applicable species, and validity period.
     * Throws an exception if a vaccine type with the same name already exists.
     *
     * @param name              the vaccine type name (e.g., "Rabies"); must not be null or blank
     * @param applicableSpecies the species the vaccine applies to (e.g., "Dog"); must not be null or blank
     * @param validityDays      the number of days the vaccine remains valid; must be positive
     * @return the newly created {@link VaccineType}
     */
    VaccineType addVaccineType(String name, Species applicableSpecies, int validityDays);

    /**
     * Updates an existing vaccine type's fields by ID.
     * Only non-null parameters are applied; omitted (null) fields retain their current values.
     * Throws an exception if the vaccine type is not found or if the new name conflicts with an existing one.
     *
     * @param id                the ID of the vaccine type to update; must not be null or blank
     * @param name              the new name, or {@code null} to keep the current value
     * @param applicableSpecies the new applicable species, or {@code null} to keep the current value
     * @param validityDays      the new validity period in days, or {@code null} to keep the current value
     * @return the updated {@link VaccineType}
     */
    VaccineType updateVaccineType(String id, String name, Species applicableSpecies, Integer validityDays);

    /**
     * Removes a vaccine type from the catalog by ID.
     * Throws an exception if the vaccine type is not found.
     *
     * @param id the ID of the vaccine type to remove; must not be null or blank
     */
    void removeVaccineType(String id);

    /**
     * Returns a list of all vaccine types currently in the catalog.
     * Returns an empty list if no vaccine types have been added.
     *
     * @return a list of all {@link VaccineType} entries
     */
    List<VaccineType> listVaccineTypes();
}
