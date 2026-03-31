package shelter.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the full prioritized matching setup selected by a user.
 * The profile stores only the criteria the user chose to rank, allowing
 * unselected criteria to be ignored by the matching workflow.
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
}
