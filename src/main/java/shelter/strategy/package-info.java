/**
 * Strategy layer — pluggable scoring rules for animal-adopter matching.
 * Each {@link shelter.strategy.IMatchingStrategy} implementation evaluates one criterion
 * (species, breed, activity level, age, lifestyle, vaccination) and returns a normalized score.
 */
package shelter.strategy;
