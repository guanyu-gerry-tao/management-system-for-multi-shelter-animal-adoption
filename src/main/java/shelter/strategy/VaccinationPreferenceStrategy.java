package shelter.strategy;

import java.util.List;

import shelter.domain.Adopter;
import shelter.domain.Animal;
import shelter.domain.VaccineType;
import shelter.service.VaccinationInfoProvider;
import shelter.service.model.OverdueVaccination;

/**
 * A concrete matching strategy related to vaccination status.
 * This strategy only participates when the adopter explicitly requires
 * vaccinated animals. It uses a {@link VaccinationInfoProvider} to compare
 * the animal's vaccination records against the vaccine types that apply to
 * that animal's species.
 */
public class VaccinationPreferenceStrategy implements IMatchingStrategy {

    private final VaccinationInfoProvider vaccinationInfoProvider;

    /**
     * Constructs the strategy with a provider that supplies vaccination facts
     * needed for scoring.
     *
     * @param vaccinationInfoProvider the provider used to query vaccination facts
     * @throws IllegalArgumentException if {@code vaccinationInfoProvider} is {@code null}
     */
    public VaccinationPreferenceStrategy(VaccinationInfoProvider vaccinationInfoProvider) {
        if (vaccinationInfoProvider == null) {
            throw new IllegalArgumentException("VaccinationInfoProvider must not be null.");
        }
        this.vaccinationInfoProvider = vaccinationInfoProvider;
    }

    /**
     * Returns the matching criterion handled by this strategy.
     *
     * @return {@link MatchingCriterion#VACCINATION}
     */
    @Override
    public MatchingCriterion getCriterion() {
        return MatchingCriterion.VACCINATION;
    }

    /**
     * Returns whether vaccination should be counted in the total score.
     * This criterion is only applicable when the adopter explicitly requires
     * vaccinated animals as part of their preferences.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return {@code true} only when vaccinated animals are required
     * @throws IllegalArgumentException if {@code adopter} or {@code animal} is {@code null}
     */
    @Override
    public boolean isApplicable(Adopter adopter, Animal animal) {
        if (adopter == null) {
            throw new IllegalArgumentException("Adopter must not be null.");
        }
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }

        return Boolean.TRUE.equals(adopter.getPreferences().getRequiresVaccinated());
    }

    /**
     * Returns the score contributed by this strategy for the given adopter-animal pair.
     * With a {@link VaccinationInfoProvider}, scoring is based on applicable vaccine types:
     * valid vaccine types contribute {@code 1.0}, overdue vaccine types contribute
     * {@code 0.5}, and missing vaccine types contribute {@code 0.0}. If there are no
     * applicable vaccine types, the score is {@code 1.0}.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return the compatibility score for vaccination status
     * @throws IllegalArgumentException if {@code adopter} or {@code animal} is {@code null}
     */
    @Override
    public double score(Adopter adopter, Animal animal) {
        if (adopter == null) {
            throw new IllegalArgumentException("Adopter must not be null.");
        }
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }

        List<VaccineType> applicableTypes = vaccinationInfoProvider.getApplicableVaccineTypes(animal);
        if (applicableTypes.isEmpty()) {
            return 1.0;
        }

        List<OverdueVaccination> overdueVaccinations =
                vaccinationInfoProvider.getOverdueVaccinations(animal);

        int overdueCount = 0;
        int missingCount = 0;
        for (OverdueVaccination overdueVaccination : overdueVaccinations) {
            if (overdueVaccination.getLastAdministered() == null) {
                missingCount++;
            } else {
                overdueCount++;
            }
        }

        int validCount = applicableTypes.size() - overdueCount - missingCount;
        double weightedScore = validCount + (overdueCount * 0.5);
        return weightedScore / applicableTypes.size();
    }
}
