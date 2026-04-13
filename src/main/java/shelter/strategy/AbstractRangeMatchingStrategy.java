package shelter.strategy;

import shelter.domain.Adopter;
import shelter.domain.Animal;

/**
 * Base class for strategies that score how close a value is to a preferred range.
 */
public abstract class AbstractRangeMatchingStrategy extends AbstractMatchingStrategy {

    /**
     * Scores the adopter-animal pair using the subclass-provided distance from the
     * preferred range.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return a normalized score in the range {@code [0.0, 1.0]}
     */
    @Override
    public double score(Adopter adopter, Animal animal) {
        validateInputs(adopter, animal);

        double distanceFromPreferredRange = getDistanceFromPreferredRange(adopter, animal);
        if (distanceFromPreferredRange == 0.0) {
            return 1.0;
        }
        if (distanceFromPreferredRange <= 0.5) {
            return 0.8;
        }
        if (distanceFromPreferredRange <= 1.0) {
            return 0.5;
        }
        return 0.0;
    }

    /**
     * Returns the distance between the animal's value and the preferred range.
     * A value inside the range should return {@code 0.0}.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return the distance from the preferred range
     */
    protected abstract double getDistanceFromPreferredRange(Adopter adopter, Animal animal);
}
