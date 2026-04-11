package shelter.strategy;

import shelter.domain.Adopter;
import shelter.domain.Animal;

/**
 * Base class for binary match strategies that return either a full match
 * or no match.
 */
public abstract class AbstractBinaryMatchingStrategy extends AbstractMatchingStrategy {

    /**
     * Returns {@code 1.0} for a binary match and {@code 0.0} otherwise.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return {@code 1.0} when the binary condition matches; {@code 0.0} otherwise
     */
    @Override
    public double score(Adopter adopter, Animal animal) {
        validateInputs(adopter, animal);
        return isMatch(adopter, animal) ? 1.0 : 0.0;
    }

    /**
     * Returns whether the adopter-animal pair is a binary match under this criterion.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @return {@code true} if the pair matches under this criterion
     */
    protected abstract boolean isMatch(Adopter adopter, Animal animal);
}
