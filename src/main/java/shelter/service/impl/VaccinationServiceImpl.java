package shelter.service.impl;

import shelter.domain.Animal;
import shelter.domain.VaccinationRecord;
import shelter.domain.VaccineType;
import shelter.exception.EntityNotFoundException;
import shelter.exception.SpeciesMismatchException;
import shelter.repository.VaccinationRecordRepository;
import shelter.repository.VaccineTypeRepository;
import shelter.service.AuditService;
import shelter.service.VaccinationInfoProvider;
import shelter.service.VaccinationService;
import shelter.service.model.OverdueVaccination;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Concrete implementation of {@link VaccinationService} that records vaccination events,
 * checks overdue status against each vaccine type's validity period, and queries vaccination
 * history by animal. All significant operations are forwarded to the {@link AuditService}
 * for traceability.
 */
public class VaccinationServiceImpl implements VaccinationService, VaccinationInfoProvider {

    private final VaccinationRecordRepository recordRepository;
    private final VaccineTypeRepository vaccineTypeRepository;
    private final AuditService<VaccinationRecord> auditService;

    /**
     * Constructs a new {@code VaccinationServiceImpl} with the required repositories and audit service.
     * The vaccine type repository is used to determine which vaccines are applicable to a given species
     * when evaluating overdue status.
     *
     * @param recordRepository    the repository for vaccination record persistence; must not be null
     * @param vaccineTypeRepository the repository for vaccine type catalog lookups; must not be null
     * @param auditService        the service used to log vaccination events; must not be null
     * @throws IllegalArgumentException if any argument is null
     */
    public VaccinationServiceImpl(VaccinationRecordRepository recordRepository,
                                  VaccineTypeRepository vaccineTypeRepository,
                                  AuditService<VaccinationRecord> auditService) {
        if (recordRepository == null) {
            throw new IllegalArgumentException("VaccinationRecordRepository must not be null.");
        }
        if (vaccineTypeRepository == null) {
            throw new IllegalArgumentException("VaccineTypeRepository must not be null.");
        }
        if (auditService == null) {
            throw new IllegalArgumentException("AuditService must not be null.");
        }
        this.recordRepository = recordRepository;
        this.vaccineTypeRepository = vaccineTypeRepository;
        this.auditService = auditService;
    }

    /**
     * {@inheritDoc}
     * Validates that the vaccine type is applicable to the animal's species before creating the record.
     * The vaccination is persisted and an audit entry is logged on success.
     *
     * @throws IllegalArgumentException if any argument is null
     * @throws SpeciesMismatchException if the vaccine type does not apply to the animal's species
     */
    @Override
    public void recordVaccination(Animal animal, VaccineType vaccineType, LocalDate date) {
        // Guard: all three arguments are required
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }
        if (vaccineType == null) {
            throw new IllegalArgumentException("VaccineType must not be null.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null.");
        }

        // Business rule: the vaccine must be approved for this animal's species
        if (vaccineType.getApplicableSpecies() != animal.getSpecies()) {
            throw new SpeciesMismatchException(
                    "Vaccine '" + vaccineType.getName() + "' is not applicable to species "
                            + animal.getSpecies() + ".");
        }

        // Create and persist the vaccination record
        VaccinationRecord record = new VaccinationRecord(
                animal.getId(), vaccineType.getId(), date);
        recordRepository.save(record);

        // Log the operation for audit trail
        auditService.log("recorded vaccination: " + vaccineType.getName()
                + " for animal " + animal.getName(), record);
    }

    /**
     * {@inheritDoc}
     * For each vaccine type applicable to the animal's species, finds the most recent
     * vaccination date and compares it against the vaccine's validity period.
     * A vaccine is overdue if it has never been given or if today is on or after the due date.
     * Results are sorted by due date ascending so the most overdue entries appear first.
     *
     * @throws IllegalArgumentException if {@code animal} is null
     */
    @Override
    public List<OverdueVaccination> getOverdueVaccinations(Animal animal) {
        // Guard: animal is required
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }

        // Retrieve all vaccine types applicable to this animal's species
        List<VaccineType> applicableTypes = getApplicableVaccineTypes(animal);

        // Build the full vaccination history for this animal once, to avoid repeated queries
        List<VaccinationRecord> history = recordRepository.findByAnimalId(animal.getId());

        LocalDate today = LocalDate.now();
        List<OverdueVaccination> overdue = new ArrayList<>();

        for (VaccineType vt : applicableTypes) {
            // Find the most recent administration date for this vaccine type
            LocalDate lastAdministered = findMostRecentDate(history, vt.getId());

            // Calculate the due date: validity window from last dose, or today if never given
            LocalDate dueDate = (lastAdministered != null)
                    ? lastAdministered.plusDays(vt.getValidityDays())
                    : today;

            // A vaccine is overdue if today is on or after the due date
            if (!today.isBefore(dueDate)) {
                overdue.add(new OverdueVaccination(vt, lastAdministered, dueDate));
            }
        }

        Collections.sort(overdue);
        return Collections.unmodifiableList(overdue);
    }

    /**
     * Returns all vaccine types applicable to the given animal's species.
     *
     * @param animal the animal to query
     * @return the list of vaccine types applicable to the animal
     * @throws IllegalArgumentException if {@code animal} is null
     */
    @Override
    public List<VaccineType> getApplicableVaccineTypes(Animal animal) {
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }
        return vaccineTypeRepository.findByApplicableSpecies(animal.getSpecies());
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if {@code animal} is null
     */
    @Override
    public List<VaccinationRecord> getVaccinationHistory(Animal animal) {
        // Guard: animal is required
        if (animal == null) {
            throw new IllegalArgumentException("Animal must not be null.");
        }
        return recordRepository.findByAnimalId(animal.getId());
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException  if {@code id} is null or blank
     * @throws EntityNotFoundException   if no vaccination record with the given ID exists
     */
    @Override
    public VaccinationRecord findById(String id) {
        // Guard: ID is required for lookup
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Vaccination record ID must not be null or blank.");
        }
        Optional<VaccinationRecord> found = recordRepository.findById(id);
        if (found.isEmpty()) {
            throw new EntityNotFoundException("VaccinationRecord not found: " + id);
        }
        return found.get();
    }

    /**
     * {@inheritDoc}
     * Delegates directly to the underlying repository's {@code findAll} query.
     */
    @Override
    public List<VaccinationRecord> listAllRecords() {
        return recordRepository.findAll();
    }

    /**
     * Scans the given vaccination history and returns the most recent administration date
     * for the specified vaccine type ID. Returns null if no matching record is found.
     *
     * @param history       the list of vaccination records to search
     * @param vaccineTypeId the vaccine type ID to filter by
     * @return the most recent date, or null if never administered
     */
    private LocalDate findMostRecentDate(List<VaccinationRecord> history, String vaccineTypeId) {
        LocalDate latest = null;
        for (VaccinationRecord rec : history) {
            if (vaccineTypeId.equals(rec.getVaccineTypeId())) {
                if (latest == null || rec.getDateAdministered().isAfter(latest)) {
                    latest = rec.getDateAdministered();
                }
            }
        }
        return latest;
    }
}
