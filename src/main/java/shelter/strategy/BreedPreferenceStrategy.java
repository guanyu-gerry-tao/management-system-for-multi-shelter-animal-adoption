package shelter.strategy;

import shelter.domain.Adopter;
import shelter.domain.Animal;

/**
 * A concrete matching strategy that evaluates whether an animal's breed
 * matches the adopter's preferred breed.
 */
public class BreedPreferenceStrategy extends AbstractBinaryMatchingStrategy {

    /**
     * Constructs a new BreedPreferenceStrategy instance.
     * This strategy is stateless and requires no initialization parameters.
     */
    public BreedPreferenceStrategy() {}

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
     * Returns whether the adopter has set a breed preference.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return {@code true} if the adopter has a breed preference; {@code false} otherwise
     * @throws IllegalArgumentException if {@code adopter} or {@code animal} is {@code null}
     */
    @Override
    public boolean isApplicable(Adopter adopter, Animal animal) {
        validateInputs(adopter, animal);

        String preferredBreed = adopter.getPreferences().getPreferredBreed();
        return preferredBreed != null && !preferredBreed.isBlank();
    }

    /**
     * Returns whether the animal's breed matches the adopter's preferred breed.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return {@code true} if the breed matches the adopter's preference
     */
    @Override
    protected boolean isMatch(Adopter adopter, Animal animal) {
        String preferredBreed = adopter.getPreferences().getPreferredBreed();
        return preferredBreed.equalsIgnoreCase(animal.getBreed());
    }
}
