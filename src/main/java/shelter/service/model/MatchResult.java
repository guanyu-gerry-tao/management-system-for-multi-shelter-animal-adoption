package shelter.service.model;

import shelter.domain.Animal;
import shelter.domain.Adopter;

/**
 * Represents the result of a single match between an animal and an adopter.
 * Holds both parties and the computed score, supporting both forward and reverse matching directions.
 */
public class MatchResult {

    private final Animal animal;
    private final Adopter adopter;
    private final int score;

    /**
     * Constructs a MatchResult with the given animal, adopter, and score.
     * Higher scores indicate a stronger compatibility between the two parties.
     *
     * @param animal  the animal involved in this match
     * @param adopter the adopter involved in this match
     * @param score   the computed match score
     */
    public MatchResult(Animal animal, Adopter adopter, int score) {
        this.animal = animal;
        this.adopter = adopter;
        this.score = score;
    }

    /**
     * Returns the animal associated with this match result.
     *
     * @return the matched animal
     */
    public Animal getAnimal() {
        return animal;
    }

    /**
     * Returns the adopter associated with this match result.
     *
     * @return the matched adopter
     */
    public Adopter getAdopter() {
        return adopter;
    }

    /**
     * Returns the match score for this result.
     *
     * @return the computed score
     */
    public int getScore() {
        return score;
    }
}
