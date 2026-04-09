package shelter.service.impl;

import shelter.domain.Staff;
import shelter.service.StaffService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Default implementation of {@link StaffService} that loads a staff profile from a CSV file.
 * In the current demo, a single hardcoded admin staff member is used; this service reads
 * the operator's name and role from a one-line CSV file at the given path.
 */
public class StaffServiceImpl implements StaffService {

    /**
     * Constructs a StaffServiceImpl with no dependencies.
     * Staff data is read directly from the file system on demand.
     */
    public StaffServiceImpl() {
    }

    /**
     * {@inheritDoc}
     * Reads the first data line of the CSV file at {@code filePath} and constructs a Staff object.
     * Expected CSV format: {@code name,role}. Throws an exception if the file cannot be read
     * or does not contain at least one data line after the header.
     *
     * @param filePath the path to the staff profile CSV file
     * @return the Staff object representing the current operator
     * @throws IllegalArgumentException if {@code filePath} is null or blank
     * @throws RuntimeException         if the file cannot be read or is malformed
     */
    @Override
    public Staff loadCurrentStaff(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path must not be null or blank.");
        }
        try {
            // Read all lines; skip header, parse the first data row as name,role
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            if (lines.size() < 2) {
                throw new IllegalArgumentException("Staff file contains no data rows: " + filePath);
            }
            String[] parts = lines.get(1).split(",", -1);
            String name = parts[0].trim();
            String role = parts.length > 1 ? parts[1].trim() : null;
            return new Staff(name, role);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read staff file: " + filePath, e);
        }
    }

    /**
     * {@inheritDoc}
     * Returns an empty list since staff are not persisted to a repository in the current demo.
     * The single active staff member is loaded on demand via {@link #loadCurrentStaff(String)}.
     */
    @Override
    public List<Staff> listAll() {
        return List.of();
    }

    /**
     * {@inheritDoc}
     * Returns an empty list since staff role queries are not supported in the current demo.
     */
    @Override
    public List<Staff> findByRole(String role) {
        return List.of();
    }
}
