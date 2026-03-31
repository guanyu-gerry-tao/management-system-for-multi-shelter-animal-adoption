package shelter.strategy;

import shelter.domain.Adopter;
import shelter.domain.Animal;

/**
 * A concrete matching strategy that evaluates whether an animal's age
 * fits the adopter's preferred age range.
 */
public class AgePreferenceStrategy implements IMatchingStrategy {

    /**
     * Returns the matching criterion handled by this strategy.
     *
     * @return {@link MatchingCriterion#AGE}
     */
    @Override
    public MatchingCriterion getCriterion() {
        return MatchingCriterion.AGE;
    }

    /**
     * Returns the score contributed by this strategy for the given adopter-animal pair.
     * An animal within the preferred age range returns {@code 1.0}. An animal
     * just outside the preferred range returns {@code 0.5}. Otherwise, the score is {@code 0.0}.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return the compatibility score for age preference
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

        int minAge = adopter.getPreferences().getMinAge();
        int maxAge = adopter.getPreferences().getMaxAge();
        int animalAge = animal.getAge();

        if (animalAge >= minAge && animalAge <= maxAge) {
            return 1.0;
        }

        if (animalAge == minAge - 1 || animalAge == maxAge + 1) {
            return 0.5;
        }

        return 0.0;
    }
}
