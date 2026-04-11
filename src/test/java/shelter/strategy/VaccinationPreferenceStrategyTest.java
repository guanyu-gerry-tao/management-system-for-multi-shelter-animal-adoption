package shelter.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.*;
import shelter.service.VaccinationInfoProvider;
import shelter.service.model.OverdueVaccination;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link VaccinationPreferenceStrategy}.
 * Covers null guards, applicability behavior, exact matches, no matches, and criterion identity.
 */
class VaccinationPreferenceStrategyTest {

    private VaccinationPreferenceStrategy strategy;
    private Dog dog;

    @BeforeEach
    void setUp() {
        strategy = new VaccinationPreferenceStrategy(
                new StubVaccinationInfoProvider(List.of(), List.of()));
        dog = new Dog("Rex", "Labrador", LocalDate.now().minusYears(3),
                ActivityLevel.MEDIUM, false, Dog.Size.LARGE, false);
    }

    @Test
    void score_nullAdopter_throws() {
        assertThrows(IllegalArgumentException.class, () -> strategy.score(null, dog));
    }

    @Test
    void score_nullAnimal_throws() {
        assertThrows(IllegalArgumentException.class, () -> strategy.score(adopter(true), null));
    }

    @Test
    void isApplicable_noPreferenceSet_returnsFalse() {
        assertFalse(strategy.isApplicable(adopter(null), dog));
    }

    @Test
    void score_exactMatch_returnsFullScore() {
        VaccineType rabies = new VaccineType("Rabies", Species.DOG, 365);
        VaccinationPreferenceStrategy strategy =
                new VaccinationPreferenceStrategy(
                        new StubVaccinationInfoProvider(List.of(rabies), List.of()));

        assertEquals(1.0, strategy.score(adopter(true), dog));
    }

    // VaccinationPreferenceStrategy has binary applicability from the adopter's perspective,
    // but the actual score supports partial credit for overdue vaccine records.

    @Test
    void score_noMatch_returnsZero() {
        VaccineType rabies = new VaccineType("Rabies", Species.DOG, 365);
        VaccinationPreferenceStrategy strategy =
                new VaccinationPreferenceStrategy(
                        new StubVaccinationInfoProvider(
                                List.of(rabies),
                                List.of(new OverdueVaccination(
                                        rabies, null, LocalDate.now().minusDays(1)))));

        assertEquals(0.0, strategy.score(adopter(true), dog));
    }

    @Test
    void score_partialMatch_returnsPartialScore() {
        VaccineType rabies = new VaccineType("Rabies", Species.DOG, 365);
        VaccineType bordetella = new VaccineType("Bordetella", Species.DOG, 365);
        VaccineType distemper = new VaccineType("Distemper", Species.DOG, 365);
        List<VaccineType> applicableTypes = List.of(rabies, bordetella, distemper);
        List<OverdueVaccination> overdueVaccinations = List.of(
                new OverdueVaccination(bordetella, LocalDate.now().minusYears(2), LocalDate.now().minusDays(1)),
                new OverdueVaccination(distemper, null, LocalDate.now().minusDays(1))
        );
        VaccinationPreferenceStrategy strategy =
                new VaccinationPreferenceStrategy(
                        new StubVaccinationInfoProvider(applicableTypes, overdueVaccinations));

        assertEquals(0.5, strategy.score(adopter(true), dog));
    }

    @Test
    void getCriterion_returnsCorrectEnum() {
        assertEquals(MatchingCriterion.VACCINATION, strategy.getCriterion());
    }

    @Test
    void constructor_nullProvider_throws() {
        assertThrows(IllegalArgumentException.class, () -> new VaccinationPreferenceStrategy(null));
    }

    private Adopter adopter(Boolean requiresVaccinated) {
        return new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD, DailySchedule.HOME_MOST_OF_DAY,
                null, new AdopterPreferences(null, null, null, requiresVaccinated, 0, 10));
    }

    private static class StubVaccinationInfoProvider implements VaccinationInfoProvider {

        private final List<VaccineType> applicableTypes;
        private final List<OverdueVaccination> overdueVaccinations;

        private StubVaccinationInfoProvider(List<VaccineType> applicableTypes,
                                            List<OverdueVaccination> overdueVaccinations) {
            this.applicableTypes = applicableTypes;
            this.overdueVaccinations = overdueVaccinations;
        }

        @Override
        public List<VaccineType> getApplicableVaccineTypes(Animal animal) {
            return applicableTypes;
        }

        @Override
        public List<OverdueVaccination> getOverdueVaccinations(Animal animal) {
            return overdueVaccinations;
        }
    }
}
