package shelter.strategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import shelter.domain.Adopter;
import shelter.domain.Animal;

/**
 * A concrete matching strategy that evaluates whether an animal's age
 * fits the adopter's preferred age range.
 * The age is measured from {@code Animal.getBirthday()} so the strategy can use
 * finer-grained age differences than whole years.
 */
public class AgePreferenceStrategy extends AbstractRangeMatchingStrategy {

    /**
     * Returns the matching criterion handled by this strategy.
     *
     * @return {@link MatchingCriterion#AGE}
     */
    @Override
    public MatchingCriterion getCriterion() {
        return MatchingCriterion.AGE;
    }

    /**
     * Returns whether the adopter has expressed an age preference.
     * In the current design, an age range of {@code 0..Integer.MAX_VALUE}
     * is treated as "no age preference".
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return {@code true} if the age criterion should be counted; {@code false} otherwise
     * @throws IllegalArgumentException if {@code adopter} or {@code animal} is {@code null}
     */
    @Override
    public boolean isApplicable(Adopter adopter, Animal animal) {
        validateInputs(adopter, animal);

        int minAge = adopter.getPreferences().getMinAge();
        int maxAge = adopter.getPreferences().getMaxAge();
        return !(minAge == 0 && maxAge == Integer.MAX_VALUE);
    }

    /**
     * Returns how far the animal's age is from the adopter's preferred age range.
     * A value inside the range returns {@code 0.0}.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return the distance from the preferred age range
     */
    @Override
    protected double getDistanceFromPreferredRange(Adopter adopter, Animal animal) {
        int minAge = adopter.getPreferences().getMinAge();
        int maxAge = adopter.getPreferences().getMaxAge();
        double animalAge = calculatePreciseAgeInYears(animal.getBirthday());
        return calculateDistanceFromRange(animalAge, minAge, maxAge);
    }

    /**
     * Calculates the animal's age in years using whole months for more precision than
     * {@code Animal.getAge()}, which returns only whole years.
     */
    private double calculatePreciseAgeInYears(LocalDate birthday) {
        long ageInMonths = ChronoUnit.MONTHS.between(birthday, LocalDate.now());
        return ageInMonths / 12.0;
    }

    /**
     * Calculates how far an age is from the nearest edge of the preferred range.
     * A value inside the range has distance {@code 0.0}.
     */
    private double calculateDistanceFromRange(double age, int minAge, int maxAge) {
        if (age < minAge) {
            return minAge - age;
        }
        if (age > maxAge) {
            return age - maxAge;
        }
        return 0.0;
    }
}
