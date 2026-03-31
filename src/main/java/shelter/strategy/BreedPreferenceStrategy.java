package shelter.strategy;

import shelter.domain.Adopter;
import shelter.domain.Animal;

/**
 * A concrete matching strategy that evaluates whether an animal's breed
 * matches the adopter's preferred breed.
 */
public class BreedPreferenceStrategy implements IMatchingStrategy {

    /**
     * Returns the matching criterion handled by this strategy.
     *
     * @return {@link MatchingCriterion#BREED}
     */
    @Override
    public MatchingCriterion getCriterion() {
        return MatchingCriterion.BREED;
    }

    /**
     * Returns the score contributed by this strategy for the given adopter-animal pair.
     * A full match returns {@code 1.0}; otherwise this strategy returns {@code 0.0}.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return {@code 1.0} if the breed matches the adopter's preference;
     *         {@code 0.0} otherwise
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

        String preferredBreed = adopter.getPreferences().getPreferredBreed();
        if (preferredBreed == null || preferredBreed.isBlank()) {
            return 0.0;
        }

        return preferredBreed.equalsIgnoreCase(animal.getBreed()) ? 1.0 : 0.0;
    }
}
