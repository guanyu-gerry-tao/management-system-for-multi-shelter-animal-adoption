package shelter.service.impl;

import shelter.domain.Adopter;
import shelter.domain.Animal;
import shelter.service.AdopterBasedMatchingService;
import shelter.service.model.MatchResult;
import shelter.strategy.IMatchingStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of {@link AdopterBasedMatchingService} that aggregates scores
 * from a configurable list of matching strategies to rank animals for a given adopter.
 * Each strategy contributes a normalized score in [0.0, 1.0]; the total is scaled to an
 * integer out of 100 per strategy so results are comparable regardless of how many
 * strategies are active.
 */
public class AdopterBasedMatchingServiceImpl implements AdopterBasedMatchingService {

    private final List<IMatchingStrategy> strategies;

    /**
     * Constructs an AdopterBasedMatchingServiceImpl with the given list of strategies.
     * The strategies are applied in the order provided; the list must not be null or empty.
     *
     * @param strategies the matching strategies to apply; must not be null or empty
     * @throws IllegalArgumentException if {@code strategies} is null or empty
     */
    public AdopterBasedMatchingServiceImpl(List<IMatchingStrategy> strategies) {
        if (strategies == null || strategies.isEmpty()) {
            throw new IllegalArgumentException("At least one matching strategy must be provided.");
        }
        this.strategies = new ArrayList<>(strategies);
    }

    /**
     * {@inheritDoc}
     * Scores each animal by summing contributions from all strategies, then sorts results
     * descending by score. Animals with a score of zero are included in the result.
     */
    @Override
    public List<MatchResult> match(Adopter adopter, List<Animal> animals) {
        if (adopter == null) throw new IllegalArgumentException("Adopter must not be null.");
        if (animals == null) throw new IllegalArgumentException("Animals list must not be null.");

        List<MatchResult> results = new ArrayList<>();
        for (Animal animal : animals) {
            // Sum raw scores from all strategies, then scale to integer points
            double rawScore = 0.0;
            for (IMatchingStrategy strategy : strategies) {
                // Polymorphism: strategy.score() dispatches to the concrete implementation at runtime
                // (e.g. SpeciesPreferenceStrategy, ActivityLevelStrategy) without this class knowing which
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
