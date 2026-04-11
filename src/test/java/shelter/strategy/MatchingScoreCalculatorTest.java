package shelter.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MatchingScoreCalculator}.
 */
class MatchingScoreCalculatorTest {

    private Adopter adopter;
    private Dog dog;

    @BeforeEach
    void setUp() {
        adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD, DailySchedule.HOME_MOST_OF_DAY,
                null, new AdopterPreferences(Species.DOG, "Labrador", null, null, 0, 10));
        dog = new Dog("Rex", "Labrador", LocalDate.now().minusYears(3),
                ActivityLevel.MEDIUM, false, Dog.Size.LARGE, false);
    }

    @Test
    void calculateScore_noApplicableStrategies_returnsOneHundred() {
        Adopter noPreferenceAdopter = new Adopter("Bob", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, null,
                new AdopterPreferences(null, null, null, null, 0, Integer.MAX_VALUE));
        MatchingScoreCalculator calculator = new MatchingScoreCalculator(
                List.of(new SpeciesPreferenceStrategy()), null);

        assertEquals(100, calculator.calculateScore(noPreferenceAdopter, dog));
    }

    @Test
    void calculateScore_withoutProfile_averagesApplicableStrategies() {
        Adopter speciesMatchesBreedDoesNotMatch = new Adopter("Bob", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, null,
                new AdopterPreferences(Species.DOG, "Poodle", null, null, 0, 10));
        MatchingScoreCalculator calculator = new MatchingScoreCalculator(
                List.of(
                        new SpeciesPreferenceStrategy(),
                        new BreedPreferenceStrategy()
                ),
                null);

        assertEquals(50, calculator.calculateScore(speciesMatchesBreedDoesNotMatch, dog));
    }

    @Test
    void calculateScore_withRankedProfile_appliesStrongerInfluenceToRankedCriterion() {
        MatchingPreferencesProfile profile = new MatchingPreferencesProfile(List.of(
                new MatchingPreferencesPriority(MatchingCriterion.BREED, 1)
        ));
        Adopter speciesMatchesBreedDoesNotMatch = new Adopter("Bob", LivingSpace.HOUSE_WITH_YARD,
                DailySchedule.HOME_MOST_OF_DAY, null,
                new AdopterPreferences(Species.DOG, "Poodle", null, null, 0, 10));
        MatchingScoreCalculator calculator = new MatchingScoreCalculator(
                List.of(
                        new SpeciesPreferenceStrategy(),
                        new BreedPreferenceStrategy()
                ),
                profile);

        assertEquals(33, calculator.calculateScore(speciesMatchesBreedDoesNotMatch, dog));
    }

    @Test
    void constructor_nullStrategies_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new MatchingScoreCalculator(null, null));
    }

    @Test
    void constructor_emptyStrategies_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new MatchingScoreCalculator(List.of(), null));
    }

}
