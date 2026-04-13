package shelter.service;

import java.util.List;

import shelter.domain.Animal;
import shelter.domain.VaccineType;
import shelter.service.model.OverdueVaccination;

/**
 * A narrow abstraction that exposes only the vaccination facts needed by
 * matching strategies.
 *
 * <p>This keeps strategy classes from depending on the full vaccination service
 * interface while still allowing richer vaccination-based matching logic.</p>
 */
public interface VaccinationInfoProvider {

    /**
     * Returns all vaccine types applicable to the given animal's species.
     *
     * @param animal the animal to query
     * @return the list of vaccine types applicable to the animal
     */
    List<VaccineType> getApplicableVaccineTypes(Animal animal);

    /**
     * Returns the overdue vaccinations for the given animal.
     *
     * @param animal the animal to query
     * @return the list of overdue vaccinations for the animal
     */
    List<OverdueVaccination> getOverdueVaccinations(Animal animal);
}
