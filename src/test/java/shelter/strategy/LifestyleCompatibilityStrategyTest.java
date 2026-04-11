package shelter.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LifestyleCompatibilityStrategy}.
 * Covers null guards, exact matches, partial matches, no matches, and criterion identity.
 */
class LifestyleCompatibilityStrategyTest {

    private LifestyleCompatibilityStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new LifestyleCompatibilityStrategy();
    }

    @Test
    void score_nullAdopter_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> strategy.score(null, dog(ActivityLevel.LOW, Dog.Size.SMALL)));
    }

    @Test
    void score_nullAnimal_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> strategy.score(adopter(LivingSpace.APARTMENT, DailySchedule.AWAY_MOST_OF_DAY),
                        null));
    }

    // LifestyleCompatibilityStrategy has no nullable preference field; it uses living space and schedule.
    // Therefore, the issue's score_noPreferenceSet_returnsZero case is intentionally skipped.

    @Test
    void score_exactMatch_returnsFullScore() {
        assertEquals(1.0, strategy.score(
                adopter(LivingSpace.HOUSE_WITH_YARD, DailySchedule.HOME_MOST_OF_DAY),
                dog(ActivityLevel.HIGH, Dog.Size.LARGE)));
    }

    @Test
    void score_partialMatch_returnsPartialScore() {
        assertEquals(0.5, strategy.score(
                adopter(LivingSpace.HOUSE_NO_YARD, DailySchedule.AWAY_PART_OF_DAY),
                dog(ActivityLevel.HIGH, Dog.Size.LARGE)));
    }

    @Test
    void score_noMatch_returnsZero() {
        assertEquals(0.0, strategy.score(
                adopter(LivingSpace.APARTMENT, DailySchedule.AWAY_MOST_OF_DAY),
                dog(ActivityLevel.HIGH, Dog.Size.LARGE)));
    }

    @Test
    void getCriterion_returnsCorrectEnum() {
        assertEquals(MatchingCriterion.LIFESTYLE, strategy.getCriterion());
    }

    private Adopter adopter(LivingSpace livingSpace, DailySchedule dailySchedule) {
        return new Adopter("Alice", livingSpace, dailySchedule, null,
                new AdopterPreferences(null, null, null, null, 0, 10));
    }

    private Dog dog(ActivityLevel activityLevel, Dog.Size size) {
        return new Dog("Rex", "Labrador", LocalDate.now().minusYears(3),
                activityLevel, false, size, false);
    }
}
