package shelter.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ActivityLevelStrategy}.
 * Covers null guards, applicability behavior, exact matches, partial matches, no matches, and criterion identity.
 */
class ActivityLevelStrategyTest {

    private ActivityLevelStrategy strategy;
    private Adopter mediumActivityAdopter;

    @BeforeEach
    void setUp() {
        strategy = new ActivityLevelStrategy();
        mediumActivityAdopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, null,
                new AdopterPreferences(null, null, ActivityLevel.MEDIUM, null, 0, 10));
    }

    @Test
    void score_nullAdopter_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> strategy.score(null, dog(ActivityLevel.MEDIUM)));
    }

    @Test
    void score_nullAnimal_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> strategy.score(mediumActivityAdopter, null));
    }

    @Test
    void isApplicable_noPreferenceSet_returnsFalse() {
        Adopter noPreference = new Adopter("Bob", LivingSpace.APARTMENT,
                DailySchedule.AWAY_MOST_OF_DAY, null,
                new AdopterPreferences(null, null, null, null, 0, 10));
        assertFalse(strategy.isApplicable(noPreference, dog(ActivityLevel.MEDIUM)));
    }

    @Test
    void score_exactMatch_returnsFullScore() {
        assertEquals(1.0, strategy.score(mediumActivityAdopter, dog(ActivityLevel.MEDIUM)));
    }

    @Test
    void score_partialMatch_returnsPartialScore() {
        assertEquals(0.5, strategy.score(mediumActivityAdopter, dog(ActivityLevel.HIGH)));
    }

    @Test
    void score_noMatch_returnsZero() {
        Adopter lowActivityAdopter = new Adopter("Cara", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, null,
                new AdopterPreferences(null, null, ActivityLevel.LOW, null, 0, 10));
        assertEquals(0.0, strategy.score(lowActivityAdopter, dog(ActivityLevel.HIGH)));
    }

    @Test
    void getCriterion_returnsCorrectEnum() {
        assertEquals(MatchingCriterion.ACTIVITY_LEVEL, strategy.getCriterion());
    }

    private Dog dog(ActivityLevel activityLevel) {
        return new Dog("Rex", "Labrador", LocalDate.now().minusYears(3),
                activityLevel, false, Dog.Size.LARGE, false);
    }
}
