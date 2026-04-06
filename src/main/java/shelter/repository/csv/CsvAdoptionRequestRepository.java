package shelter.repository.csv;

import shelter.domain.Adopter;
import shelter.domain.AdoptionRequest;
import shelter.domain.Animal;
import shelter.domain.RequestStatus;
import shelter.repository.AdopterRepository;
import shelter.repository.AdoptionRequestRepository;
import shelter.repository.AnimalRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CSV-backed implementation of {@link AdoptionRequestRepository} that persists adoption request
 * records to a flat file named {@code adoption-requests.csv}.
 * The repository requires injected {@link AnimalRepository} and {@link AdopterRepository} instances
 * to reconstruct full object graphs when loading from CSV.
 * Records are kept in memory and flushed to disk on every mutation.
 */
public class CsvAdoptionRequestRepository implements AdoptionRequestRepository {

    private static final String FILE_NAME = "adoption-requests.csv";
    private static final String HEADER = "id,adopterId,animalId,status,submittedAt";

    private final Path filePath;
    private final Map<String, AdoptionRequest> store;
    private final AnimalRepository animalRepository;
    private final AdopterRepository adopterRepository;

    /**
     * Constructs a new CsvAdoptionRequestRepository backed by the given data directory.
     * The {@code animalRepository} and {@code adopterRepository} are used to resolve full objects
     * when loading adoption requests from CSV rows.
     * If the CSV file does not yet exist it is created with just the header row.
     *
     * @param dataDir           the path to the directory for the CSV file; must not be null or blank
     * @param animalRepository  used to look up animals by ID during CSV deserialization; must not be null
     * @param adopterRepository used to look up adopters by ID during CSV deserialization; must not be null
     * @throws IllegalArgumentException if any parameter is null or {@code dataDir} is blank
     * @throws RuntimeException         if the directory cannot be created or the file cannot be read
     */
    public CsvAdoptionRequestRepository(String dataDir,
                                        AnimalRepository animalRepository,
                                        AdopterRepository adopterRepository) {
        if (dataDir == null || dataDir.isBlank()) {
            throw new IllegalArgumentException("Data directory must not be null or blank.");
        }
        if (animalRepository == null) {
            throw new IllegalArgumentException("AnimalRepository must not be null.");
        }
        if (adopterRepository == null) {
            throw new IllegalArgumentException("AdopterRepository must not be null.");
        }
        this.filePath = Paths.get(dataDir, FILE_NAME);
        this.store = new LinkedHashMap<>();
        this.animalRepository = animalRepository;
        this.adopterRepository = adopterRepository;
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
            throw new RuntimeException("Failed to initialize CsvAdoptionRequestRepository: " + e.getMessage(), e);
        }
    }

    /**
     * Reads all CSV rows and reconstructs AdoptionRequest objects into the in-memory store.
     * Rows whose referenced animal or adopter cannot be found are skipped with a warning.
     */
    private void loadAll() {
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                try {
                    AdoptionRequest req = parseLine(line);
                    store.put(req.getId(), req);
                } catch (Exception e) {
                    System.err.println("Skipping malformed adoption-request CSV row " + i + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load adoption requests from CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a single CSV row and reconstructs a full {@link AdoptionRequest}.
     * Fields are in order: id, adopterId, animalId, status, submittedAt.
     *
     * @param line a non-empty CSV row
     * @return the reconstructed {@link AdoptionRequest}
     * @throws IllegalStateException if the referenced adopter or animal is not found
     */
    private AdoptionRequest parseLine(String line) {
        String[] parts = CsvUtils.splitCsv(line);
        String id          = CsvUtils.unescapeCsv(parts[0]);
        String adopterId   = CsvUtils.unescapeCsv(parts[1]);
        String animalId    = CsvUtils.unescapeCsv(parts[2]);
        RequestStatus status = RequestStatus.valueOf(parts[3].trim());
        LocalDateTime submittedAt = LocalDateTime.parse(parts[4].trim());

        Adopter adopter = adopterRepository.findById(adopterId)
                .orElseThrow(() -> new IllegalStateException(
                        "Adopter not found for adoption request: " + adopterId));
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new IllegalStateException(
                        "Animal not found for adoption request: " + animalId));

        return new AdoptionRequest(id, adopter, animal, status, submittedAt);
    }

    /** Writes the entire in-memory store back to the CSV file. */
    private void flush() {
        try {
            StringBuilder sb = new StringBuilder(HEADER).append(System.lineSeparator());
            for (AdoptionRequest req : store.values()) {
                sb.append(CsvUtils.escapeCsv(req.getId())).append(',')
                  .append(CsvUtils.escapeCsv(req.getAdopter().getId())).append(',')
                  .append(CsvUtils.escapeCsv(req.getAnimal().getId())).append(',')
                  .append(req.getStatus().name()).append(',')
                  .append(req.getSubmittedAt().toString())
                  .append(System.lineSeparator());
            }
            Files.writeString(filePath, sb.toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to flush adoption requests to CSV: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code request} is null
     */
    @Override
    public void save(AdoptionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AdoptionRequest must not be null.");
        }
        store.put(request.getId(), request);
        flush();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code id} is null or blank
     */
    @Override
    public Optional<AdoptionRequest> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID must not be null or blank.");
        }
        return Optional.ofNullable(store.get(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AdoptionRequest> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code adopterId} is null or blank
     */
    @Override
    public List<AdoptionRequest> findByAdopterId(String adopterId) {
        if (adopterId == null || adopterId.isBlank()) {
            throw new IllegalArgumentException("Adopter ID must not be null or blank.");
        }
        List<AdoptionRequest> result = new ArrayList<>();
        for (AdoptionRequest req : store.values()) {
            if (adopterId.equals(req.getAdopter().getId())) {
                result.add(req);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code animalId} is null or blank
     */
    @Override
    public List<AdoptionRequest> findByAnimalId(String animalId) {
        if (animalId == null || animalId.isBlank()) {
            throw new IllegalArgumentException("Animal ID must not be null or blank.");
        }
        List<AdoptionRequest> result = new ArrayList<>();
        for (AdoptionRequest req : store.values()) {
            if (animalId.equals(req.getAnimal().getId())) {
                result.add(req);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * {@inheritDoc}
     * Shelter membership is determined by comparing the given {@code shelterId} to
     * each request's {@code animal.getShelterId()}.
     *
     * @throws IllegalArgumentException if {@code shelterId} is null or blank
     */
    @Override
    public List<AdoptionRequest> findByShelterId(String shelterId) {
        if (shelterId == null || shelterId.isBlank()) {
            throw new IllegalArgumentException("Shelter ID must not be null or blank.");
        }
        List<AdoptionRequest> result = new ArrayList<>();
        for (AdoptionRequest req : store.values()) {
            if (shelterId.equals(req.getAnimal().getShelterId())) {
                result.add(req);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code status} is null
     */
    @Override
    public List<AdoptionRequest> findByStatus(RequestStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null.");
        }
        List<AdoptionRequest> result = new ArrayList<>();
        for (AdoptionRequest req : store.values()) {
            if (req.getStatus() == status) {
                result.add(req);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code adopterId} is null or blank, or {@code status} is null
     */
    @Override
    public List<AdoptionRequest> findByAdopterIdAndStatus(String adopterId, RequestStatus status) {
        if (adopterId == null || adopterId.isBlank()) {
            throw new IllegalArgumentException("Adopter ID must not be null or blank.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null.");
        }
        List<AdoptionRequest> result = new ArrayList<>();
        for (AdoptionRequest req : store.values()) {
            if (adopterId.equals(req.getAdopter().getId()) && req.getStatus() == status) {
                result.add(req);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code shelterId} is null or blank, or {@code status} is null
     */
    @Override
    public List<AdoptionRequest> findByShelterIdAndStatus(String shelterId, RequestStatus status) {
        if (shelterId == null || shelterId.isBlank()) {
            throw new IllegalArgumentException("Shelter ID must not be null or blank.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null.");
        }
        List<AdoptionRequest> result = new ArrayList<>();
        for (AdoptionRequest req : store.values()) {
            if (shelterId.equals(req.getAnimal().getShelterId()) && req.getStatus() == status) {
                result.add(req);
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
