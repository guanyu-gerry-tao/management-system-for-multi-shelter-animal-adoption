package shelter.startup;

import shelter.repository.AdopterRepository;
import shelter.repository.AdoptionRequestRepository;
import shelter.repository.AnimalRepository;
import shelter.repository.AuditRepository;
import shelter.repository.ShelterRepository;
import shelter.repository.TransferRequestRepository;
import shelter.repository.VaccinationRecordRepository;
import shelter.repository.VaccineTypeRepository;

import java.util.Objects;

/**
 * Groups all repositories created during startup into one object.
 * This keeps startup wiring readable by avoiding long parameter lists.
 */
public class RepositoryBundle {

    private final ShelterRepository shelterRepository;
    private final AnimalRepository animalRepository;
    private final AdopterRepository adopterRepository;
    private final AdoptionRequestRepository adoptionRequestRepository;
    private final TransferRequestRepository transferRequestRepository;
    private final VaccineTypeRepository vaccineTypeRepository;
    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final AuditRepository auditRepository;

    /**
     * Constructs a repository bundle containing every repository used by startup wiring.
     *
     * @param shelterRepository the shelter repository
     * @param animalRepository the animal repository
     * @param adopterRepository the adopter repository
     * @param adoptionRequestRepository the adoption request repository
     * @param transferRequestRepository the transfer request repository
     * @param vaccineTypeRepository the vaccine type repository
     * @param vaccinationRecordRepository the vaccination record repository
     * @param auditRepository the audit repository
     */
    public RepositoryBundle(ShelterRepository shelterRepository,
                            AnimalRepository animalRepository,
                            AdopterRepository adopterRepository,
                            AdoptionRequestRepository adoptionRequestRepository,
                            TransferRequestRepository transferRequestRepository,
                            VaccineTypeRepository vaccineTypeRepository,
                            VaccinationRecordRepository vaccinationRecordRepository,
                            AuditRepository auditRepository) {
        this.shelterRepository = Objects.requireNonNull(shelterRepository, "Shelter repository must not be null.");
        this.animalRepository = Objects.requireNonNull(animalRepository, "Animal repository must not be null.");
        this.adopterRepository = Objects.requireNonNull(adopterRepository, "Adopter repository must not be null.");
        this.adoptionRequestRepository = Objects.requireNonNull(
                adoptionRequestRepository, "Adoption request repository must not be null.");
        this.transferRequestRepository = Objects.requireNonNull(
                transferRequestRepository, "Transfer request repository must not be null.");
        this.vaccineTypeRepository = Objects.requireNonNull(
                vaccineTypeRepository, "Vaccine type repository must not be null.");
        this.vaccinationRecordRepository = Objects.requireNonNull(
                vaccinationRecordRepository, "Vaccination record repository must not be null.");
        this.auditRepository = Objects.requireNonNull(auditRepository, "Audit repository must not be null.");
    }

    /**
     * Provides access to the shelter repository for loading and persisting shelter records.
     * Returns the instance supplied to this bundle at construction time.
     *
     * @return the shelter repository
     */
    public ShelterRepository shelterRepository() { return shelterRepository; }

    /**
     * Provides access to the animal repository for loading and persisting animal records.
     * Returns the instance supplied to this bundle at construction time.
     *
     * @return the animal repository
     */
    public AnimalRepository animalRepository() { return animalRepository; }

    /**
     * Provides access to the adopter repository for loading and persisting adopter records.
     * Returns the instance supplied to this bundle at construction time.
     *
     * @return the adopter repository
     */
    public AdopterRepository adopterRepository() { return adopterRepository; }

    /**
     * Provides access to the adoption request repository for loading and persisting adoption requests.
     * Returns the instance supplied to this bundle at construction time.
     *
     * @return the adoption request repository
     */
    public AdoptionRequestRepository adoptionRequestRepository() { return adoptionRequestRepository; }

    /**
     * Provides access to the transfer request repository for loading and persisting transfer requests.
     * Returns the instance supplied to this bundle at construction time.
     *
     * @return the transfer request repository
     */
    public TransferRequestRepository transferRequestRepository() { return transferRequestRepository; }

    /**
     * Provides access to the vaccine type repository for loading and persisting vaccine type definitions.
     * Returns the instance supplied to this bundle at construction time.
     *
     * @return the vaccine type repository
     */
    public VaccineTypeRepository vaccineTypeRepository() { return vaccineTypeRepository; }

    /**
     * Provides access to the vaccination record repository for loading and persisting individual vaccination events.
     * Returns the instance supplied to this bundle at construction time.
     *
     * @return the vaccination record repository
     */
    public VaccinationRecordRepository vaccinationRecordRepository() { return vaccinationRecordRepository; }

    /**
     * Provides access to the audit repository for appending and retrieving audit log entries.
     * Returns the instance supplied to this bundle at construction time.
     *
     * @return the audit repository
     */
    public AuditRepository auditRepository() { return auditRepository; }
}
