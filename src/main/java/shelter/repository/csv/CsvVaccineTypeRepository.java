package shelter.repository.csv;

import shelter.domain.Species;
import shelter.domain.VaccineType;
import shelter.repository.VaccineTypeRepository;

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
 * CSV-backed implementation of {@link VaccineTypeRepository} that persists vaccine type catalog
 * entries to a flat file named {@code vaccine-types.csv}.
 * Records are loaded into an in-memory {@link LinkedHashMap} at construction time and written back
 * to disk on every {@link #save(VaccineType)} or {@link #delete(String)} call.
 */
public class CsvVaccineTypeRepository implements VaccineTypeRepository {

    private static final String FILE_NAME = "vaccine-types.csv";
    private static final String HEADER = "id,name,applicableSpecies,validityDays";

    private final Path filePath;
    private final Map<String, VaccineType> store;

    /**
     * Constructs a new CsvVaccineTypeRepository that reads from and writes to the given directory.
     * If the CSV file does not yet exist it is created with just the header row; existing data
     * is loaded into memory immediately upon construction.
     *
     * @param dataDir the path to the directory that contains (or will contain) the CSV file;
     *                must not be null or blank
     * @throws IllegalArgumentException if {@code dataDir} is null or blank
     * @throws RuntimeException         if the directory cannot be created or the file cannot be read
     */
    public CsvVaccineTypeRepository(String dataDir) {
        if (dataDir == null || dataDir.isBlank()) {
            throw new IllegalArgumentException("Data directory must not be null or blank.");
        }
        this.filePath = Paths.get(dataDir, FILE_NAME);
        this.store = new LinkedHashMap<>();
        initAndLoad();
    }

    /** Ensures the data directory and file exist, then loads all rows into memory. */
    private void initAndLoad() {
        try {
            Files.createDirectories(filePath.getParent());
            if (!Files.exists(filePath)) {
                Files.writeString(filePath, HEADER + System.lineSeparator());
            } else {
                loadAll();
            }
        } catch (IOException e) {
            throw new shelter.exception.DataPersistenceException("Failed to initialize CsvVaccineTypeRepository: " + e.getMessage(), e);
        }
    }

    /** Reads all CSV rows and reconstructs VaccineType objects into the in-memory store. */
    private void loadAll() {
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                try {
                    VaccineType vt = parseLine(line);
                    store.put(vt.getId(), vt);
                } catch (Exception e) {
                    System.err.println("Skipping malformed vaccine-type CSV row " + i + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new shelter.exception.DataPersistenceException("Failed to load vaccine types from CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a single CSV row into a VaccineType using the reconstruction constructor.
     * Fields are expected in the order: id, name, applicableSpecies, validityDays.
     *
     * @param line a non-empty CSV row
     * @return the reconstructed {@link VaccineType}
     */
    private VaccineType parseLine(String line) {
        String[] parts = CsvUtils.splitCsv(line);
        String id = CsvUtils.unescapeCsv(parts[0]);
        String name = CsvUtils.unescapeCsv(parts[1]);
        Species species = Species.valueOf(parts[2].trim());
        int validityDays = Integer.parseInt(parts[3].trim());
        return new VaccineType(id, name, species, validityDays);
    }

    /** Writes the entire in-memory store back to the CSV file. */
    private void flush() {
        try {
            StringBuilder sb = new StringBuilder(HEADER).append(System.lineSeparator());
            for (VaccineType vt : store.values()) {
                sb.append(CsvUtils.escapeCsv(vt.getId())).append(',')
                  .append(CsvUtils.escapeCsv(vt.getName())).append(',')
                  .append(vt.getApplicableSpecies().name()).append(',')
                  .append(vt.getValidityDays())
                  .append(System.lineSeparator());
            }
            Files.writeString(filePath, sb.toString());
        } catch (IOException e) {
            throw new shelter.exception.DataPersistenceException("Failed to flush vaccine types to CSV: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code vaccineType} is null
     */
    @Override
    public void save(VaccineType vaccineType) {
        if (vaccineType == null) {
            throw new IllegalArgumentException("VaccineType must not be null.");
        }
        store.put(vaccineType.getId(), vaccineType);
        flush();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code id} is null or blank
     */
    @Override
    public Optional<VaccineType> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID must not be null or blank.");
        }
        return Optional.ofNullable(store.get(id));
    }

    /**
     * {@inheritDoc}
     * Name matching is case-insensitive as required by the interface contract.
     *
     * @throws IllegalArgumentException if {@code name} is null or blank
     */
    @Override
    public Optional<VaccineType> findByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be null or blank.");
        }
        return store.values().stream()
                .filter(vt -> vt.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code species} is null
     */
    @Override
    public List<VaccineType> findByApplicableSpecies(Species species) {
        if (species == null) {
            throw new IllegalArgumentException("Species must not be null.");
        }
        List<VaccineType> result = new ArrayList<>();
        for (VaccineType vt : store.values()) {
            if (vt.getApplicableSpecies() == species) {
                result.add(vt);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VaccineType> findAll() {
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
