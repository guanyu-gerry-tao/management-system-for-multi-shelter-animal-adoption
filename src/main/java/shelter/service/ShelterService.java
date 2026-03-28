package shelter.service;

import shelter.domain.Shelter;

import java.util.List;

/**
 * Manages shelter records within the system, including registration, updates, and closure.
 * Implementations are responsible for ensuring shelter capacity constraints are respected.
 */
public interface ShelterService {

    /**
     * Registers a new shelter into the system.
     * Throws an exception if a shelter with the same name and location already exists.
     *
     * @param shelter the shelter to register
     */
    void register(Shelter shelter);

    /**
     * Updates the information of an existing shelter record, such as capacity or location.
     * Throws an exception if the shelter is not found in the system.
     *
     * @param shelter the shelter with updated information
     */
    void update(Shelter shelter);

    /**
     * Removes a shelter from the system.
     * Throws an exception if the shelter still holds animals or has pending transfer requests.
     *
     * @param shelter the shelter to remove
     */
    void remove(Shelter shelter);

    /**
     * Returns all shelters currently registered in the system.
     * Returns an empty list if no shelters are registered.
     *
     * @return a list of all registered shelters
     */
    List<Shelter> listAll();
}
