package shelter.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LifestyleCompatibilityStrategy}.
 */
class LifestyleCompatibilityStrategyTest {

    private LifestyleCompatibilityStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new LifestyleCompatibilityStrategy();
    }

    @Test
    void isApplicable_withValidInputs_returnsTrue() {
        assertTrue(strategy.isApplicable(adopter(LivingSpace.APARTMENT, DailySchedule.AWAY_MOST_OF_DAY),
                dog(ActivityLevel.HIGH, Dog.Size.LARGE)));
    }

    @Test
    void score_largeHighEnergyDogInApartmentWithAwaySchedule_returnsZero() {
        assertEquals(0.0, strategy.score(
                adopter(LivingSpace.APARTMENT, DailySchedule.AWAY_MOST_OF_DAY),
                dog(ActivityLevel.HIGH, Dog.Size.LARGE)));
    }

    @Test
    void score_largeDogWithYardAndHomeSchedule_returnsOne() {
        assertEquals(1.0, strategy.score(
                adopter(LivingSpace.HOUSE_WITH_YARD, DailySchedule.HOME_MOST_OF_DAY),
                dog(ActivityLevel.HIGH, Dog.Size.LARGE)));
    }

    @Test
    void score_nullInput_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> strategy.score(null, dog(ActivityLevel.LOW, Dog.Size.SMALL)));
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
