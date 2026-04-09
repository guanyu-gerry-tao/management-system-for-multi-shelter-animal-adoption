package shelter.service.model;

import java.util.Objects;

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
     * All fields are immutable once set; use the constructor to supply values.
     * Any field may be null if the explanation source did not produce that component.
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
     * May be null if the explanation source did not produce this component.
     *
     * @return the match rationale string, or null
     */
    public String getMatchRationale() {
        return matchRationale;
    }

    /**
     * Returns an assessment of how reliable and meaningful the match scores are.
     * May be null if the explanation source did not produce this component.
     *
     * @return the confidence assessment string, or null
     */
    public String getConfidenceAssessment() {
        return confidenceAssessment;
    }

    /**
     * Returns personalized advice for the adopter based on their lifestyle and preferences.
     * May be null if the explanation source did not produce this component.
     *
     * @return the personalized advice string, or null
     */
    public String getPersonalizedAdvice() {
        return personalizedAdvice;
    }

    /**
     * Copy constructor that creates a new ExplanationResult with all field values copied from {@code other}.
     * The copy preserves the same rationale, confidence assessment, and personalized advice.
     *
     * @param other the ExplanationResult instance to copy; must not be null
     */
    public ExplanationResult(ExplanationResult other) {
        this(other.matchRationale, other.confidenceAssessment, other.personalizedAdvice);
    }

    /**
     * Returns true if the given object is an ExplanationResult with equal rationale, confidence, and advice.
     * Equality is value-based since explanation results have no unique ID.
     *
     * @param o the object to compare
     * @return true if all fields are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExplanationResult)) return false;
        ExplanationResult other = (ExplanationResult) o;
        return Objects.equals(matchRationale, other.matchRationale)
                && Objects.equals(confidenceAssessment, other.confidenceAssessment)
                && Objects.equals(personalizedAdvice, other.personalizedAdvice);
    }

    /**
     * Returns a hash code based on all fields.
     * Consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(matchRationale, confidenceAssessment, personalizedAdvice);
    }

    /**
     * Returns a string representation of this explanation result including all three components.
     *
     * @return a human-readable description of this explanation result
     */
    @Override
    public String toString() {
        return "ExplanationResult[rationale=" + matchRationale
                + ", confidence=" + confidenceAssessment
                + ", advice=" + personalizedAdvice + "]";
    }

}
