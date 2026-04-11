package shelter.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AgePreferenceStrategy}.
 */
class AgePreferenceStrategyTest {

    private AgePreferenceStrategy strategy;
    private Adopter prefersTwoYearOld;

    @BeforeEach
    void setUp() {
        strategy = new AgePreferenceStrategy();
        prefersTwoYearOld = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, null,
                new AdopterPreferences(null, null, null, null, 2, 2));
    }

    @Test
    void isApplicable_withAgePreference_returnsTrue() {
        assertTrue(strategy.isApplicable(prefersTwoYearOld, dogAtBirthday(LocalDate.now().minusYears(2))));
    }

    @Test
    void isApplicable_withSentinelRange_returnsFalse() {
        Adopter noAgePreference = new Adopter("Bob", LivingSpace.APARTMENT,
                DailySchedule.AWAY_MOST_OF_DAY, null,
                new AdopterPreferences(null, null, null, null, 0, Integer.MAX_VALUE));
        assertFalse(strategy.isApplicable(noAgePreference, dogAtBirthday(LocalDate.now().minusYears(2))));
    }

    @Test
    void score_insidePreferredRange_returnsOne() {
        assertEquals(1.0, strategy.score(prefersTwoYearOld,
                dogAtBirthday(LocalDate.now().minusYears(2))));
    }

    @Test
    void score_upToHalfYearOutsideRange_returnsPointEight() {
        assertEquals(0.8, strategy.score(prefersTwoYearOld,
                dogAtBirthday(LocalDate.now().minusYears(2).minusMonths(3))));
    }

    @Test
    void score_halfToOneYearOutsideRange_returnsPointFive() {
        assertEquals(0.5, strategy.score(prefersTwoYearOld,
                dogAtBirthday(LocalDate.now().minusYears(2).minusMonths(9))));
    }

    @Test
    void score_moreThanOneYearOutsideRange_returnsZero() {
        assertEquals(0.0, strategy.score(prefersTwoYearOld,
                dogAtBirthday(LocalDate.now().minusYears(3).minusMonths(6))));
    }

    private Dog dogAtBirthday(LocalDate birthday) {
        return new Dog("Rex", "Labrador", birthday,
                ActivityLevel.MEDIUM, false, Dog.Size.MEDIUM, false);
    }
}
