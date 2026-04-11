package shelter.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SpeciesPreferenceStrategy}.
 */
class SpeciesPreferenceStrategyTest {

    private SpeciesPreferenceStrategy strategy;
    private Adopter adopter;
    private Dog dog;
    private Cat cat;

    @BeforeEach
    void setUp() {
        strategy = new SpeciesPreferenceStrategy();
        adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD, DailySchedule.HOME_MOST_OF_DAY,
                null, new AdopterPreferences(Species.DOG, null, null, null, 0, 10));
        dog = new Dog("Rex", "Labrador", LocalDate.now().minusYears(3),
                ActivityLevel.MEDIUM, false, Dog.Size.LARGE, false);
        cat = new Cat("Milo", "Tabby", LocalDate.now().minusYears(2),
                ActivityLevel.LOW, false, true, false);
    }

    @Test
    void isApplicable_withSpeciesPreference_returnsTrue() {
        assertTrue(strategy.isApplicable(adopter, dog));
    }

    @Test
    void isApplicable_withoutSpeciesPreference_returnsFalse() {
        Adopter noPreference = new Adopter("Bob", LivingSpace.APARTMENT,
                DailySchedule.AWAY_MOST_OF_DAY, null,
                new AdopterPreferences(null, null, null, null, 0, 10));
        assertFalse(strategy.isApplicable(noPreference, dog));
    }

    @Test
    void score_matchingSpecies_returnsOne() {
        assertEquals(1.0, strategy.score(adopter, dog));
    }

    @Test
    void score_differentSpecies_returnsZero() {
        assertEquals(0.0, strategy.score(adopter, cat));
    }

    @Test
    void score_nullInput_throws() {
        assertThrows(IllegalArgumentException.class, () -> strategy.score(null, dog));
        assertThrows(IllegalArgumentException.class, () -> strategy.score(adopter, null));
    }
}
