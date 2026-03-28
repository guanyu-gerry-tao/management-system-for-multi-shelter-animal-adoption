package shelter.service;

import shelter.domain.Staff;

import java.util.List;

/**
 * Loads the current operator's staff profile from a file at application startup.
 * The loaded Staff object represents the person performing operations in this session.
 */
public interface StaffService {

    /**
     * Reads a staff profile from the given file path and returns the corresponding Staff object.
     * Throws an exception if the file cannot be read or the profile data is invalid.
     *
     * @param filePath the path to the staff profile file (CSV or JSON)
     * @return the Staff object representing the current operator
     */
    Staff loadCurrentStaff(String filePath);

    /**
     * Returns all staff members registered in the system.
     * Returns an empty list if no staff are registered.
     *
     * @return a list of all staff members
     */
    List<Staff> listAll();

    /**
     * Returns all staff members with the given role.
     * Returns an empty list if no staff with that role exist.
     *
     * @param role the role to filter by (e.g. "Veterinarian", "Coordinator")
     * @return a list of staff members with the specified role
     */
    List<Staff> findByRole(String role);
}
