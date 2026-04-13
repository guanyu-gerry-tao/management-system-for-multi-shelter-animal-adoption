package shelter.service.impl;

import shelter.service.ExplanationService;
import shelter.service.model.ExplanationResult;
import shelter.service.model.MatchResult;

import java.util.List;

/**
 * A deterministic, non-AI implementation of {@link ExplanationService} used as the default
 * placeholder until {@code AIExplanationService} is integrated.
 * Returns a fixed "not connected" message for every field instead of an AI-generated explanation,
 * making it clear to demo viewers that no real AI output is being produced.
 */
public class MockExplanationService implements ExplanationService {

    /** Placeholder message shown in every explanation field when no AI service is connected. */
    private static final String NOT_CONNECTED_MESSAGE =
            "AI explanation service is not connected — no explanation available.";

    /**
     * Returns an ExplanationResult whose every field is the fixed "not connected" placeholder.
     * This method ignores the input {@code results} entirely and never calls any external service,
     * keeping tests deterministic and making the absence of a real AI explanation obvious in demo output.
     *
     * @param results the ranked list of match results to explain; may be empty but must not be null
     * @return an ExplanationResult with the "not connected" placeholder in every field
     */
    @Override
    public ExplanationResult explain(List<MatchResult> results) {
        return new ExplanationResult(NOT_CONNECTED_MESSAGE, NOT_CONNECTED_MESSAGE, NOT_CONNECTED_MESSAGE);
    }
}
