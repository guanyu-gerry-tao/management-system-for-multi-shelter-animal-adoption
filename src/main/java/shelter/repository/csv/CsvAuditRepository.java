package shelter.repository.csv;

import shelter.domain.Staff;
import shelter.exception.DataPersistenceException;
import shelter.repository.AuditRepository;
import shelter.service.model.AuditEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CSV-backed implementation of {@link AuditRepository} that appends audit log entries
 * to a flat file named {@code audit.csv}. Unlike other CSV repositories, this one never
 * rewrites existing entries — each {@link #append} call adds a single line to the end of
 * the file, preserving the original insertion order of the audit trail.
 * The {@link #findAll()} method reads the file from disk each time to reflect any
 * entries written by other invocations of the same command.
 */
public class CsvAuditRepository implements AuditRepository {

    private static final String FILE_NAME = "audit.csv";
    private static final String HEADER = "id,staffId,staffName,action,targetDescription,timestamp";
    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path filePath;

    /**
     * Constructs a new CsvAuditRepository that appends to a file in the given directory.
     * If the directory or file does not yet exist, both are created with an appropriate header row.
     *
     * @param dataDir the path to the directory containing the CSV file; must not be null or blank
     * @throws IllegalArgumentException if {@code dataDir} is null or blank
     * @throws DataPersistenceException if the directory cannot be created or the file cannot be initialised
     */
    public CsvAuditRepository(String dataDir) {
        if (dataDir == null || dataDir.isBlank()) {
            throw new IllegalArgumentException("Data directory must not be null or blank.");
        }
        this.filePath = Paths.get(dataDir, FILE_NAME);
        initFile();
    }

    /** Creates the directory and file if they do not already exist. */
    private void initFile() {
        try {
            Files.createDirectories(filePath.getParent());
            if (!Files.exists(filePath)) {
                Files.writeString(filePath, HEADER + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new DataPersistenceException(
                    "Failed to initialise CsvAuditRepository: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * Appends a single CSV row to the end of the audit file without rewriting previous entries.
     *
     * @throws IllegalArgumentException if any string argument is null or blank, or timestamp is null
     * @throws DataPersistenceException if the row cannot be written to disk
     */
    @Override
    public void append(String staffId, String staffName, String action,
                       String targetDescription, LocalDateTime timestamp) {
        // Guard: all fields are required for a meaningful audit entry
        if (staffId == null || staffId.isBlank()) {
            throw new IllegalArgumentException("Staff ID must not be null or blank.");
        }
        if (staffName == null || staffName.isBlank()) {
            throw new IllegalArgumentException("Staff name must not be null or blank.");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action must not be null or blank.");
        }
        if (targetDescription == null || targetDescription.isBlank()) {
            throw new IllegalArgumentException("Target description must not be null or blank.");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp must not be null.");
        }

        // Build and append the CSV row; generate a unique row ID for traceability
        String id = java.util.UUID.randomUUID().toString();
        String row = CsvUtils.escapeCsv(id) + ','
                + CsvUtils.escapeCsv(staffId) + ','
                + CsvUtils.escapeCsv(staffName) + ','
                + CsvUtils.escapeCsv(action) + ','
                + CsvUtils.escapeCsv(targetDescription) + ','
                + timestamp.format(TIMESTAMP_FMT)
                + System.lineSeparator();
        try {
            Files.writeString(filePath, row, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new DataPersistenceException(
                    "Failed to append audit entry to CSV: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * Reads all rows from the audit file on each call to ensure entries from previous
     * command invocations are included in the returned list.
     *
     * @throws DataPersistenceException if the file cannot be read
     */
    @Override
    public List<AuditEntry<String>> findAll() {
        try {
            List<String> lines = Files.readAllLines(filePath);
            List<AuditEntry<String>> result = new ArrayList<>();
            // Skip header row; parse each subsequent non-blank line
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                try {
                    result.add(parseLine(line));
                } catch (Exception e) {
                    System.err.println("Skipping malformed audit CSV row " + i + ": " + e.getMessage());
                }
            }
            return Collections.unmodifiableList(result);
        } catch (IOException e) {
            throw new DataPersistenceException(
                    "Failed to read audit entries from CSV: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * Overwrites the file with just the header row, effectively clearing all entries.
     *
     * @throws DataPersistenceException if the file cannot be written
     */
    @Override
    public void clear() {
        try {
            Files.writeString(filePath, HEADER + System.lineSeparator());
        } catch (IOException e) {
            throw new DataPersistenceException(
                    "Failed to clear audit log: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a single CSV row into an {@link AuditEntry} with a String-typed target.
     * Fields are expected in order: id, staffId, staffName, action, targetDescription, timestamp.
     * The staffId stored in the row is used only for traceability; the reconstructed Staff object
     * carries the staffName so it can be displayed without looking up the staff repository.
     *
     * @param line a non-empty CSV row
     * @return the reconstructed audit entry
     */
    private AuditEntry<String> parseLine(String line) {
        String[] parts = CsvUtils.splitCsv(line);
        String staffName = CsvUtils.unescapeCsv(parts[2]);
        String action = CsvUtils.unescapeCsv(parts[3]);
        String targetDescription = CsvUtils.unescapeCsv(parts[4]);
        LocalDateTime timestamp = LocalDateTime.parse(parts[5].trim(), TIMESTAMP_FMT);

        // Reconstruct a minimal Staff object for display; full Staff lookup is not required here
        Staff staff = new Staff(staffName);
        return new AuditEntry<>(staff, action, targetDescription, timestamp);
    }
}
