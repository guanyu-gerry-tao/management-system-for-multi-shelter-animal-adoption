package shelter.service;

import shelter.domain.Animal;
import shelter.domain.VaccineType;
import shelter.service.model.OverdueVaccination;

import java.time.LocalDate;
import java.util.List;

/**
 * Tracks vaccination records for animals and identifies overdue vaccinations.
 * Implementations determine overdue status based on each VaccineType's validity period.
 */
public interface VaccinationService {

    /**
     * Records that an animal received a specific vaccine on the given date.
     * Throws an exception if any argument is null, or if the vaccine type is not applicable to the animal's species.
     *
     * @param animal      the animal that received the vaccine
     * @param vaccineType the type of vaccine administered
     * @param date        the date the vaccine was given
     */
    void recordVaccination(Animal animal, VaccineType vaccineType, LocalDate date);

    /**
     * Returns a list of overdue vaccinations for the given animal, including due dates and last administered dates.
     * Returns an empty list if all vaccinations are up to date.
     *
     * @param animal the animal to check
     * @return a list of overdue vaccination details
     */
    List<OverdueVaccination> getOverdueVaccinations(Animal animal);

    /**
     * Returns the vaccination record for the given unique record ID.
     * Throws an exception if no record with that ID is found.
     *
     * @param id the unique identifier of the vaccination record
     * @return the matching vaccination record
     */
    shelter.domain.VaccinationRecord findById(String id);
}
