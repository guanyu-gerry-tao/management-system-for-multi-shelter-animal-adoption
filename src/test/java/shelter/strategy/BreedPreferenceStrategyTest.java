package shelter.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BreedPreferenceStrategy}.
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
    void isApplicable_withBreedPreference_returnsTrue() {
        assertTrue(strategy.isApplicable(adopter, labrador));
    }

    @Test
    void isApplicable_withBlankBreedPreference_returnsFalse() {
        Adopter noPreference = new Adopter("Bob", LivingSpace.APARTMENT,
                DailySchedule.AWAY_MOST_OF_DAY, null,
                new AdopterPreferences(null, " ", null, null, 0, 10));
        assertFalse(strategy.isApplicable(noPreference, labrador));
    }

    @Test
    void score_matchingBreedIgnoringCase_returnsOne() {
        assertEquals(1.0, strategy.score(adopter, labrador));
    }

    @Test
    void score_differentBreed_returnsZero() {
        assertEquals(0.0, strategy.score(adopter, poodle));
    }
}
