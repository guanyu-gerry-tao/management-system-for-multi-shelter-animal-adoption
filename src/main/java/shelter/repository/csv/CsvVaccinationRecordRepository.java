package shelter.repository.csv;

import shelter.domain.VaccinationRecord;
import shelter.repository.AnimalRepository;
import shelter.repository.VaccinationRecordRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CSV-backed implementation of {@link VaccinationRecordRepository} that persists vaccination
 * history to a flat file named {@code vaccination-records.csv}.
 * Records are loaded into an in-memory {@link LinkedHashMap} at construction time and written back
 * to disk on every {@link #save(VaccinationRecord)} or {@link #delete(String)} call.
 */
public class CsvVaccinationRecordRepository implements VaccinationRecordRepository {

    private static final String FILE_NAME = "vaccination-records.csv";
    private static final String HEADER = "id,animalId,vaccineTypeId,dateAdministered";

    private final Path filePath;
    private final Map<String, VaccinationRecord> store;
    private final AnimalRepository animalRepository;

    /**
     * Constructs a new CsvVaccinationRecordRepository that reads from and writes to the given directory.
     * The {@code animalRepository} is used when resolving shelter membership for
     * {@link #findByShelterId(String)} queries.
     * If the CSV file does not yet exist it is created with just the header row.
     *
     * @param dataDir          the path to the directory containing the CSV file; must not be null or blank
     * @param animalRepository the repository used to look up animals by their shelter ID;
     *                         must not be null
     * @throws IllegalArgumentException if any parameter is null or {@code dataDir} is blank
     * @throws RuntimeException         if the directory cannot be created or the file cannot be read
     */
    public CsvVaccinationRecordRepository(String dataDir, AnimalRepository animalRepository) {
        if (dataDir == null || dataDir.isBlank()) {
            throw new IllegalArgumentException("Data directory must not be null or blank.");
        }
        if (animalRepository == null) {
            throw new IllegalArgumentException("AnimalRepository must not be null.");
        }
        this.filePath = Paths.get(dataDir, FILE_NAME);
        this.store = new LinkedHashMap<>();
        this.animalRepository = animalRepository;
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
            throw new RuntimeException("Failed to initialize CsvVaccinationRecordRepository: " + e.getMessage(), e);
        }
    }

    /** Reads all CSV rows and reconstructs VaccinationRecord objects into the in-memory store. */
    private void loadAll() {
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                try {
                    VaccinationRecord rec = parseLine(line);
                    store.put(rec.getId(), rec);
                } catch (Exception e) {
                    System.err.println("Skipping malformed vaccination-record CSV row " + i + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load vaccination records from CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a single CSV row into a VaccinationRecord using the reconstruction constructor.
     * Fields are expected in order: id, animalId, vaccineTypeId, dateAdministered.
     *
     * @param line a non-empty CSV row
     * @return the reconstructed {@link VaccinationRecord}
     */
    private VaccinationRecord parseLine(String line) {
        String[] parts = CsvUtils.splitCsv(line);
        String id = CsvUtils.unescapeCsv(parts[0]);
        String animalId = CsvUtils.unescapeCsv(parts[1]);
        String vaccineTypeId = CsvUtils.unescapeCsv(parts[2]);
        LocalDate date = LocalDate.parse(parts[3].trim());
        return new VaccinationRecord(id, animalId, vaccineTypeId, date);
    }

    /** Writes the entire in-memory store back to the CSV file. */
    private void flush() {
        try {
            StringBuilder sb = new StringBuilder(HEADER).append(System.lineSeparator());
            for (VaccinationRecord rec : store.values()) {
                sb.append(CsvUtils.escapeCsv(rec.getId())).append(',')
                  .append(CsvUtils.escapeCsv(rec.getAnimalId())).append(',')
                  .append(CsvUtils.escapeCsv(rec.getVaccineTypeId())).append(',')
                  .append(rec.getDateAdministered().toString())
                  .append(System.lineSeparator());
            }
            Files.writeString(filePath, sb.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to flush vaccination records to CSV: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code record} is null
     */
    @Override
    public void save(VaccinationRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("VaccinationRecord must not be null.");
        }
        store.put(record.getId(), record);
        flush();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code id} is null or blank
     */
    @Override
    public Optional<VaccinationRecord> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID must not be null or blank.");
        }
        return Optional.ofNullable(store.get(id));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code animalId} is null or blank
     */
    @Override
    public List<VaccinationRecord> findByAnimalId(String animalId) {
        if (animalId == null || animalId.isBlank()) {
            throw new IllegalArgumentException("Animal ID must not be null or blank.");
        }
        List<VaccinationRecord> result = new ArrayList<>();
        for (VaccinationRecord rec : store.values()) {
            if (animalId.equals(rec.getAnimalId())) {
                result.add(rec);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VaccinationRecord> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    /**
     * Returns all vaccination records for animals currently housed in the given shelter.
     * The lookup is performed by finding all animals with the matching shelter ID via the
     * injected {@link AnimalRepository}, then filtering records by those animal IDs.
     *
     * @param shelterId the shelter ID to filter by; must not be null or blank
     * @return a list of vaccination records for animals in the specified shelter
     * @throws IllegalArgumentException if {@code shelterId} is null or blank
     */
    @Override
    public List<VaccinationRecord> findByShelterId(String shelterId) {
        if (shelterId == null || shelterId.isBlank()) {
            throw new IllegalArgumentException("Shelter ID must not be null or blank.");
        }
        List<String> animalIds = new ArrayList<>();
        animalRepository.findByShelterId(shelterId).forEach(a -> animalIds.add(a.getId()));

        List<VaccinationRecord> result = new ArrayList<>();
        for (VaccinationRecord rec : store.values()) {
            if (animalIds.contains(rec.getAnimalId())) {
                result.add(rec);
            }
        }
        return Collections.unmodifiableList(result);
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
