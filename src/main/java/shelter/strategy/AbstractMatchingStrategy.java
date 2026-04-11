package shelter.strategy;

import shelter.domain.Adopter;
import shelter.domain.Animal;

/**
 * Shared base class for matching strategies that need common input validation.
 */
public abstract class AbstractMatchingStrategy implements IMatchingStrategy {

    /**
     * Validates that both inputs are present before a strategy performs any work.
     *
     * @param adopter the adopter being evaluated
     * @param animal the animal being evaluated
     * @throws IllegalArgumentException if either argument is {@code null}
     */
    protected void validateInputs(Adopter adopter, Animal animal) {
        if (adopter == null) {
            throw new IllegalArgumentException("Adopter must not be null.");
        }
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }
    }
}
