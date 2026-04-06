package shelter.repository.csv;

import shelter.domain.ActivityLevel;
import shelter.domain.Adopter;
import shelter.domain.AdopterPreferences;
import shelter.domain.DailySchedule;
import shelter.domain.LivingSpace;
import shelter.domain.Species;
import shelter.repository.AdopterRepository;

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
 * CSV-backed implementation of {@link AdopterRepository} that persists adopter records to a flat
 * file named {@code adopters.csv}.
 * Records are loaded into an in-memory {@link LinkedHashMap} at construction time and written back
 * to disk on every {@link #save(Adopter)} or {@link #delete(String)} call.
 */
public class CsvAdopterRepository implements AdopterRepository {

    private static final String FILE_NAME = "adopters.csv";
    private static final String HEADER =
            "id,name,livingSpace,dailySchedule,personalNotes,"
            + "preferredSpecies,preferredBreed,preferredActivityLevel,"
            + "minAge,maxAge,adoptedAnimalIds";

    private final Path filePath;
    private final Map<String, Adopter> store;

    /**
     * Constructs a new CsvAdopterRepository that reads from and writes to the given data directory.
     * If the CSV file does not yet exist it is created with just the header row; existing data
     * is loaded into memory immediately upon construction.
     *
     * @param dataDir the path to the directory that contains (or will contain) the CSV file;
     *                must not be null or blank
     * @throws IllegalArgumentException if {@code dataDir} is null or blank
     * @throws RuntimeException         if the directory cannot be created or the file cannot be read
     */
    public CsvAdopterRepository(String dataDir) {
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
            throw new shelter.exception.DataPersistenceException("Failed to initialize CsvAdopterRepository: " + e.getMessage(), e);
        }
    }

    /** Reads all CSV rows and reconstructs Adopter objects into the in-memory store. */
    private void loadAll() {
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                try {
                    Adopter a = parseLine(line);
                    store.put(a.getId(), a);
                } catch (Exception e) {
                    System.err.println("Skipping malformed adopter CSV row " + i + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new shelter.exception.DataPersistenceException("Failed to load adopters from CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a single CSV row into an Adopter using the reconstruction constructor.
     * Fields are in order: id, name, livingSpace, dailySchedule, personalNotes,
     * preferredSpecies, preferredBreed, preferredActivityLevel, minAge, maxAge, adoptedAnimalIds.
     *
     * @param line a non-empty CSV row
     * @return the reconstructed {@link Adopter}
     */
    private Adopter parseLine(String line) {
        String[] parts = CsvUtils.splitCsv(line);
        String id              = CsvUtils.unescapeCsv(parts[0]);
        String name            = CsvUtils.unescapeCsv(parts[1]);
        LivingSpace living     = LivingSpace.valueOf(parts[2].trim());
        DailySchedule schedule = DailySchedule.valueOf(parts[3].trim());
        String notes           = CsvUtils.unescapeCsv(parts[4]);

        String speciesRaw      = parts[5].trim();
        Species prefSpecies    = speciesRaw.isEmpty() ? null : Species.valueOf(speciesRaw);
        String prefBreed       = CsvUtils.unescapeCsv(parts[6]);

        String actRaw          = parts[7].trim();
        ActivityLevel prefAct  = actRaw.isEmpty() ? null : ActivityLevel.valueOf(actRaw);

        int minAge             = Integer.parseInt(parts[8].trim());
        int maxAge             = Integer.parseInt(parts[9].trim());

        List<String> adoptedIds = CsvUtils.decodeList(CsvUtils.unescapeCsv(parts[10]));

        AdopterPreferences prefs = new AdopterPreferences(prefSpecies, prefBreed, prefAct, minAge, maxAge);
        return new Adopter(id, name, living, schedule, notes, prefs, adoptedIds);
    }

    /** Writes the entire in-memory store back to the CSV file. */
    private void flush() {
        try {
            StringBuilder sb = new StringBuilder(HEADER).append(System.lineSeparator());
            for (Adopter a : store.values()) {
                AdopterPreferences p = a.getPreferences();
                sb.append(CsvUtils.escapeCsv(a.getId())).append(',')
                  .append(CsvUtils.escapeCsv(a.getName())).append(',')
                  .append(a.getLivingSpace().name()).append(',')
                  .append(a.getDailySchedule().name()).append(',')
                  .append(CsvUtils.escapeCsv(a.getPersonalNotes())).append(',')
                  .append(p.getPreferredSpecies() != null ? p.getPreferredSpecies().name() : "").append(',')
                  .append(CsvUtils.escapeCsv(p.getPreferredBreed())).append(',')
                  .append(p.getPreferredActivityLevel() != null ? p.getPreferredActivityLevel().name() : "").append(',')
                  .append(p.getMinAge()).append(',')
                  .append(p.getMaxAge()).append(',')
                  .append(CsvUtils.escapeCsv(CsvUtils.encodeList(a.getAdoptedAnimalIds())))
                  .append(System.lineSeparator());
            }
            Files.writeString(filePath, sb.toString());
        } catch (IOException e) {
            throw new shelter.exception.DataPersistenceException("Failed to flush adopters to CSV: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code adopter} is null
     */
    @Override
    public void save(Adopter adopter) {
        if (adopter == null) {
            throw new IllegalArgumentException("Adopter must not be null.");
        }
        store.put(adopter.getId(), adopter);
        flush();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code id} is null or blank
     */
    @Override
    public Optional<Adopter> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID must not be null or blank.");
        }
        return Optional.ofNullable(store.get(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Adopter> findAll() {
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
