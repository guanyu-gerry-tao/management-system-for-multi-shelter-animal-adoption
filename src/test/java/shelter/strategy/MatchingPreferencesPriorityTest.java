package shelter.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MatchingPreferencesPriority}.
 */
class MatchingPreferencesPriorityTest {

    @Test
    void constructor_validArguments_storesFields() {
        MatchingPreferencesPriority priority =
                new MatchingPreferencesPriority(MatchingCriterion.SPECIES, 1);
        assertEquals(MatchingCriterion.SPECIES, priority.getCriterion());
        assertEquals(1, priority.getRank());
    }

    @Test
    void constructor_nullCriterion_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new MatchingPreferencesPriority(null, 1));
    }

    @Test
    void constructor_nonPositiveRank_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new MatchingPreferencesPriority(MatchingCriterion.SPECIES, 0));
    }
}
