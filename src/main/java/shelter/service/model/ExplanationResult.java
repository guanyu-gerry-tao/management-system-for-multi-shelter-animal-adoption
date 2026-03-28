package shelter.service.model;

// TODO: fields are pending team discussion — confirm what AI should generate before finalizing this class

/**
 * Represents the structured output of an ExplanationService call for a set of match results.
 * Encapsulates multiple aspects of the AI-generated explanation, allowing callers to use
 * each component independently.
 */
public class ExplanationResult {

    private final String matchRationale;
    private final String confidenceAssessment;
    private final String personalizedAdvice;

    /**
     * Constructs an ExplanationResult with all explanation components.
     * All fields are immutable once set; use the builder or constructor to supply values.
     *
     * @param matchRationale       explanation of why the top matches are good or poor fits
     * @param confidenceAssessment assessment of whether the match scores are reliable and meaningful
     * @param personalizedAdvice   tailored recommendations for the adopter based on their profile
     */
    public ExplanationResult(String matchRationale, String confidenceAssessment, String personalizedAdvice) {
        this.matchRationale = matchRationale;
        this.confidenceAssessment = confidenceAssessment;
        this.personalizedAdvice = personalizedAdvice;
    }

    /**
     * Returns the rationale explaining why the matched animals are a good or poor fit.
     *
     * @return the match rationale string
     */
    public String getMatchRationale() {
        return matchRationale;
    }

    /**
     * Returns an assessment of how reliable and meaningful the match scores are.
     *
     * @return the confidence assessment string
     */
    public String getConfidenceAssessment() {
        return confidenceAssessment;
    }

    /**
     * Returns personalized advice for the adopter based on their lifestyle and preferences.
     *
     * @return the personalized advice string
     */
    public String getPersonalizedAdvice() {
        return personalizedAdvice;
    }
}
