/*
Modificaition on Domain/ActivityLevel is Needed.
Scoring rule need to be adjuted later. 
How does this apply to rabbit? 
Score 1 for rabbit since rabit neither required outdoor activity nor extra space. 
*/


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
     * Returns the score contributed by this strategy for the given adopter-animal pair.
     * The score is based on a simple combination of living-space fit and schedule fit.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return the compatibility score for lifestyle fit
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

    private double calculateLivingSpaceScore(Adopter adopter, Animal animal) {
        LivingSpace livingSpace = adopter.getLivingSpace();

        if (animal instanceof Dog dog) {
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

    private double calculateScheduleScore(Adopter adopter, Animal animal) {
        DailySchedule dailySchedule = adopter.getDailySchedule();
        ActivityLevel activityLevel = animal.getActivityLevel();

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
