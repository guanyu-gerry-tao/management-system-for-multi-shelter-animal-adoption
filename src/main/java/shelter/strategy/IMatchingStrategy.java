package shelter.strategy;

import shelter.domain.Adopter;
import shelter.domain.Animal;

/**
 * Defines a scoring rule applied by the matching system to evaluate one
 * matching criterion for an adopter-animal pair.
 */
public interface IMatchingStrategy {

    /**
     * Returns the matching criterion handled by this strategy.
     *
     * @return the criterion evaluated by this strategy
     */
    MatchingCriterion getCriterion();

    /**
     * Returns the score contributed by this strategy for the given adopter-animal pair.
     * Implementations should return a normalized value, where larger values
     * indicate stronger compatibility under this criterion.
     *
     * @param adopter the adopter being evaluated
     * @param animal  the animal being evaluated
     * @return the score contributed by this strategy
     */
    double score(Adopter adopter, Animal animal);
}
