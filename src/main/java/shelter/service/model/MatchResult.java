package shelter.service.model;

import shelter.domain.Animal;
import shelter.domain.Adopter;

import java.util.Objects;

/**
 * Represents the result of a single match between an animal and an adopter.
 * Holds both parties and the computed score, supporting both forward and reverse matching directions.
 * Results are naturally ordered by score descending, allowing ranked lists to be sorted directly.
 */
public class MatchResult implements Comparable<MatchResult> {

    private final Animal animal;
    private final Adopter adopter;
    private final int score;

    /**
     * Constructs a MatchResult with the given animal, adopter, and score.
     * Higher scores indicate a stronger compatibility between the two parties.
     * Neither {@code animal} nor {@code adopter} may be null.
     *
     * @param animal  the animal involved in this match; must not be null
     * @param adopter the adopter involved in this match; must not be null
     * @param score   the computed match score
     * @throws IllegalArgumentException if {@code animal} or {@code adopter} is null
     */
    public MatchResult(Animal animal, Adopter adopter, int score) {
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }
        if (adopter == null) {
            throw new IllegalArgumentException("Adopter must not be null.");
        }
        this.animal = animal;
        this.adopter = adopter;
        this.score = score;
    }

    /**
     * Constructs a copy of the given match result, preserving all field values.
     * This copy constructor creates an independent snapshot of an existing result.
     *
     * @param other the match result to copy; must not be null
     */
    public MatchResult(MatchResult other) {
        this.animal = other.animal;
        this.adopter = other.adopter;
        this.score = other.score;
    }

    /**
     * Returns the animal associated with this match result.
     * Never null for a validly constructed result.
     *
     * @return the matched animal
     */
    public Animal getAnimal() {
        return animal;
    }

    /**
     * Returns the adopter associated with this match result.
     * Never null for a validly constructed result.
     *
     * @return the matched adopter
     */
    public Adopter getAdopter() {
        return adopter;
    }

    /**
     * Returns the match score for this result.
     * Higher values indicate greater compatibility between the animal and adopter.
     *
     * @return the computed score
     */
    public int getScore() {
        return score;
    }

    /**
     * Compares this result to another by score in descending order.
     * A result with a higher score is ordered before one with a lower score,
     * so that sorting a list of MatchResults places the best matches first.
     *
     * @param other the other MatchResult to compare to
     * @return a negative number if this result has a higher score, positive if lower, zero if equal
     */
    @Override
    public int compareTo(MatchResult other) {
        return Integer.compare(other.score, this.score);
    }

    /**
     * Returns a string representation of this match result including animal, adopter, and score.
     *
     * @return a human-readable description of this match result
     */
    @Override
    public String toString() {
        return "MatchResult[animal=" + animal.getName()
                + ", adopter=" + adopter.getName()
                + ", score=" + score + "]";
    }

    /**
     * Returns true if the given object is a MatchResult with the same animal, adopter, and score.
     * Equality is value-based since match results have no unique ID.
     *
     * @param o the object to compare
     * @return true if all fields are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchResult)) return false;
        MatchResult other = (MatchResult) o;
        return score == other.score
                && Objects.equals(animal, other.animal)
                && Objects.equals(adopter, other.adopter);
    }

    /**
     * Returns a hash code based on animal, adopter, and score.
     * Consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(animal, adopter, score);
    }
}
