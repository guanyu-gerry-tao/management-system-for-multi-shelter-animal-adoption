package shelter.strategy;

import shelter.domain.Adopter;
import shelter.domain.Animal;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates the final match score by combining applicable strategy scores.
 * Ranked criteria receive stronger influence, while applicable but unranked
 * criteria still contribute with a normal baseline weight.
 */
public class MatchingScoreCalculator {

    private static final double DEFAULT_WEIGHT = 1.0;

    private final List<IMatchingStrategy> strategies;
    private final MatchingPreferencesProfile profile;

    /**
     * Constructs a calculator with strategies and an optional priority profile.
     *
     * @param strategies the strategies used to score each adopter-animal pair
     * @param profile the optional ranked preference profile; may be {@code null}
     * @throws IllegalArgumentException if {@code strategies} is {@code null}, empty,
     *                                  or contains {@code null}
     */
    public MatchingScoreCalculator(List<IMatchingStrategy> strategies,
                                   MatchingPreferencesProfile profile) {
        if (strategies == null || strategies.isEmpty()) {
            throw new IllegalArgumentException("At least one matching strategy must be provided.");
        }
        for (IMatchingStrategy strategy : strategies) {
            if (strategy == null) {
                throw new IllegalArgumentException("Matching strategies must not contain null entries.");
            }
        }
        this.strategies = new ArrayList<>(strategies);
        this.profile = profile;
    }

    /**
     * Calculates an integer score from 0 to 100 for one adopter-animal pair.
     * Only applicable strategies count toward the final score.
     * If no strategy is applicable, the user has not expressed a scoring preference,
     * so every animal-adopter pair is treated as an equally acceptable match.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return the weighted score scaled to 100
     */
    public int calculateScore(Adopter adopter, Animal animal) {
        double weightedScoreTotal = 0.0;
        double weightTotal = 0.0;

        for (IMatchingStrategy strategy : strategies) {
            if (strategy.isApplicable(adopter, animal)) {
                double influence = getInfluenceForCriterion(strategy.getCriterion());
                weightedScoreTotal += strategy.score(adopter, animal) * influence;
                weightTotal += influence;
            }
        }

        if (weightTotal == 0.0) {
            return 100;
        }

        return (int) Math.round((weightedScoreTotal / weightTotal) * 100);
    }

    /**
     * Converts a rank into matching influence without exposing a separate public weight method.
     * Unranked applicable criteria use the default weight. Ranked criteria get higher
     * influence, and rank 1 receives the strongest influence.
     */
    private double getInfluenceForCriterion(MatchingCriterion criterion) {
        if (profile == null || profile.getPriorities().isEmpty()) {
            return DEFAULT_WEIGHT;
        }

        Integer rank = profile.getRank(criterion);
        if (rank == null) {
            return DEFAULT_WEIGHT;
        }

        return profile.getLargestRank() - rank + 2.0;
    }
}
