package shelter.strategy;

import shelter.domain.ActivityLevel;
import shelter.domain.Adopter;
import shelter.domain.Animal;

/**
 * A concrete matching strategy that evaluates preference alignment between the
 * adopter's desired activity level and the animal's activity level.
 * This strategy answers the question: "Is this the kind of energy level the adopter wants?"
 * It does not consider whether the adopter's daily life can realistically support that level;
 * that practical fit is handled separately by {@link LifestyleCompatibilityStrategy}.
 */
public class ActivityLevelStrategy extends AbstractOrdinalMatchingStrategy {

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
     * Returns whether the adopter has set an activity-level preference.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return {@code true} if the adopter has an activity preference; {@code false} otherwise
     * @throws IllegalArgumentException if {@code adopter} or {@code animal} is {@code null}
     */
    @Override
    public boolean isApplicable(Adopter adopter, Animal animal) {
        validateInputs(adopter, animal);

        return adopter.getPreferences().getPreferredActivityLevel() != null;
    }

    /**
     * Returns the ordinal distance between the adopter's preferred activity level
     * and the animal's actual activity level.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return the ordinal distance, where {@code 0} means exact match
     */
    @Override
    protected int getOrdinalDistance(Adopter adopter, Animal animal) {
        ActivityLevel preferredActivity = adopter.getPreferences().getPreferredActivityLevel();
        ActivityLevel animalActivity = animal.getActivityLevel();

        return Math.abs(getLevelRank(preferredActivity) - getLevelRank(animalActivity));
    }

    /**
     * Returns a simple ordinal rank for each activity level.
     */
    private int getLevelRank(ActivityLevel activityLevel) {
        if (activityLevel == ActivityLevel.LOW) {
            return 0;
        }
        if (activityLevel == ActivityLevel.MEDIUM) {
            return 1;
        }
        return 2;
    }
}
