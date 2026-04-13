package shelter.repository.csv;

import shelter.domain.Animal;
import shelter.domain.RequestStatus;
import shelter.domain.Shelter;
import shelter.domain.TransferRequest;
import shelter.repository.AnimalRepository;
import shelter.repository.ShelterRepository;
import shelter.repository.TransferRequestRepository;

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
 * CSV-backed implementation of {@link TransferRequestRepository} that persists transfer request
 * records to a flat file named {@code transfer-requests.csv}.
 * Requires injected {@link AnimalRepository} and {@link ShelterRepository} instances to reconstruct
 * full object graphs when loading from CSV.
 * Records are kept in memory and flushed to disk on every mutation.
 */
public class CsvTransferRequestRepository implements TransferRequestRepository {

    private static final String FILE_NAME = "transfer-requests.csv";
    private static final String HEADER = "id,animalId,fromShelterId,toShelterId,status,requestedAt";

    private final Path filePath;
    private final Map<String, TransferRequest> store;
    private final AnimalRepository animalRepository;
    private final ShelterRepository shelterRepository;

    /**
     * Constructs a new CsvTransferRequestRepository backed by the given data directory.
     * The {@code animalRepository} and {@code shelterRepository} are used to resolve full objects
     * when loading transfer requests from CSV rows.
     * If the CSV file does not yet exist it is created with just the header row.
     *
     * @param dataDir           the path to the directory for the CSV file; must not be null or blank
     * @param animalRepository  used to look up animals by ID during CSV deserialization; must not be null
     * @param shelterRepository used to look up shelters by ID during CSV deserialization; must not be null
     * @throws IllegalArgumentException if any parameter is null or {@code dataDir} is blank
     * @throws RuntimeException         if the directory cannot be created or the file cannot be read
     */
    public CsvTransferRequestRepository(String dataDir,
                                        AnimalRepository animalRepository,
                                        ShelterRepository shelterRepository) {
        if (dataDir == null || dataDir.isBlank()) {
            throw new IllegalArgumentException("Data directory must not be null or blank.");
        }
        if (animalRepository == null) {
            throw new IllegalArgumentException("AnimalRepository must not be null.");
        }
        if (shelterRepository == null) {
            throw new IllegalArgumentException("ShelterRepository must not be null.");
        }
        this.filePath = Paths.get(dataDir, FILE_NAME);
        this.store = new LinkedHashMap<>();
        this.animalRepository = animalRepository;
        this.shelterRepository = shelterRepository;
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
            throw new shelter.exception.DataPersistenceException("Failed to initialize CsvTransferRequestRepository: " + e.getMessage(), e);
        }
    }

    /**
     * Reads all CSV rows and reconstructs TransferRequest objects into the in-memory store.
     * Rows whose referenced animal or shelters cannot be found are skipped with a warning.
     */
    private void loadAll() {
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                try {
                    TransferRequest req = parseLine(line);
                    store.put(req.getId(), req);
                } catch (Exception e) {
                    System.err.println("Skipping malformed transfer-request CSV row " + i + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new shelter.exception.DataPersistenceException("Failed to load transfer requests from CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a single CSV row and reconstructs a full {@link TransferRequest}.
     * Fields are in order: id, animalId, fromShelterId, toShelterId, status, requestedAt.
     *
     * @param line a non-empty CSV row
     * @return the reconstructed {@link TransferRequest}
     * @throws IllegalStateException if the referenced animal or shelters are not found
     */
    private TransferRequest parseLine(String line) {
        String[] parts = CsvUtils.splitCsv(line);
        String id             = CsvUtils.unescapeCsv(parts[0]);
        String animalId       = CsvUtils.unescapeCsv(parts[1]);
        String fromShelterId  = CsvUtils.unescapeCsv(parts[2]);
        String toShelterId    = CsvUtils.unescapeCsv(parts[3]);
        RequestStatus status  = RequestStatus.valueOf(parts[4].trim());
        LocalDateTime requestedAt = LocalDateTime.parse(parts[5].trim());

        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new IllegalStateException(
                        "Animal not found for transfer request: " + animalId));
        Shelter from = shelterRepository.findById(fromShelterId)
                .orElseThrow(() -> new IllegalStateException(
                        "Source shelter not found for transfer request: " + fromShelterId));
        Shelter to = shelterRepository.findById(toShelterId)
                .orElseThrow(() -> new IllegalStateException(
                        "Destination shelter not found for transfer request: " + toShelterId));

        return new TransferRequest(id, animal, from, to, status, requestedAt);
    }

    /** Writes the entire in-memory store back to the CSV file. */
    private void flush() {
        try {
            StringBuilder sb = new StringBuilder(HEADER).append(System.lineSeparator());
            for (TransferRequest req : store.values()) {
                sb.append(CsvUtils.escapeCsv(req.getId())).append(',')
                  .append(CsvUtils.escapeCsv(req.getAnimal().getId())).append(',')
                  .append(CsvUtils.escapeCsv(req.getFrom().getId())).append(',')
                  .append(CsvUtils.escapeCsv(req.getTo().getId())).append(',')
                  .append(req.getStatus().name()).append(',')
                  .append(req.getRequestedAt().toString())
                  .append(System.lineSeparator());
            }
            Files.writeString(filePath, sb.toString());
        } catch (IOException e) {
            throw new shelter.exception.DataPersistenceException("Failed to flush transfer requests to CSV: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code request} is null
     */
    @Override
    public void save(TransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("TransferRequest must not be null.");
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
    public Optional<TransferRequest> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID must not be null or blank.");
        }
        return Optional.ofNullable(store.get(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TransferRequest> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code animalId} is null or blank
     */
    @Override
    public List<TransferRequest> findByAnimalId(String animalId) {
        if (animalId == null || animalId.isBlank()) {
            throw new IllegalArgumentException("Animal ID must not be null or blank.");
        }
        List<TransferRequest> result = new ArrayList<>();
        for (TransferRequest req : store.values()) {
            if (animalId.equals(req.getAnimal().getId())) {
                result.add(req);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code shelterId} is null or blank
     */
    @Override
    public List<TransferRequest> findByFromShelterId(String shelterId) {
        if (shelterId == null || shelterId.isBlank()) {
            throw new IllegalArgumentException("Shelter ID must not be null or blank.");
        }
        List<TransferRequest> result = new ArrayList<>();
        for (TransferRequest req : store.values()) {
            if (shelterId.equals(req.getFrom().getId())) {
                result.add(req);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code shelterId} is null or blank
     */
    @Override
    public List<TransferRequest> findByToShelterId(String shelterId) {
        if (shelterId == null || shelterId.isBlank()) {
            throw new IllegalArgumentException("Shelter ID must not be null or blank.");
        }
        List<TransferRequest> result = new ArrayList<>();
        for (TransferRequest req : store.values()) {
            if (shelterId.equals(req.getTo().getId())) {
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
    public List<TransferRequest> findByStatus(RequestStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null.");
        }
        List<TransferRequest> result = new ArrayList<>();
        for (TransferRequest req : store.values()) {
            if (req.getStatus() == status) {
                result.add(req);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * {@inheritDoc}
     * Matches {@code shelterId} against both the source and destination shelter of each request.
     *
     * @throws IllegalArgumentException if {@code shelterId} is null or blank, or {@code status} is null
     */
    @Override
    public List<TransferRequest> findByShelterIdAndStatus(String shelterId, RequestStatus status) {
        if (shelterId == null || shelterId.isBlank()) {
            throw new IllegalArgumentException("Shelter ID must not be null or blank.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null.");
        }
        List<TransferRequest> result = new ArrayList<>();
        for (TransferRequest req : store.values()) {
            boolean matchesShelter = shelterId.equals(req.getFrom().getId())
                                  || shelterId.equals(req.getTo().getId());
            if (matchesShelter && req.getStatus() == status) {
                result.add(req);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code animalId} is null or blank, or {@code status} is null
     */
    @Override
    public List<TransferRequest> findByAnimalIdAndStatus(String animalId, RequestStatus status) {
        if (animalId == null || animalId.isBlank()) {
            throw new IllegalArgumentException("Animal ID must not be null or blank.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status must not be null.");
        }
        List<TransferRequest> result = new ArrayList<>();
        for (TransferRequest req : store.values()) {
            if (animalId.equals(req.getAnimal().getId()) && req.getStatus() == status) {
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
