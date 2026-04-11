package shelter.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ActivityLevelStrategy}.
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
    void isApplicable_withActivityPreference_returnsTrue() {
        assertTrue(strategy.isApplicable(mediumActivityAdopter, dog(ActivityLevel.MEDIUM)));
    }

    @Test
    void isApplicable_withoutActivityPreference_returnsFalse() {
        Adopter noPreference = new Adopter("Bob", LivingSpace.APARTMENT,
                DailySchedule.AWAY_MOST_OF_DAY, null,
                new AdopterPreferences(null, null, null, null, 0, 10));
        assertFalse(strategy.isApplicable(noPreference, dog(ActivityLevel.MEDIUM)));
    }

    @Test
    void score_exactMatch_returnsOne() {
        assertEquals(1.0, strategy.score(mediumActivityAdopter, dog(ActivityLevel.MEDIUM)));
    }

    @Test
    void score_oneLevelAway_returnsHalf() {
        assertEquals(0.5, strategy.score(mediumActivityAdopter, dog(ActivityLevel.HIGH)));
    }

    @Test
    void score_twoLevelsAway_returnsZero() {
        Adopter lowActivityAdopter = new Adopter("Cara", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, null,
                new AdopterPreferences(null, null, ActivityLevel.LOW, null, 0, 10));
        assertEquals(0.0, strategy.score(lowActivityAdopter, dog(ActivityLevel.HIGH)));
    }

    private Dog dog(ActivityLevel activityLevel) {
        return new Dog("Rex", "Labrador", LocalDate.now().minusYears(3),
                activityLevel, false, Dog.Size.LARGE, false);
    }
}
