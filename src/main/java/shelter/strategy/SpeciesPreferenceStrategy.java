package shelter.strategy;

import shelter.domain.Adopter;
import shelter.domain.Animal;
import shelter.domain.Species;

/**
 * A concrete matching strategy that evaluates whether an animal's species
 * matches the adopter's preferred species.
 */
public class SpeciesPreferenceStrategy implements IMatchingStrategy {

    /**
     * Returns the matching criterion handled by this strategy.
     *
     * @return {@link MatchingCriterion#SPECIES}
     */
    @Override
    public MatchingCriterion getCriterion() {
        return MatchingCriterion.SPECIES;
    }

    /**
     * Returns the score contributed by this strategy for the given adopter-animal pair.
     * A full match returns {@code 1.0}; otherwise this strategy returns {@code 0.0}.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return {@code 1.0} if the species matches the adopter's preference;
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

        Species preferredSpecies = adopter.getPreferences().getPreferredSpecies();
        if (preferredSpecies == null) {
            return 0.0;
        }

        return preferredSpecies == animal.getSpecies() ? 1.0 : 0.0;
    }

}
