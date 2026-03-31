package shelter.strategy;

import shelter.domain.ActivityLevel;
import shelter.domain.Adopter;
import shelter.domain.Animal;

/**
 * A concrete matching strategy that evaluates compatibility between the adopter's
 * preferred activity level and the animal's activity level.
 */
public class ActivityLevelStrategy implements IMatchingStrategy {

    /**
     * Returns the matching criterion handled by this strategy.
     *
     * @return {@link MatchingCriterion#ACTIVITY_LEVEL}
     */
    @Override
    public MatchingCriterion getCriterion() {
        return MatchingCriterion.ACTIVITY_LEVEL;
    }

    /**
     * Returns the score contributed by this strategy for the given adopter-animal pair.
     * An exact activity match returns {@code 1.0}, a nearby match returns {@code 0.5},
     * and a poor match returns {@code 0.0}.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return the compatibility score for activity level
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

        ActivityLevel preferredActivity = adopter.getPreferences().getPreferredActivityLevel();
        ActivityLevel animalActivity = animal.getActivityLevel();

        if (preferredActivity == null) {
            return 0.0;
        }

        if (preferredActivity == animalActivity) {
            return 1.0;
        }

        if ((preferredActivity == ActivityLevel.LOW && animalActivity == ActivityLevel.MEDIUM)
                || (preferredActivity == ActivityLevel.MEDIUM && animalActivity == ActivityLevel.LOW)
                || (preferredActivity == ActivityLevel.MEDIUM && animalActivity == ActivityLevel.HIGH)
                || (preferredActivity == ActivityLevel.HIGH && animalActivity == ActivityLevel.MEDIUM)) {
            return 0.5;
        }

        return 0.0;
    }
}
