package shelter.service.impl;

import shelter.service.ExplanationService;
import shelter.service.model.ExplanationResult;
import shelter.service.model.MatchResult;

import java.util.List;

/**
 * A deterministic, non-AI implementation of {@link ExplanationService} for use in unit tests.
 * Returns fixed, predictable explanation components that do not depend on any external AI service,
 * ensuring tests remain fast, isolated, and reproducible regardless of network or API availability.
 */
public class MockExplanationService implements ExplanationService {

    /**
     * Returns a fixed ExplanationResult containing placeholder values for each explanation component.
     * The result is derived purely from the size of the input list so tests can assert on stable output
     * without any AI call or random behavior.
     *
     * @param results the ranked list of match results to explain; may be empty but must not be null
     * @return a deterministic ExplanationResult with fixed placeholder strings
     */
    @Override
    public ExplanationResult explain(List<MatchResult> results) {
        // Build a stable rationale that mentions the number of results for minimal test assertability
        String rationale = "Mock rationale: " + results.size() + " match(es) evaluated.";
        String confidence = "Mock confidence: scores are deterministic in test mode.";
        String advice = "Mock advice: review all candidates before deciding.";
        return new ExplanationResult(rationale, confidence, advice);
    }
}
