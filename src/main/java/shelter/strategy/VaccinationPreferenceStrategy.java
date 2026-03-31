package shelter.strategy;

import shelter.domain.Adopter;
import shelter.domain.Animal;

/**
 * A concrete matching strategy related to vaccination status.
 * At this stage, the adopter-side vaccination requirement has not been modeled yet,
 * so this strategy provisionally rewards animals that are already vaccinated.
 * TODO: revise this strategy after the team decides where vaccination preference
 * should be stored in the adopter-side matching data.
 */
public class VaccinationPreferenceStrategy implements IMatchingStrategy {

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
     * Returns the score contributed by this strategy for the given adopter-animal pair.
     * Currently, vaccinated animals receive {@code 1.0} and unvaccinated animals
     * receive {@code 0.0}. This scoring rule can be revised later when a user-side
     * vaccination preference is added to the design.
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

        // For the current first-stage design, vaccinated animals are treated as the stronger match.
        return animal.isVaccinated() ? 1.0 : 0.0;
    }
}
