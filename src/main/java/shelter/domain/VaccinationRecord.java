package shelter.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single vaccination event for an animal in the shelter system.
 * Each record captures which animal received which vaccine and on what date,
 * allowing the system to determine whether vaccinations are current or overdue.
 */
public class VaccinationRecord implements Comparable<VaccinationRecord> {

    private final String id;
    private final String animalId;
    private final String vaccineTypeId;
    private final LocalDate dateAdministered;

    /**
     * Reconstruction constructor for deserializing a VaccinationRecord from persistent storage.
     * This constructor accepts an explicit {@code id} so the original record identifier is
     * preserved when reloading vaccination history from CSV data.
     *
     * @param id               the pre-existing unique identifier; must not be null or blank
     * @param animalId         the ID of the animal that received the vaccine; must not be null or blank
     * @param vaccineTypeId    the ID of the vaccine type administered; must not be null or blank
     * @param dateAdministered the date the vaccine was given; must not be null
     * @throws IllegalArgumentException if any parameter is null or blank
     */
    public VaccinationRecord(String id, String animalId, String vaccineTypeId, LocalDate dateAdministered) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("VaccinationRecord ID must not be null or blank.");
        }
        if (animalId == null || animalId.isBlank()) {
            throw new IllegalArgumentException("Animal ID must not be null or blank.");
        }
        if (vaccineTypeId == null || vaccineTypeId.isBlank()) {
            throw new IllegalArgumentException("Vaccine type ID must not be null or blank.");
        }
        if (dateAdministered == null) {
            throw new IllegalArgumentException("Date administered must not be null.");
        }
        this.id = id;
        this.animalId = animalId;
        this.vaccineTypeId = vaccineTypeId;
        this.dateAdministered = dateAdministered;
    }

    /**
     * Constructs a new VaccinationRecord for the given animal, vaccine type, and date.
     * A unique ID is generated automatically at construction time.
     *
     * @param animalId          the ID of the animal that received the vaccine; must not be null or blank
     * @param vaccineTypeId     the ID of the vaccine type administered; must not be null or blank
     * @param dateAdministered  the date the vaccine was given; must not be null
     * @throws IllegalArgumentException if any parameter is null or blank
     */
    public VaccinationRecord(String animalId, String vaccineTypeId, LocalDate dateAdministered) {
        if (animalId == null || animalId.isBlank()) {
            throw new IllegalArgumentException("Animal ID must not be null or blank.");
        }
        if (vaccineTypeId == null || vaccineTypeId.isBlank()) {
            throw new IllegalArgumentException("Vaccine type ID must not be null or blank.");
        }
        if (dateAdministered == null) {
            throw new IllegalArgumentException("Date administered must not be null.");
        }
        this.id = UUID.randomUUID().toString();
        this.animalId = animalId;
        this.vaccineTypeId = vaccineTypeId;
        this.dateAdministered = dateAdministered;
    }

    /**
     * Returns the unique identifier of this vaccination record.
     * The ID is generated automatically at construction time and never changes.
     *
     * @return the UUID string identifying this record
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the ID of the animal that received the vaccine.
     *
     * @return the animal's ID string
     */
    public String getAnimalId() {
        return animalId;
    }

    /**
     * Returns the ID of the vaccine type that was administered.
     *
     * @return the vaccine type's ID string
     */
    public String getVaccineTypeId() {
        return vaccineTypeId;
    }

    /**
     * Returns the date on which the vaccine was administered.
     *
     * @return the date administered
     */
    public LocalDate getDateAdministered() {
        return dateAdministered;
    }

    /**
     * Copy constructor that creates a new VaccinationRecord with all field values copied from {@code other}.
     * The copy preserves the same ID, animal, vaccine type, and administration date.
     *
     * @param other the VaccinationRecord instance to copy; must not be null
     */
    public VaccinationRecord(VaccinationRecord other) {
        this(other.id, other.animalId, other.vaccineTypeId, other.dateAdministered);
    }

    /**
     * Compares this record to another by date administered ascending.
     * Earlier records are ordered first, reflecting chronological vaccination history.
     *
     * @param other the other VaccinationRecord to compare to
     * @return a negative number if this record was administered earlier, positive if later, zero if equal
     */
    @Override
    public int compareTo(VaccinationRecord other) {
        return this.dateAdministered.compareTo(other.dateAdministered);
    }

    /**
     * Returns true if the given object is a VaccinationRecord with the same ID.
     *
     * @param o the object to compare
     * @return true if {@code o} is a VaccinationRecord with the same ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VaccinationRecord)) return false;
        VaccinationRecord other = (VaccinationRecord) o;
        return Objects.equals(id, other.id);
    }

    /**
     * Returns a hash code based on this record's unique ID.
     * Consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of this vaccination record including its ID, animal, vaccine, and date.
     *
     * @return a human-readable description of this vaccination record
     */
    @Override
    public String toString() {
        return "VaccinationRecord[id=" + id + ", animalId=" + animalId
                + ", vaccineTypeId=" + vaccineTypeId + ", date=" + dateAdministered + "]";
    }
}
