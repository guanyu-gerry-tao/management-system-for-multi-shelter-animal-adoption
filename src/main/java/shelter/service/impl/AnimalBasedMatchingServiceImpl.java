package shelter.service.impl;

import shelter.domain.Adopter;
import shelter.domain.Animal;
import shelter.service.AnimalBasedMatchingService;
import shelter.service.model.MatchResult;
import shelter.strategy.IMatchingStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of {@link AnimalBasedMatchingService} that aggregates scores
 * from a configurable list of matching strategies to rank adopters for a given animal.
 * Each strategy contributes a normalized score in [0.0, 1.0]; the total is scaled to an
 * integer out of 100 per strategy so results are comparable regardless of how many
 * strategies are active.
 */
public class AnimalBasedMatchingServiceImpl implements AnimalBasedMatchingService {

    private final List<IMatchingStrategy> strategies;

    /**
     * Constructs an AnimalBasedMatchingServiceImpl with the given list of strategies.
     * The strategies are applied in the order provided; the list must not be null or empty.
     *
     * @param strategies the matching strategies to apply; must not be null or empty
     * @throws IllegalArgumentException if {@code strategies} is null or empty
     */
    public AnimalBasedMatchingServiceImpl(List<IMatchingStrategy> strategies) {
        if (strategies == null || strategies.isEmpty()) {
            throw new IllegalArgumentException("At least one matching strategy must be provided.");
        }
        this.strategies = new ArrayList<>(strategies);
    }

    /**
     * {@inheritDoc}
     * Scores each adopter by summing contributions from all strategies, then sorts results
     * descending by score. Adopters with a score of zero are included in the result.
     */
    @Override
    public List<MatchResult> match(Animal animal, List<Adopter> adopters) {
        if (animal == null) throw new IllegalArgumentException("Animal must not be null.");
        if (adopters == null) throw new IllegalArgumentException("Adopters list must not be null.");

        List<MatchResult> results = new ArrayList<>();
        for (Adopter adopter : adopters) {
            // Sum raw scores from all strategies, then scale to integer points
            double rawScore = 0.0;
            for (IMatchingStrategy strategy : strategies) {
                // Polymorphism: strategy.score() dispatches to the concrete implementation at runtime
                // (e.g. BreedPreferenceStrategy, LifestyleCompatibilityStrategy) without this class knowing which
                rawScore += strategy.score(adopter, animal);
            }
            int score = (int) Math.round(rawScore * 100);
            results.add(new MatchResult(animal, adopter, score));
        }

        // Sort best match first
        Collections.sort(results);
        return results;
    }
}
