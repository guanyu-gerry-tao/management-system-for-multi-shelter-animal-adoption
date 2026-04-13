package shelter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.*;
import shelter.service.impl.AdopterBasedMatchingServiceImpl;
import shelter.service.impl.AnimalBasedMatchingServiceImpl;
import shelter.service.model.MatchResult;
import shelter.strategy.IMatchingStrategy;
import shelter.strategy.MatchingCriterion;
import shelter.strategy.MatchingPreferencesPriority;
import shelter.strategy.MatchingPreferencesProfile;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AdopterBasedMatchingServiceImpl} and {@link AnimalBasedMatchingServiceImpl}.
 * Uses inline stub strategies to control scores precisely without depending on real strategy logic.
 */
class MatchingServiceImplTest {

    // --- Stub strategies ---

    /** Always returns 1.0 — represents a perfect match on any criterion. */
    private static class AlwaysOneStrategy implements IMatchingStrategy {
        @Override public MatchingCriterion getCriterion() { return MatchingCriterion.SPECIES; }
        @Override public boolean isApplicable(Adopter adopter, Animal animal) { return true; }
        @Override public double score(Adopter adopter, Animal animal) { return 1.0; }
    }

    /** Always returns 0.0 — represents no match on any criterion. */
    private static class AlwaysZeroStrategy implements IMatchingStrategy {
        @Override public MatchingCriterion getCriterion() { return MatchingCriterion.SPECIES; }
        @Override public boolean isApplicable(Adopter adopter, Animal animal) { return true; }
        @Override public double score(Adopter adopter, Animal animal) { return 0.0; }
    }

    /** Returns 1.0 only if animal name equals "Rex", otherwise 0.0. */
    private static class FavorRexStrategy implements IMatchingStrategy {
        @Override public MatchingCriterion getCriterion() { return MatchingCriterion.BREED; }
        @Override public boolean isApplicable(Adopter adopter, Animal animal) { return true; }
        @Override public double score(Adopter adopter, Animal animal) {
            return "Rex".equals(animal.getName()) ? 1.0 : 0.0;
        }
    }

    /** Returns a fixed score for one criterion. */
    private static class FixedScoreStrategy implements IMatchingStrategy {
        private final MatchingCriterion criterion;
        private final double score;

        private FixedScoreStrategy(MatchingCriterion criterion, double score) {
            this.criterion = criterion;
            this.score = score;
        }

        @Override public MatchingCriterion getCriterion() { return criterion; }
        @Override public boolean isApplicable(Adopter adopter, Animal animal) { return true; }
        @Override public double score(Adopter adopter, Animal animal) { return score; }
    }

    /** Never applies to the current match. */
    private static class NeverApplicableStrategy implements IMatchingStrategy {
        @Override public MatchingCriterion getCriterion() { return MatchingCriterion.SPECIES; }
        @Override public boolean isApplicable(Adopter adopter, Animal animal) { return false; }
        @Override public double score(Adopter adopter, Animal animal) { return 0.0; }
    }

    private Adopter adopter;
    private Dog rex;
    private Dog buddy;

    @BeforeEach
    void setUp() {
        adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD, DailySchedule.HOME_MOST_OF_DAY,
                null, new AdopterPreferences(Species.DOG, null, ActivityLevel.MEDIUM, null, 0, 10));
        rex   = new Dog("Rex",   "Labrador", LocalDate.now().minusYears(3), ActivityLevel.MEDIUM, false, Dog.Size.LARGE, false);
        buddy = new Dog("Buddy", "Poodle",   LocalDate.now().minusYears(2), ActivityLevel.LOW,    false, Dog.Size.SMALL, false);
    }

    // === AdopterBasedMatchingServiceImpl ===

    @Test
    void adopterBased_ranksHigherScoringAnimalFirst() {
        AdopterBasedMatchingServiceImpl service =
                new AdopterBasedMatchingServiceImpl(List.of(new FavorRexStrategy()));
        List<MatchResult> results = service.match(adopter, List.of(buddy, rex));
        assertEquals("Rex", results.get(0).getAnimal().getName());
    }

    @Test
    void adopterBased_allSameScore_returnsBothResults() {
        AdopterBasedMatchingServiceImpl service =
                new AdopterBasedMatchingServiceImpl(List.of(new AlwaysOneStrategy()));
        List<MatchResult> results = service.match(adopter, List.of(rex, buddy));
        assertEquals(2, results.size());
        assertEquals(100, results.get(0).getScore());
    }

    @Test
    void adopterBased_emptyAnimalList_returnsEmpty() {
        AdopterBasedMatchingServiceImpl service =
                new AdopterBasedMatchingServiceImpl(List.of(new AlwaysOneStrategy()));
        assertTrue(service.match(adopter, List.of()).isEmpty());
    }

    @Test
    void adopterBased_zeroStrategy_allScoresZero() {
        AdopterBasedMatchingServiceImpl service =
                new AdopterBasedMatchingServiceImpl(List.of(new AlwaysZeroStrategy()));
        List<MatchResult> results = service.match(adopter, List.of(rex, buddy));
        assertEquals(0, results.get(0).getScore());
        assertEquals(0, results.get(1).getScore());
    }

    @Test
    void adopterBased_noApplicableStrategies_scoresEveryAnimalAsBestMatch() {
        AdopterBasedMatchingServiceImpl service =
                new AdopterBasedMatchingServiceImpl(List.of(new NeverApplicableStrategy()));
        List<MatchResult> results = service.match(adopter, List.of(rex, buddy));
        assertEquals(100, results.get(0).getScore());
        assertEquals(100, results.get(1).getScore());
    }

    @Test
    void adopterBased_rankedProfile_changesWeightedScore() {
        MatchingPreferencesProfile profile = new MatchingPreferencesProfile(List.of(
                new MatchingPreferencesPriority(MatchingCriterion.BREED, 1)
        ));
        AdopterBasedMatchingServiceImpl service = new AdopterBasedMatchingServiceImpl(
                List.of(
                        new FixedScoreStrategy(MatchingCriterion.SPECIES, 1.0),
                        new FixedScoreStrategy(MatchingCriterion.BREED, 0.0)
                ),
                profile);

        List<MatchResult> results = service.match(adopter, List.of(rex));

        // BREED is ranked, so its zero score has stronger influence than the unranked SPECIES score.
        assertEquals(33, results.get(0).getScore());
    }

    @Test
    void adopterBased_nullAdopter_throws() {
        AdopterBasedMatchingServiceImpl service =
                new AdopterBasedMatchingServiceImpl(List.of(new AlwaysOneStrategy()));
        assertThrows(IllegalArgumentException.class, () -> service.match(null, List.of(rex)));
    }

    @Test
    void adopterBased_nullAnimalList_throws() {
        AdopterBasedMatchingServiceImpl service =
                new AdopterBasedMatchingServiceImpl(List.of(new AlwaysOneStrategy()));
        assertThrows(IllegalArgumentException.class, () -> service.match(adopter, null));
    }

    @Test
    void adopterBased_constructor_nullStrategies_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new AdopterBasedMatchingServiceImpl(null));
    }

    @Test
    void adopterBased_constructor_emptyStrategies_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new AdopterBasedMatchingServiceImpl(List.of()));
    }

    // === AnimalBasedMatchingServiceImpl ===

    @Test
    void animalBased_ranksHigherScoringAdopterFirst() {
        // Strategy that always returns 1.0 for alice, 0.0 for bob — by checking adopter name
        IMatchingStrategy favorAlice = new IMatchingStrategy() {
            @Override public MatchingCriterion getCriterion() { return MatchingCriterion.SPECIES; }
            @Override public boolean isApplicable(Adopter adopter, Animal animal) { return true; }
            @Override public double score(Adopter a, Animal animal) {
                return "Alice".equals(a.getName()) ? 1.0 : 0.0;
            }
        };
        Adopter bob = new Adopter("Bob", LivingSpace.APARTMENT, DailySchedule.AWAY_MOST_OF_DAY,
                null, new AdopterPreferences(null, null, null, null, 0, 5));
        AnimalBasedMatchingServiceImpl service =
                new AnimalBasedMatchingServiceImpl(List.of(favorAlice));
        List<MatchResult> results = service.match(rex, List.of(bob, adopter));
        assertEquals("Alice", results.get(0).getAdopter().getName());
    }

    @Test
    void animalBased_emptyAdopterList_returnsEmpty() {
        AnimalBasedMatchingServiceImpl service =
                new AnimalBasedMatchingServiceImpl(List.of(new AlwaysOneStrategy()));
        assertTrue(service.match(rex, List.of()).isEmpty());
    }

    @Test
    void animalBased_multipleStrategies_scoresAccumulate() {
        // Two applicable strategies each returning 1.0 still produce a perfect score.
        AnimalBasedMatchingServiceImpl service =
                new AnimalBasedMatchingServiceImpl(List.of(new AlwaysOneStrategy(), new AlwaysOneStrategy()));
        List<MatchResult> results = service.match(rex, List.of(adopter));
        assertEquals(100, results.get(0).getScore());
    }

    @Test
    void animalBased_nullAnimal_throws() {
        AnimalBasedMatchingServiceImpl service =
                new AnimalBasedMatchingServiceImpl(List.of(new AlwaysOneStrategy()));
        assertThrows(IllegalArgumentException.class, () -> service.match(null, List.of(adopter)));
    }

    @Test
    void animalBased_nullAdopterList_throws() {
        AnimalBasedMatchingServiceImpl service =
                new AnimalBasedMatchingServiceImpl(List.of(new AlwaysOneStrategy()));
        assertThrows(IllegalArgumentException.class, () -> service.match(rex, null));
    }

    @Test
    void animalBased_constructor_nullStrategies_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new AnimalBasedMatchingServiceImpl(null));
    }
}
