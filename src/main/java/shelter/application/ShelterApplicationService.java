package shelter.application;

import shelter.domain.Shelter;

import java.util.List;

/**
 * Application service for shelter management use cases.
 * Orchestrates underlying service calls to register, update, remove, and list shelters,
 * ensuring audit logging and consistency across operations.
 */
public interface ShelterApplicationService {

    /**
     * Registers a new shelter with the given name, location, and capacity.
     * Throws an exception if a shelter with the same name and location already exists.
     *
     * @param name     the shelter name; must not be null or blank
     * @param location the shelter location; must not be null or blank
     * @param capacity the maximum number of animals the shelter can hold; must be positive
     * @return the newly created {@link Shelter}
     */
    Shelter registerShelter(String name, String location, int capacity);

    /**
     * Returns a list of all registered shelters in the system.
     * Returns an empty list if no shelters have been registered.
     *
     * @return a list of all shelters
     */
    List<Shelter> listShelters();

    /**
     * Updates an existing shelter's information with the provided values.
     * Only non-null parameters are applied; omitted (null) fields retain their current values.
     * Throws an exception if the shelter is not found.
     *
     * @param shelterId the ID of the shelter to update; must not be null or blank
     * @param name      the new name, or {@code null} to keep the current value
     * @param location  the new location, or {@code null} to keep the current value
     * @param capacity  the new capacity, or {@code null} to keep the current value
     * @return the updated {@link Shelter}
     */
    Shelter updateShelter(String shelterId, String name, String location, Integer capacity);

    /**
     * Removes a shelter from the system by ID.
     * Throws an exception if the shelter is not found, still holds animals, or has pending transfer requests.
     *
     * @param shelterId the ID of the shelter to remove; must not be null or blank
     */
    void removeShelter(String shelterId);
}
