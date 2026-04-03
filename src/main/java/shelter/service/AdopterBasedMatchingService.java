package shelter.service;

import shelter.domain.Adopter;
import shelter.domain.Animal;
import shelter.service.model.MatchResult;

import java.util.List;

/**
 * Scores and ranks animals for a given adopter by applying a configurable set of matching strategies.
 * The ranked list can be passed to an ExplanationService for human-readable summaries.
 */
public interface AdopterBasedMatchingService {

    /**
     * Evaluates each animal in the provided list against the adopter's preferences and returns
     * a ranked list of match results ordered from highest to lowest score.
     * Animals with no compatibility with the adopter's preferences may still appear in the result
     * with a score of zero.
     *
     * @param adopter the adopter to match against
     * @param animals the pool of animals to evaluate
     * @return a list of MatchResult objects sorted by descending score
     */
    List<MatchResult> match(Adopter adopter, List<Animal> animals);
}
