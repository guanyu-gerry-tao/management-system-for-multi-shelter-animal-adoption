package shelter.repository.csv;

import shelter.domain.ActivityLevel;
import shelter.domain.Animal;
import shelter.domain.Cat;
import shelter.domain.Dog;
import shelter.domain.Other;
import shelter.domain.Rabbit;
import shelter.domain.Species;
import shelter.repository.AnimalRepository;

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
 * CSV-backed implementation of {@link AnimalRepository} that persists animal records to a flat file
 * named {@code animals.csv}.
 * All concrete animal types ({@link Dog}, {@link Cat}, {@link Rabbit}, and {@link Other}) are stored
 * in the same file using a species discriminator column: standard species write their enum name
 * (e.g. {@code DOG}), while {@link Other} animals write their free-form {@code speciesName} so the
 * correct type can be reconstructed on reload. Species-specific columns are left empty for
 * non-applicable types. Records are loaded into memory at construction and flushed on every mutation.
 */
public class CsvAnimalRepository implements AnimalRepository {

    private static final String FILE_NAME = "animals.csv";
    /**
     * Column layout: id, species, name, breed, birthday, activityLevel, vaccinated,
     * adopterId, shelterId, size(dog only), neutered(dog/cat), indoor(cat only), furLength(rabbit only)
     */
    private static final String HEADER =
            "id,species,name,breed,birthday,activityLevel,vaccinated,adopterId,shelterId,size,neutered,indoor,furLength";

    private final Path filePath;
    private final Map<String, Animal> store;

    /**
     * Constructs a new CsvAnimalRepository that reads from and writes to the given data directory.
     * If the CSV file does not yet exist it is created with just the header row; existing data
     * is loaded into memory immediately upon construction.
     *
     * @param dataDir the path to the directory that contains (or will contain) the CSV file;
     *                must not be null or blank
     * @throws IllegalArgumentException if {@code dataDir} is null or blank
     * @throws RuntimeException         if the directory cannot be created or the file cannot be read
     */
    public CsvAnimalRepository(String dataDir) {
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
            throw new shelter.exception.DataPersistenceException("Failed to initialize CsvAnimalRepository: " + e.getMessage(), e);
        }
    }

    /** Reads all CSV rows and reconstructs Animal objects into the in-memory store. */
    private void loadAll() {
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                try {
                    Animal a = parseLine(line);
                    store.put(a.getId(), a);
                } catch (Exception e) {
                    System.err.println("Skipping malformed animal CSV row " + i + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new shelter.exception.DataPersistenceException("Failed to load animals from CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a single CSV row into the appropriate Animal subclass based on the species column.
     * Fields are in order: id, species, name, breed, birthday, activityLevel, vaccinated,
     * adopterId, shelterId, size, neutered, indoor, furLength.
     * Species-specific columns that do not apply are stored as empty strings.
     *
     * @param line a non-empty CSV row
     * @return the reconstructed {@link Animal} (a {@link Dog}, {@link Cat}, {@link Rabbit}, or {@link Other})
     */
    private Animal parseLine(String line) {
        String[] p = CsvUtils.splitCsv(line);
        String id            = CsvUtils.unescapeCsv(p[0]);
        String name          = CsvUtils.unescapeCsv(p[2]);
        String breed         = CsvUtils.unescapeCsv(p[3]);
        LocalDate birthday   = LocalDate.parse(p[4].trim());
        ActivityLevel act    = ActivityLevel.valueOf(p[5].trim());
        boolean vaccinated   = Boolean.parseBoolean(p[6].trim());
        String adopterId     = CsvUtils.unescapeCsv(p[7]);  // null if empty
        String shelterId     = CsvUtils.unescapeCsv(p[8]);  // null if empty

        // columns 9–12 are species-specific
        String sizeRaw      = p.length > 9  ? p[9].trim()  : "";
        String neuteredRaw  = p.length > 10 ? p[10].trim() : "";
        String indoorRaw    = p.length > 11 ? p[11].trim() : "";
        String furRaw       = p.length > 12 ? p[12].trim() : "";

        String speciesRaw = CsvUtils.unescapeCsv(p[1]);
        if (speciesRaw.equals("DOG")) {
            Dog.Size size = sizeRaw.isEmpty() ? Dog.Size.MEDIUM : Dog.Size.valueOf(sizeRaw);
            boolean neutered = !neuteredRaw.isEmpty() && Boolean.parseBoolean(neuteredRaw);
            return new Dog(id, name, breed, birthday, act, vaccinated, adopterId, shelterId, size, neutered);
        } else if (speciesRaw.equals("CAT")) {
            boolean indoor   = !indoorRaw.isEmpty() && Boolean.parseBoolean(indoorRaw);
            boolean neutered = !neuteredRaw.isEmpty() && Boolean.parseBoolean(neuteredRaw);
            return new Cat(id, name, breed, birthday, act, vaccinated, adopterId, shelterId, indoor, neutered);
        } else if (speciesRaw.equals("RABBIT")) {
            Rabbit.FurLength fur = furRaw.isEmpty() ? Rabbit.FurLength.SHORT : Rabbit.FurLength.valueOf(furRaw);
            return new Rabbit(id, name, breed, birthday, act, vaccinated, adopterId, shelterId, fur);
        } else {
            // Any unrecognised species string is treated as Other; speciesRaw is the free-form name
            return new Other(id, name, breed, birthday, act, vaccinated, adopterId, shelterId, speciesRaw);
        }
    }

    /** Serialises a single Animal to the 13-column CSV row format. */
    private String toRow(Animal a) {
        String sizeVal     = "";
        String neuteredVal = "";
        String indoorVal   = "";
        String furVal      = "";

        if (a instanceof Dog) {
            Dog d = (Dog) a;
            sizeVal     = d.getSize().name();
            neuteredVal = String.valueOf(d.isNeutered());
        } else if (a instanceof Cat) {
            Cat c = (Cat) a;
            neuteredVal = String.valueOf(c.isNeutered());
            indoorVal   = String.valueOf(c.isIndoor());
        } else if (a instanceof Rabbit) {
            Rabbit r = (Rabbit) a;
            furVal = r.getFurLength().name();
        }

        // For Other animals, write the free-form speciesName into col 1 instead of "OTHER",
        // so the reader can reconstruct the correct speciesName on reload.
        String speciesCol = (a instanceof Other)
                ? CsvUtils.escapeCsv(((Other) a).getSpeciesName())
                : a.getSpecies().name();

        return CsvUtils.escapeCsv(a.getId()) + ','
             + speciesCol + ','
             + CsvUtils.escapeCsv(a.getName()) + ','
             + CsvUtils.escapeCsv(a.getBreed()) + ','
             + a.getBirthday().toString() + ','
             + a.getActivityLevel().name() + ','
             + a.isVaccinated() + ','
             + CsvUtils.escapeCsv(a.getAdopterId()) + ','
             + CsvUtils.escapeCsv(a.getShelterId()) + ','
             + sizeVal + ','
             + neuteredVal + ','
             + indoorVal + ','
             + furVal;
    }

    /** Writes the entire in-memory store back to the CSV file. */
    private void flush() {
        try {
            StringBuilder sb = new StringBuilder(HEADER).append(System.lineSeparator());
            for (Animal a : store.values()) {
                sb.append(toRow(a)).append(System.lineSeparator());
            }
            Files.writeString(filePath, sb.toString());
        } catch (IOException e) {
            throw new shelter.exception.DataPersistenceException("Failed to flush animals to CSV: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code animal} is null
     */
    @Override
    public void save(Animal animal) {
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }
        store.put(animal.getId(), animal);
        flush();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code id} is null or blank
     */
    @Override
    public Optional<Animal> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID must not be null or blank.");
        }
        return Optional.ofNullable(store.get(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Animal> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code shelterId} is null or blank
     */
    @Override
    public List<Animal> findByShelterId(String shelterId) {
        if (shelterId == null || shelterId.isBlank()) {
            throw new IllegalArgumentException("Shelter ID must not be null or blank.");
        }
        List<Animal> result = new ArrayList<>();
        for (Animal a : store.values()) {
            if (shelterId.equals(a.getShelterId())) {
                result.add(a);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code adopterId} is null or blank
     */
    @Override
    public List<Animal> findByAdopterId(String adopterId) {
        if (adopterId == null || adopterId.isBlank()) {
            throw new IllegalArgumentException("Adopter ID must not be null or blank.");
        }
        List<Animal> result = new ArrayList<>();
        for (Animal a : store.values()) {
            if (adopterId.equals(a.getAdopterId())) {
                result.add(a);
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
