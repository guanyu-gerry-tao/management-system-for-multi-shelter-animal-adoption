package shelter.service;

import shelter.service.model.ExplanationResult;
import shelter.service.model.MatchResult;

import java.util.List;

/**
 * Generates a structured explanation for a set of match results between animals and adopters.
 * This interface decouples the explanation logic from the matching logic, allowing AI or mock implementations.
 */
public interface ExplanationService {

    /**
     * Produces a structured explanation of the top match results, including rationale,
     * confidence assessment, and personalized advice derived from the MatchResult data.
     *
     * @param results the ranked list of match results to explain
     * @return an ExplanationResult containing structured components of the explanation
     */
    ExplanationResult explain(List<MatchResult> results);
}
