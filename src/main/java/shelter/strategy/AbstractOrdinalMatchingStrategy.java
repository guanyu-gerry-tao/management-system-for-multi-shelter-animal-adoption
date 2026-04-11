package shelter.strategy;

import shelter.domain.Adopter;
import shelter.domain.Animal;

/**
 * Base class for ordinal strategies where closer values receive stronger scores.
 */
public abstract class AbstractOrdinalMatchingStrategy extends AbstractMatchingStrategy {

    /**
     * Scores the adopter-animal pair using the ordinal distance supplied by the subclass.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return a normalized score in the range {@code [0.0, 1.0]}
     */
    @Override
    public double score(Adopter adopter, Animal animal) {
        validateInputs(adopter, animal);

        int distance = getOrdinalDistance(adopter, animal);
        if (distance == 0) {
            return 1.0;
        }
        if (distance == 1) {
            return 0.5;
        }
        return 0.0;
    }

    /**
     * Returns the ordinal distance between the adopter's preference and the animal's value.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return the ordinal distance, where {@code 0} means exact match
     */
    protected abstract int getOrdinalDistance(Adopter adopter, Animal animal);
}
