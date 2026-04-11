package shelter.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BreedPreferenceStrategy}.
 * Covers null guards, applicability behavior, exact matches, no matches, and criterion identity.
 */
class BreedPreferenceStrategyTest {

    private BreedPreferenceStrategy strategy;
    private Adopter adopter;
    private Dog labrador;
    private Dog poodle;

    @BeforeEach
    void setUp() {
        strategy = new BreedPreferenceStrategy();
        adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD, DailySchedule.HOME_MOST_OF_DAY,
                null, new AdopterPreferences(null, "labrador", null, null, 0, 10));
        labrador = new Dog("Rex", "Labrador", LocalDate.now().minusYears(3),
                ActivityLevel.MEDIUM, false, Dog.Size.LARGE, false);
        poodle = new Dog("Buddy", "Poodle", LocalDate.now().minusYears(2),
                ActivityLevel.LOW, false, Dog.Size.SMALL, false);
    }

    @Test
    void score_nullAdopter_throws() {
        assertThrows(IllegalArgumentException.class, () -> strategy.score(null, labrador));
    }

    @Test
    void score_nullAnimal_throws() {
        assertThrows(IllegalArgumentException.class, () -> strategy.score(adopter, null));
    }

    @Test
    void isApplicable_noPreferenceSet_returnsFalse() {
        Adopter noPreference = new Adopter("Bob", LivingSpace.APARTMENT,
                DailySchedule.AWAY_MOST_OF_DAY, null,
                new AdopterPreferences(null, " ", null, null, 0, 10));
        assertFalse(strategy.isApplicable(noPreference, labrador));
    }

    @Test
    void score_exactMatch_returnsFullScore() {
        assertEquals(1.0, strategy.score(adopter, labrador));
    }

    // BreedPreferenceStrategy is binary, so there is no partial-match score to test.

    @Test
    void score_noMatch_returnsZero() {
        assertEquals(0.0, strategy.score(adopter, poodle));
    }

    @Test
    void getCriterion_returnsCorrectEnum() {
        assertEquals(MatchingCriterion.BREED, strategy.getCriterion());
    }
}
