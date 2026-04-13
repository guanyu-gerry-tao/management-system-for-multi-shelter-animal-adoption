package shelter.strategy;

import shelter.domain.ActivityLevel;
import shelter.domain.Adopter;
import shelter.domain.Animal;
import shelter.domain.Cat;
import shelter.domain.DailySchedule;
import shelter.domain.Dog;
import shelter.domain.LivingSpace;

/**
 * A concrete matching strategy that evaluates whether an animal is a practical
 * fit for the adopter's living space and daily schedule.
 * This strategy answers the question: "Can the adopter realistically support this animal
 * in daily life?" It focuses on feasibility rather than stated preference.
 * Unlike {@link ActivityLevelStrategy}, which checks what the adopter wants,
 * this strategy checks whether the adopter's home environment and availability
 * can support the animal's care needs.
 */
public class LifestyleCompatibilityStrategy implements IMatchingStrategy {

    /**
     * Returns the matching criterion handled by this strategy.
     *
     * @return {@link MatchingCriterion#LIFESTYLE}
     */
    @Override
    public MatchingCriterion getCriterion() {
        return MatchingCriterion.LIFESTYLE;
    }

    /**
     * Returns whether lifestyle compatibility can be evaluated for this adopter-animal pair.
     * This strategy is based on the adopter's living space and schedule, so it is applicable
     * whenever both inputs are present.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return {@code true} if both inputs are non-null
     * @throws IllegalArgumentException if {@code adopter} or {@code animal} is {@code null}
     */
    @Override
    public boolean isApplicable(Adopter adopter, Animal animal) {
        if (adopter == null) {
            throw new IllegalArgumentException("Adopter must not be null.");
        }
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }

        return true;
    }

    /**
     * Returns the score contributed by this strategy for the given adopter-animal pair.
     * The score is based on a simple combination of living-space fit and schedule fit.
     * It reflects practical feasibility, not personal preference.
     *
     * <p>In other words, this strategy measures whether the adopter's current lifestyle
     * can support the animal in practice.</p>
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return the practical-fit score for lifestyle compatibility
     * @throws IllegalArgumentException if {@code adopter} or {@code animal} is {@code null}
     */
    @Override
    public double score(Adopter adopter, Animal animal) {
        if (adopter == null) {
            throw new IllegalArgumentException("Adopter must not be null.");
        }
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }

        double livingSpaceScore = calculateLivingSpaceScore(adopter, animal);
        double scheduleScore = calculateScheduleScore(adopter, animal);

        // Lifestyle fit is treated as a balance between home environment and time availability.
        return (livingSpaceScore + scheduleScore) / 2.0;
    }

    /**
     * Estimates whether the adopter's home environment is suitable for the animal.
     */
    private double calculateLivingSpaceScore(Adopter adopter, Animal animal) {
        LivingSpace livingSpace = adopter.getLivingSpace();

        if (animal instanceof Dog dog) {
            // Living-space scoring focuses on whether the home can physically accommodate the animal.
            // Large dogs are the weakest fit for apartment living.
            if (livingSpace == LivingSpace.APARTMENT && dog.getSize() == Dog.Size.LARGE) {
                return 0.0;
            }
            // Medium dogs may still work in apartments, but the fit is not ideal.
            if (livingSpace == LivingSpace.APARTMENT && dog.getSize() == Dog.Size.MEDIUM) {
                return 0.5;
            }
            // Large dogs without a yard are possible, but still not the strongest match.
            if (livingSpace == LivingSpace.HOUSE_NO_YARD && dog.getSize() == Dog.Size.LARGE) {
                return 0.5;
            }
            // A house with a yard is treated as the strongest fit for any dog size.
            if (livingSpace == LivingSpace.HOUSE_WITH_YARD) {
                return 1.0;
            }
            return 1.0;
        }

        if (animal instanceof Cat cat) {
            // Indoor cats are usually a strong fit for apartment-style homes.
            if (livingSpace == LivingSpace.APARTMENT && cat.isIndoor()) {
                return 1.0;
            }
            return 0.8;
        }

        // Rabbits are treated as generally adaptable for now.
        return 0.8;
    }

    /**
     * Estimates whether the adopter's daily routine can support the animal's care demand.
     */
    private double calculateScheduleScore(Adopter adopter, Animal animal) {
        DailySchedule dailySchedule = adopter.getDailySchedule();
        ActivityLevel activityLevel = animal.getActivityLevel();

        // Schedule scoring uses activity level only as a proxy for care demand,
        // not as an adopter preference. Higher-energy animals generally need more time and attention.
        // Being home most of the day gives the strongest schedule fit.
        if (dailySchedule == DailySchedule.HOME_MOST_OF_DAY) {
            return 1.0;
        }

        if (dailySchedule == DailySchedule.AWAY_PART_OF_DAY) {
            // High-energy animals are harder to support when the adopter is away more often.
            if (activityLevel == ActivityLevel.HIGH) {
                return 0.5;
            }
            return 1.0;
        }

        // High-energy animals are the weakest fit when the adopter is away most of the day.
        if (activityLevel == ActivityLevel.HIGH) {
            return 0.0;
        }

        // Lower-energy animals may still work, but the schedule is not ideal.
        return 0.5;
    }
}
