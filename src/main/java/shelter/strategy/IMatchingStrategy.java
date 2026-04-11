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
     * Returns whether this strategy should contribute to the current adopter-animal match.
     * A strategy is not applicable when the adopter did not express a preference for the
     * criterion, or when the current design does not yet support evaluating that criterion.
     *
     * @param adopter the adopter being evaluated
     * @param animal  the animal being evaluated
     * @return {@code true} if this criterion should be counted in the total score;
     *         {@code false} otherwise
     */
    boolean isApplicable(Adopter adopter, Animal animal);

    /**
     * Returns the score contributed by this strategy for the given adopter-animal pair.
     * Implementations should return a normalized value, where larger values
     * indicate stronger compatibility under this criterion.
     * This method is expected to be called only when {@link #isApplicable(Adopter, Animal)}
     * returns {@code true}.
     *
     * @param adopter the adopter being evaluated
     * @param animal  the animal being evaluated
     * @return the score contributed by this strategy
     */
    double score(Adopter adopter, Animal animal);
}
