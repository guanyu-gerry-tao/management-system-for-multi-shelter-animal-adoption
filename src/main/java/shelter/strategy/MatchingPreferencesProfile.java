package shelter.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents the full prioritized matching setup selected by a user.
 * The profile stores the criteria the user chose to rank. Ranked criteria can
 * receive stronger influence in the matching workflow, while unranked criteria
 * may still be considered when their corresponding strategy is applicable.
 */
public class MatchingPreferencesProfile {

    private final List<MatchingPreferencesPriority> priorities;

    /**
     * Constructs a matching preferences profile with the given ranked criteria.
     *
     * @param priorities the prioritized criteria selected by the user
     * @throws IllegalArgumentException if {@code priorities} is {@code null}
     */
    public MatchingPreferencesProfile(List<MatchingPreferencesPriority> priorities) {
        if (priorities == null) {
            throw new IllegalArgumentException("Matching priorities must not be null.");
        }
        Set<MatchingCriterion> seenCriteria = new HashSet<>();
        Set<Integer> seenRanks = new HashSet<>();
        for (MatchingPreferencesPriority priority : priorities) {
            if (priority == null) {
                throw new IllegalArgumentException("Matching priority entries must not be null.");
            }
            if (!seenCriteria.add(priority.getCriterion())) {
                throw new IllegalArgumentException("Each matching criterion can only be ranked once.");
            }
            if (!seenRanks.add(priority.getRank())) {
                throw new IllegalArgumentException("Each priority rank can only be used once.");
            }
        }
        this.priorities = Collections.unmodifiableList(new ArrayList<>(priorities));
    }

    /**
     * Returns all prioritized criteria in this profile.
     * The returned list cannot be modified by callers.
     *
     * @return an unmodifiable list of matching priorities
     */
    public List<MatchingPreferencesPriority> getPriorities() {
        return priorities;
    }

    /**
     * Returns the rank for a criterion, or {@code null} if the user did not rank that criterion.
     *
     * @param criterion the criterion to look up
     * @return the rank for the criterion, or {@code null} when unranked
     * @throws IllegalArgumentException if {@code criterion} is {@code null}
     */
    public Integer getRank(MatchingCriterion criterion) {
        if (criterion == null) {
            throw new IllegalArgumentException("Matching criterion must not be null.");
        }
        for (MatchingPreferencesPriority priority : priorities) {
            if (priority.getCriterion() == criterion) {
                return priority.getRank();
            }
        }
        return null;
    }

    /**
     * Returns the largest rank number in the profile.
     * This is used to translate rank order into weighted matching influence.
     *
     * @return the largest rank, or {@code 0} when the profile is empty
     */
    public int getLargestRank() {
        int largestRank = 0;
        for (MatchingPreferencesPriority priority : priorities) {
            largestRank = Math.max(largestRank, priority.getRank());
        }
        return largestRank;
    }
}
