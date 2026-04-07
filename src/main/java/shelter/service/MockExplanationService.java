package shelter.service;

import shelter.service.model.ExplanationResult;
import shelter.service.model.MatchResult;

import java.util.List;

/**
 * A deterministic stub implementation of {@link ExplanationService} for use in unit tests.
 * This mock always returns a fixed, hardcoded {@link ExplanationResult} regardless of input,
 * ensuring predictable output without calling any external AI API.
 */
public class MockExplanationService implements ExplanationService {

    /**
     * Returns a fixed {@link ExplanationResult} with placeholder text for any input.
     * If the results list is empty, the explanation indicates that no matches were available;
     * otherwise it references the top match by name. No external API calls are made.
     *
     * @param results the ranked list of match results to explain; may be empty but must not be null
     * @return a non-null ExplanationResult with deterministic mock content
     */
    @Override
    public ExplanationResult explain(List<MatchResult> results) {
        if (results == null || results.isEmpty()) {
            return new ExplanationResult(
                    "[Mock] No matches available to explain.",
                    "[Mock] Confidence: N/A — no match data provided.",
                    "[Mock] No personalized advice available without match results."
            );
        }

        MatchResult top = results.get(0);
        String animalName = top.getAnimal().getName();
        String adopterName = top.getAdopter().getName();
        int score = top.getScore();

        return new ExplanationResult(
                "[Mock] Top match: " + animalName + " for " + adopterName
                        + " with score " + score + ".",
                "[Mock] Confidence: HIGH — this is a mock assessment.",
                "[Mock] Consider visiting the shelter to meet " + animalName + " in person."
        );
    }
}
