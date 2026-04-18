package shelter.application.impl;

import shelter.application.VaccinationApplicationService;
import shelter.application.model.VaccinationRecordView;
import shelter.domain.Animal;
import shelter.domain.VaccinationRecord;
import shelter.domain.Species;
import shelter.domain.VaccineType;
import shelter.exception.EntityNotFoundException;
import shelter.service.AnimalService;
import shelter.service.AuditService;
import shelter.service.VaccinationService;
import shelter.service.VaccineTypeCatalogService;
import shelter.service.model.OverdueVaccination;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of {@link VaccinationApplicationService} that orchestrates
 * vaccination recording, overdue checks, and vaccine type catalog management.
 * Records audit entries for every mutating operation.
 */
public class VaccinationApplicationServiceImpl implements VaccinationApplicationService {

    private final VaccinationService vaccinationService;
    private final VaccineTypeCatalogService vaccineTypeCatalogService;
    private final AnimalService animalService;
    private final AuditService<Object> auditService;

    /**
     * Constructs a VaccinationApplicationServiceImpl with the required service dependencies.
     * All four services are mandatory; none may be null.
     *
     * @param vaccinationService        the service for recording and querying vaccination records; must not be null
     * @param vaccineTypeCatalogService the service for vaccine type catalog management; must not be null
     * @param animalService             the service for animal lookups; must not be null
     * @param auditService              the service for recording audit log entries; must not be null
     * @throws IllegalArgumentException if any argument is null
     */
    public VaccinationApplicationServiceImpl(VaccinationService vaccinationService,
                                              VaccineTypeCatalogService vaccineTypeCatalogService,
                                              AnimalService animalService,
                                              AuditService<Object> auditService) {
        if (vaccinationService        == null) throw new IllegalArgumentException("VaccinationService must not be null.");
        if (vaccineTypeCatalogService == null) throw new IllegalArgumentException("VaccineTypeCatalogService must not be null.");
        if (animalService             == null) throw new IllegalArgumentException("AnimalService must not be null.");
        if (auditService              == null) throw new IllegalArgumentException("AuditService must not be null.");
        this.vaccinationService        = vaccinationService;
        this.vaccineTypeCatalogService = vaccineTypeCatalogService;
        this.animalService             = animalService;
        this.auditService              = auditService;
    }

    /**
     * {@inheritDoc}
     * Looks up the animal and vaccine type by name, then delegates recording to VaccinationService.
     */
    @Override
    public void recordVaccination(String animalId, String vaccineTypeName, LocalDate date) {
        Animal animal           = animalService.findById(animalId);
        VaccineType vaccineType = vaccineTypeCatalogService.findByName(vaccineTypeName);
        vaccinationService.recordVaccination(animal, vaccineType, date);
        auditService.log("recorded vaccination", animal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OverdueVaccination> getOverdueVaccinations(String animalId) {
        Animal animal = animalService.findById(animalId);
        return vaccinationService.getOverdueVaccinations(animal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VaccineType addVaccineType(String name, Species applicableSpecies, int validityDays) {
        VaccineType vaccineType = new VaccineType(name, applicableSpecies, validityDays);
        vaccineTypeCatalogService.add(vaccineType);
        auditService.log("added vaccine type", vaccineType);
        return vaccineType;
    }

    /**
     * {@inheritDoc}
     * Fetches the existing vaccine type, merges only the non-null fields, then persists.
     */
    @Override
    public VaccineType updateVaccineType(String id, String name, Species applicableSpecies, Integer validityDays) {
        VaccineType existing = vaccineTypeCatalogService.findById(id);

        // Apply only provided (non-null) fields
        if (name              != null) existing.setName(name);
        if (applicableSpecies != null) existing.setApplicableSpecies(applicableSpecies);
        if (validityDays      != null) existing.setValidityDays(validityDays);

        vaccineTypeCatalogService.update(existing);
        auditService.log("updated vaccine type", existing);
        return existing;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeVaccineType(String id) {
        vaccineTypeCatalogService.remove(id);
        auditService.log("removed vaccine type", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<VaccineType> listVaccineTypes() {
        return vaccineTypeCatalogService.listAll();
    }

    /**
     * {@inheritDoc}
     * Resolves animal and vaccine type names by looking up each record's referenced IDs
     * through the respective services. Records whose referenced animal or vaccine type
     * cannot be found are skipped (defensive against orphaned rows).
     */
    @Override
    public List<VaccinationRecordView> listAllVaccinationRecords() {
        List<VaccinationRecord> records = vaccinationService.listAllRecords();
        List<VaccinationRecordView> views = new ArrayList<>();
        for (VaccinationRecord r : records) {
            Animal animal;
            VaccineType vt;
            try {
                // Look up associated entities; orphaned rows throw and are skipped
                animal = animalService.findById(r.getAnimalId());
                vt = vaccineTypeCatalogService.findById(r.getVaccineTypeId());
            } catch (EntityNotFoundException skip) {
                continue;
            }
            views.add(new VaccinationRecordView(
                    r, animal.getName(), vt.getName(), animal.getSpecies()));
        }
        return Collections.unmodifiableList(views);
    }
}
