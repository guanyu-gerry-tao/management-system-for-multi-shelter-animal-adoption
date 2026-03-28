package shelter.service;

import shelter.domain.VaccineType;

import java.util.List;

/**
 * Manages the catalog of vaccine types available in the system, supporting full CRUD operations.
 * The catalog can be initialized from and persisted to a CSV file for easy configuration.
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
     * Throws an exception if the vaccine type is not found.
     *
     * @param vaccineType the vaccine type with updated information
     */
    void update(VaccineType vaccineType);

    /**
     * Removes a vaccine type from the catalog by name.
     * Throws an exception if the vaccine type is not found.
     *
     * @param name the name of the vaccine type to remove
     */
    void remove(String name);

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

    /**
     * Loads vaccine types from a CSV file, replacing the current catalog contents.
     * Throws an exception if the file path is invalid or the file format is incorrect.
     *
     * @param filePath the path to the CSV file
     */
    void loadFromCsv(String filePath);

    /**
     * Saves the current catalog contents to a CSV file.
     * Throws an exception if the file cannot be written.
     *
     * @param filePath the path to the CSV file
     */
    void saveToCsv(String filePath);
}
