package shelter.strategy;

/**
 * Represents one prioritized matching criterion selected by a user.
 * A lower rank means higher importance in the later matching process.
 */
public class MatchingPreferencesPriority {

    private final MatchingCriterion criterion;
    private final int rank;

    /**
     * Constructs one priority entry for the given criterion and rank.
     *
     * @param criterion the matching criterion being prioritized
     * @param rank the rank of importance, where {@code 1} is the highest priority
     * @throws IllegalArgumentException if {@code criterion} is {@code null} or {@code rank <= 0}
     */
    public MatchingPreferencesPriority(MatchingCriterion criterion, int rank) {
        if (criterion == null) {
            throw new IllegalArgumentException("Matching criterion must not be null.");
        }
        if (rank <= 0) {
            throw new IllegalArgumentException("Priority rank must be positive.");
        }
        this.criterion = criterion;
        this.rank = rank;
    }

    /**
     * Returns the prioritized matching criterion.
     *
     * @return the criterion
     */
    public MatchingCriterion getCriterion() {
        return criterion;
    }

    /**
     * Returns the priority rank, where {@code 1} means the most important criterion.
     *
     * @return the priority rank
     */
    public int getRank() {
        return rank;
    }
}
