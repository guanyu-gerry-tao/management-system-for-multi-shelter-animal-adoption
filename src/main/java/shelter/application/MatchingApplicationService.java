package shelter.application;

import shelter.service.model.MatchResult;

import java.util.List;

/**
 * Application service for animal-adopter matching use cases.
 * Orchestrates scoring across all active matching strategies and optionally
 * invokes the explanation service to produce natural-language summaries.
 */
public interface MatchingApplicationService {

    /**
     * Finds and ranks available animals in the given shelter for the specified adopter.
     * Only animals whose {@code adopterId} is null are considered.
     * If {@code withExplanation} is true, each result is supplemented with a natural-language explanation.
     * Returns an empty list if no available animals are found.
     *
     * @param adopterId       the ID of the adopter to match for; must not be null or blank
     * @param shelterId       the ID of the shelter to search in; must not be null or blank
     * @param withExplanation if true, explanation text is generated for each result
     * @return a ranked list of {@link MatchResult} instances, best match first
     */
    List<MatchResult> matchAnimalsForAdopter(String adopterId, String shelterId,
                                              boolean withExplanation);

    /**
     * Finds and ranks all registered adopters by compatibility with the given available animal.
     * Throws an exception if the animal is not found or has already been adopted.
     * If {@code withExplanation} is true, each result is supplemented with a natural-language explanation.
     * Returns an empty list if no adopters are registered.
     *
     * @param animalId        the ID of the animal to match for; must not be null or blank
     * @param withExplanation if true, explanation text is generated for each result
     * @return a ranked list of {@link MatchResult} instances, best match first
     */
    List<MatchResult> matchAdoptersForAnimal(String animalId, boolean withExplanation);
}
