package shelter.service;

import shelter.domain.VaccineType;

import java.util.List;

/**
 * Manages the catalog of vaccine types available in the system, supporting full CRUD operations.
 * Each vaccine type is uniquely identified by its ID; name is used for lookup convenience
 * but may be updated.
 */
public interface VaccineTypeCatalogService {

    /**
     * Adds a new vaccine type to the catalog.
     * Throws an exception if a vaccine type with the same name already exists.
     *
     * @param vaccineType the vaccine type to add
     */
    void add(VaccineType vaccineType);

    /**
     * Updates an existing vaccine type in the catalog.
     * The vaccine type is identified by its ID; all mutable fields are replaced with those of the given object.
     * Throws an exception if the vaccine type is not found.
     *
     * @param vaccineType the vaccine type with updated information
     */
    void update(VaccineType vaccineType);

    /**
     * Removes a vaccine type from the catalog by ID.
     * Throws an exception if no vaccine type with that ID is found.
     *
     * @param id the ID of the vaccine type to remove
     */
    void remove(String id);

    /**
     * Returns the vaccine type with the given ID.
     * Throws an exception if no matching vaccine type is found.
     *
     * @param id the ID to look up
     * @return the matching vaccine type
     */
    VaccineType findById(String id);

    /**
     * Returns the vaccine type with the given name.
     * Throws an exception if no matching vaccine type is found.
     *
     * @param name the name to look up
     * @return the matching vaccine type
     */
    VaccineType findByName(String name);

    /**
     * Returns all vaccine types currently in the catalog.
     * Returns an empty list if the catalog is empty.
     *
     * @return a list of all vaccine types
     */
    List<VaccineType> listAll();
}
