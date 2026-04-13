package shelter.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AgePreferenceStrategy}.
 * Covers null guards, applicability behavior, exact matches, partial matches, no matches, and criterion identity.
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
    void score_nullAdopter_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> strategy.score(null, dogAtBirthday(LocalDate.now().minusYears(2))));
    }

    @Test
    void score_nullAnimal_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> strategy.score(prefersTwoYearOld, null));
    }

    @Test
    void isApplicable_noPreferenceSet_returnsFalse() {
        Adopter noAgePreference = new Adopter("Bob", LivingSpace.APARTMENT,
                DailySchedule.AWAY_MOST_OF_DAY, null,
                new AdopterPreferences(null, null, null, null, null, null));
        assertFalse(strategy.isApplicable(noAgePreference, dogAtBirthday(LocalDate.now().minusYears(2))));
    }

    @Test
    void score_exactMatch_returnsFullScore() {
        assertEquals(1.0, strategy.score(prefersTwoYearOld,
                dogAtBirthday(LocalDate.now().minusYears(2))));
    }

    @Test
    void score_partialMatch_returnsPartialScore() {
        assertEquals(0.8, strategy.score(prefersTwoYearOld,
                dogAtBirthday(LocalDate.now().minusYears(2).minusMonths(3))));
    }

    @Test
    void score_halfToOneYearOutsideRange_returnsPointFive() {
        assertEquals(0.5, strategy.score(prefersTwoYearOld,
                dogAtBirthday(LocalDate.now().minusYears(2).minusMonths(9))));
    }

    @Test
    void score_noMatch_returnsZero() {
        assertEquals(0.0, strategy.score(prefersTwoYearOld,
                dogAtBirthday(LocalDate.now().minusYears(3).minusMonths(6))));
    }

    @Test
    void getCriterion_returnsCorrectEnum() {
        assertEquals(MatchingCriterion.AGE, strategy.getCriterion());
    }

    private Dog dogAtBirthday(LocalDate birthday) {
        return new Dog("Rex", "Labrador", birthday,
                ActivityLevel.MEDIUM, false, Dog.Size.MEDIUM, false);
    }
}
