package shelter.strategy;

import shelter.domain.Adopter;
import shelter.domain.Animal;
import shelter.domain.Species;

/**
 * A concrete matching strategy that evaluates whether an animal's species
 * matches the adopter's preferred species.
 */
public class SpeciesPreferenceStrategy extends AbstractBinaryMatchingStrategy {

    /**
     * Constructs a new SpeciesPreferenceStrategy instance.
     * This strategy is stateless and requires no initialization parameters.
     */
    public SpeciesPreferenceStrategy() {}

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
     * Returns whether the adopter has set a species preference.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return {@code true} if the adopter has a species preference; {@code false} otherwise
     * @throws IllegalArgumentException if {@code adopter} or {@code animal} is {@code null}
     */
    @Override
    public boolean isApplicable(Adopter adopter, Animal animal) {
        validateInputs(adopter, animal);

        return adopter.getPreferences().getPreferredSpecies() != null;
    }

    /**
     * Returns whether the animal's species matches the adopter's preferred species.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return {@code true} if the species matches the adopter's preference
     */
    @Override
    protected boolean isMatch(Adopter adopter, Animal animal) {
        Species preferredSpecies = adopter.getPreferences().getPreferredSpecies();
        return preferredSpecies == animal.getSpecies();
    }

}
