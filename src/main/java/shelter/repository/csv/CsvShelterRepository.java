package shelter.repository.csv;

import shelter.domain.Shelter;
import shelter.repository.ShelterRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CSV-backed implementation of {@link ShelterRepository} that persists shelter records to a flat file.
 * Records are loaded into an in-memory {@link LinkedHashMap} at construction time and written back to
 * disk on every {@link #save(Shelter)} or {@link #delete(String)} call to ensure durability.
 */
public class CsvShelterRepository implements ShelterRepository {

    private static final String FILE_NAME = "shelters.csv";
    private static final String HEADER = "id,name,location,capacity";

    private final Path filePath;
    private final Map<String, Shelter> store;

    /**
     * Constructs a new CsvShelterRepository that reads from and writes to the given data directory.
     * If the CSV file does not yet exist it is created with just the header row; existing data is
     * loaded into memory immediately.
     *
     * @param dataDir the path to the directory that contains (or will contain) the CSV file;
     *                must not be null or blank
     * @throws IllegalArgumentException if {@code dataDir} is null or blank
     * @throws RuntimeException         if the data directory cannot be created or the file cannot be read
     */
    public CsvShelterRepository(String dataDir) {
        if (dataDir == null || dataDir.isBlank()) {
            throw new IllegalArgumentException("Data directory must not be null or blank.");
        }
        this.filePath = Paths.get(dataDir, FILE_NAME);
        this.store = new LinkedHashMap<>();
        initAndLoad();
    }

    /**
     * Initializes the data directory and loads existing records into memory.
     * Creates the CSV file with a header if it does not already exist.
     */
    private void initAndLoad() {
        try {
            Files.createDirectories(filePath.getParent());
            if (!Files.exists(filePath)) {
                Files.writeString(filePath, HEADER + System.lineSeparator());
            } else {
                loadAll();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize CsvShelterRepository: " + e.getMessage(), e);
        }
    }

    /**
     * Reads all CSV rows and reconstructs Shelter objects into the in-memory store.
     * Rows that cannot be parsed are skipped with a warning printed to stderr.
     */
    private void loadAll() {
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue;
                }
                try {
                    Shelter s = parseLine(line);
                    store.put(s.getId(), s);
                } catch (Exception e) {
                    System.err.println("Skipping malformed shelter CSV row " + i + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load shelters from CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a single CSV row into a Shelter object using the reconstruction constructor.
     * Fields are expected in the order: id, name, location, capacity.
     *
     * @param line a non-empty CSV row
     * @return the reconstructed {@link Shelter}
     */
    private Shelter parseLine(String line) {
        String[] parts = CsvUtils.splitCsv(line);
        String id = CsvUtils.unescapeCsv(parts[0]);
        String name = CsvUtils.unescapeCsv(parts[1]);
        String location = CsvUtils.unescapeCsv(parts[2]);
        int capacity = Integer.parseInt(parts[3].trim());
        return new Shelter(id, name, location, capacity);
    }

    /**
     * Flushes the entire in-memory store back to the CSV file on disk.
     * Called after every mutating operation to keep the file consistent with memory.
     */
    private void flush() {
        try {
            StringBuilder sb = new StringBuilder(HEADER).append(System.lineSeparator());
            for (Shelter s : store.values()) {
                sb.append(CsvUtils.escapeCsv(s.getId())).append(',')
                  .append(CsvUtils.escapeCsv(s.getName())).append(',')
                  .append(CsvUtils.escapeCsv(s.getLocation())).append(',')
                  .append(s.getCapacity())
                  .append(System.lineSeparator());
            }
            Files.writeString(filePath, sb.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to flush shelters to CSV: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code shelter} is null
     */
    @Override
    public void save(Shelter shelter) {
        if (shelter == null) {
            throw new IllegalArgumentException("Shelter must not be null.");
        }
        store.put(shelter.getId(), shelter);
        flush();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code id} is null or blank
     */
    @Override
    public Optional<Shelter> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID must not be null or blank.");
        }
        return Optional.ofNullable(store.get(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Shelter> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code id} is null or blank
     */
    @Override
    public void delete(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID must not be null or blank.");
        }
        store.remove(id);
        flush();
    }
}
