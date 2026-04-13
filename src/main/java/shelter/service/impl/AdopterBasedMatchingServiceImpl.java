package shelter.service.impl;

import shelter.domain.Adopter;
import shelter.domain.Animal;
import shelter.service.AdopterBasedMatchingService;
import shelter.service.model.MatchResult;
import shelter.strategy.IMatchingStrategy;
import shelter.strategy.MatchingPreferencesProfile;
import shelter.strategy.MatchingScoreCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of {@link AdopterBasedMatchingService} that aggregates scores
 * from a configurable list of matching strategies to rank animals for a given adopter.
 * Each applicable strategy contributes a normalized score in [0.0, 1.0]. A priority
 * profile can give ranked criteria stronger influence, while unranked applicable
 * criteria still count with normal influence. Criteria that are not applicable do not count.
 */
public class AdopterBasedMatchingServiceImpl implements AdopterBasedMatchingService {

    private final MatchingScoreCalculator scoreCalculator;

    /**
     * Constructs an AdopterBasedMatchingServiceImpl with the given list of strategies.
     * The strategies are applied in the order provided; the list must not be null or empty.
     *
     * @param strategies the matching strategies to apply; must not be null or empty
     * @throws IllegalArgumentException if {@code strategies} is null or empty
     */
    public AdopterBasedMatchingServiceImpl(List<IMatchingStrategy> strategies) {
        this(strategies, null);
    }

    /**
     * Constructs an AdopterBasedMatchingServiceImpl with strategies and a priority profile.
     * The profile may be {@code null}; in that case every applicable strategy uses normal weight.
     *
     * @param strategies the matching strategies to apply; must not be null or empty
     * @param profile the optional ranked preference profile
     * @throws IllegalArgumentException if {@code strategies} is null or empty
     */
    public AdopterBasedMatchingServiceImpl(List<IMatchingStrategy> strategies,
                                           MatchingPreferencesProfile profile) {
        if (strategies == null || strategies.isEmpty()) {
            throw new IllegalArgumentException("At least one matching strategy must be provided.");
        }
        this.scoreCalculator = new MatchingScoreCalculator(strategies, profile);
    }

    /**
     * {@inheritDoc}
     * Scores each animal using applicable strategy contributions, then sorts results descending
     * by score. Animals with a score of zero are included in the result.
     */
    @Override
    public List<MatchResult> match(Adopter adopter, List<Animal> animals) {
        if (adopter == null) throw new IllegalArgumentException("Adopter must not be null.");
        if (animals == null) throw new IllegalArgumentException("Animals list must not be null.");

        List<MatchResult> results = new ArrayList<>();
        for (Animal animal : animals) {
            int score = scoreCalculator.calculateScore(adopter, animal);
            results.add(new MatchResult(animal, adopter, score));
        }

        // Sort best match first
        Collections.sort(results);
        return results;
    }
}
