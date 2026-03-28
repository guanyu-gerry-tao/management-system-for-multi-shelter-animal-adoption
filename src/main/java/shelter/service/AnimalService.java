package shelter.service;

import shelter.domain.Adopter;
import shelter.domain.Animal;
import shelter.domain.Shelter;

import java.time.LocalDate;
import java.util.List;

/**
 * Manages animal records within the system, including registration, updates, and removal.
 * Implementations are responsible for maintaining consistency between animal records and shelter rosters.
 */
public interface AnimalService {

    /**
     * Registers a new animal into the specified shelter.
     * Throws an exception if the shelter is at capacity or the animal is already registered.
     *
     * @param animal  the animal to register
     * @param shelter the shelter to assign the animal to
     */
    void register(Animal animal, Shelter shelter);

    /**
     * Updates the information of an existing animal record; the animal may be any subtype such as Dog, Cat, or Rabbit.
     * Throws an exception if the animal is not found in the system.
     *
     * @param animal the animal with updated information
     */
    void update(Animal animal);

    /**
     * Removes an animal from the system and its associated shelter.
     * Throws an exception if the animal is not found or has a pending adoption request.
     *
     * @param animal the animal to remove
     */
    void remove(Animal animal);

    /**
     * Returns all animals currently registered in the specified shelter.
     * Returns an empty list if the shelter has no animals.
     *
     * @param shelter the shelter to query
     * @return a list of animals in the shelter
     */
    List<Animal> getAnimalsByShelter(Shelter shelter);

    /**
     * Returns all animals registered into the system after the given date.
     * Returns an empty list if no animals were registered after that date.
     *
     * @param date the cutoff date (exclusive)
     * @return a list of animals registered after the date
     */
    List<Animal> registeredAfter(LocalDate date);

    /**
     * Returns all animals that were adopted after the given date.
     * Returns an empty list if no animals were adopted after that date.
     *
     * @param date the cutoff date (exclusive)
     * @return a list of animals adopted after the date
     */
    List<Animal> adoptedAfter(LocalDate date);

    /**
     * Returns all animals adopted by the given adopter.
     * Returns an empty list if the adopter has not adopted any animals.
     *
     * @param adopter the adopter to query
     * @return a list of animals adopted by the adopter
     */
    List<Animal> adoptedBy(Adopter adopter);
}
