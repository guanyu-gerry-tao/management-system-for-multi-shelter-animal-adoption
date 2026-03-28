package shelter.service;

import shelter.domain.Adopter;
import shelter.domain.Animal;

import java.time.LocalDate;
import java.util.List;

/**
 * Manages adopter records within the system, including registration, updates, and removal.
 * Implementations are responsible for validating adopter information before persisting changes.
 */
public interface AdopterService {

    /**
     * Registers a new adopter into the system.
     * Throws an exception if the adopter is already registered or required fields are missing.
     *
     * @param adopter the adopter to register
     */
    void register(Adopter adopter);

    /**
     * Updates the information of an existing adopter record.
     * Throws an exception if the adopter is not found in the system.
     *
     * @param adopter the adopter with updated information
     */
    void update(Adopter adopter);

    /**
     * Removes an adopter from the system.
     * Throws an exception if the adopter is not found or has a pending adoption request.
     *
     * @param adopter the adopter to remove
     */
    void remove(Adopter adopter);

    /**
     * Returns all adopters currently registered in the system.
     * Returns an empty list if no adopters are registered.
     *
     * @return a list of all registered adopters
     */
    List<Adopter> listAll();

    /**
     * Returns the adopter with the given ID.
     * Throws an exception if no adopter with that ID is found.
     *
     * @param id the unique identifier of the adopter
     * @return the matching adopter
     */
    Adopter findById(String id);

    /**
     * Returns all adopters who registered after the given date.
     * Returns an empty list if no adopters registered after that date.
     *
     * @param date the cutoff date (exclusive)
     * @return a list of adopters registered after the date
     */
    List<Adopter> registeredAfter(LocalDate date);

    /**
     * Returns all adopters who have at least one approved adoption after the given date.
     * Returns an empty list if no such adopters exist.
     *
     * @param date the cutoff date (exclusive)
     * @return a list of adopters with an approved adoption after the date
     */
    List<Adopter> adoptedAfter(LocalDate date);

    /**
     * Returns all adopters who have adopted the given animal.
     * Returns an empty list if no adopter has adopted the animal.
     *
     * @param animal the animal to query
     * @return a list of adopters who adopted the animal
     */
    List<Adopter> adoptedAnimal(Animal animal);
}
