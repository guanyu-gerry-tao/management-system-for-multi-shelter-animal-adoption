package shelter.application.model;

import shelter.domain.Species;
import shelter.domain.VaccinationRecord;

import java.util.Objects;

/**
 * Application-layer DTO that bundles a {@link VaccinationRecord} with the display names
 * of its associated animal and vaccine type. Used by the presentation layer to render
 * human-readable vaccination snapshots without re-querying the domain services.
 */
public final class VaccinationRecordView {

    private final VaccinationRecord record;
    private final String animalName;
    private final String vaccineTypeName;
    private final Species species;

    /**
     * Constructs a VaccinationRecordView bundling a record with its resolved display fields.
     * All fields are required and must not be null or blank.
     *
     * @param record          the underlying vaccination record; must not be null
     * @param animalName      the animal's display name; must not be null or blank
     * @param vaccineTypeName the vaccine type's display name; must not be null or blank
     * @param species         the animal's species; must not be null
     * @throws IllegalArgumentException if any argument is null or blank
     */
    public VaccinationRecordView(VaccinationRecord record, String animalName,
                                 String vaccineTypeName, Species species) {
        if (record == null) {
            throw new IllegalArgumentException("VaccinationRecord must not be null.");
        }
        if (animalName == null || animalName.isBlank()) {
            throw new IllegalArgumentException("Animal name must not be null or blank.");
        }
        if (vaccineTypeName == null || vaccineTypeName.isBlank()) {
            throw new IllegalArgumentException("Vaccine type name must not be null or blank.");
        }
        if (species == null) {
            throw new IllegalArgumentException("Species must not be null.");
        }
        this.record = record;
        this.animalName = animalName;
        this.vaccineTypeName = vaccineTypeName;
        this.species = species;
    }

    /**
     * Returns the wrapped vaccination record.
     *
     * @return the wrapped vaccination record
     */
    public VaccinationRecord getRecord() {
        return record;
    }

    /**
     * Returns the display name of the vaccinated animal.
     *
     * @return the display name of the vaccinated animal
     */
    public String getAnimalName() {
        return animalName;
    }

    /**
     * Returns the display name of the vaccine type.
     *
     * @return the display name of the vaccine type
     */
    public String getVaccineTypeName() {
        return vaccineTypeName;
    }

    /**
     * Returns the species of the vaccinated animal.
     *
     * @return the species of the vaccinated animal
     */
    public Species getSpecies() {
        return species;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VaccinationRecordView that)) return false;
        return Objects.equals(record, that.record)
                && Objects.equals(animalName, that.animalName)
                && Objects.equals(vaccineTypeName, that.vaccineTypeName)
                && species == that.species;
    }

    @Override
    public int hashCode() {
        return Objects.hash(record, animalName, vaccineTypeName, species);
    }

    @Override
    public String toString() {
        return "VaccinationRecordView[record=" + record + ", animal=" + animalName
                + ", vaccine=" + vaccineTypeName + ", species=" + species + "]";
    }
}
