package shelter.service;

import shelter.domain.Adopter;
import shelter.domain.Animal;
import shelter.service.model.MatchResult;

import java.util.List;

/**
 * Scores and ranks adopters for a given animal by applying a configurable set of matching strategies.
 * Supports animal-centric adoption workflows as the counterpart to AdopterBasedMatchingService.
 */
public interface AnimalBasedMatchingService {

    /**
     * Evaluates each adopter in the provided list against the given animal and returns
     * a ranked list of match results ordered from highest to lowest score.
     *
     * @param animal   the animal to match against
     * @param adopters the pool of adopters to evaluate
     * @return a list of MatchResult objects sorted by descending score
     */
    List<MatchResult> match(Animal animal, List<Adopter> adopters);
}
