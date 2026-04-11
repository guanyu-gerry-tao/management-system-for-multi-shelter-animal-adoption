package shelter.strategy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MatchingPreferencesProfile}.
 */
class MatchingPreferencesProfileTest {

    @Test
    void constructor_validPriorities_storesUnmodifiableList() {
        MatchingPreferencesProfile profile = new MatchingPreferencesProfile(List.of(
                new MatchingPreferencesPriority(MatchingCriterion.SPECIES, 1),
                new MatchingPreferencesPriority(MatchingCriterion.BREED, 2)
        ));

        assertEquals(2, profile.getPriorities().size());
        assertThrows(UnsupportedOperationException.class,
                () -> profile.getPriorities().add(
                        new MatchingPreferencesPriority(MatchingCriterion.AGE, 3)));
    }

    @Test
    void getRank_existingCriterion_returnsRank() {
        MatchingPreferencesProfile profile = new MatchingPreferencesProfile(List.of(
                new MatchingPreferencesPriority(MatchingCriterion.SPECIES, 1)
        ));

        assertEquals(1, profile.getRank(MatchingCriterion.SPECIES));
    }

    @Test
    void getRank_unrankedCriterion_returnsNull() {
        MatchingPreferencesProfile profile = new MatchingPreferencesProfile(List.of(
                new MatchingPreferencesPriority(MatchingCriterion.SPECIES, 1)
        ));

        assertNull(profile.getRank(MatchingCriterion.BREED));
    }

    @Test
    void getLargestRank_returnsLargestRank() {
        MatchingPreferencesProfile profile = new MatchingPreferencesProfile(List.of(
                new MatchingPreferencesPriority(MatchingCriterion.SPECIES, 1),
                new MatchingPreferencesPriority(MatchingCriterion.BREED, 3)
        ));

        assertEquals(3, profile.getLargestRank());
    }

    @Test
    void constructor_duplicateCriterion_throws() {
        assertThrows(IllegalArgumentException.class, () -> new MatchingPreferencesProfile(List.of(
                new MatchingPreferencesPriority(MatchingCriterion.SPECIES, 1),
                new MatchingPreferencesPriority(MatchingCriterion.SPECIES, 2)
        )));
    }

    @Test
    void constructor_duplicateRank_throws() {
        assertThrows(IllegalArgumentException.class, () -> new MatchingPreferencesProfile(List.of(
                new MatchingPreferencesPriority(MatchingCriterion.SPECIES, 1),
                new MatchingPreferencesPriority(MatchingCriterion.BREED, 1)
        )));
    }
}
