package shelter.domain;

import java.util.UUID;

/**
 * Represents a type of vaccine that can be administered to animals in the shelter system.
 * Each vaccine type defines which species it applies to and how long it remains valid
 * before a booster is required. Instances are managed through the vaccine type catalog.
 */
public class VaccineType {

    private final String id;
    private String name;
    private Species applicableSpecies;
    private int validityDays;

    /**
     * Reconstruction constructor for deserializing a VaccineType from persistent storage.
     * This constructor accepts an explicit {@code id} so the original identifier is preserved
     * when reloading the vaccine type catalog from CSV data.
     *
     * @param id                the pre-existing unique identifier; must not be null or blank
     * @param name              the name of the vaccine (e.g., "Rabies", "FVRCP"); must not be null or blank
     * @param applicableSpecies the species this vaccine applies to; must not be null
     * @param validityDays      the number of days the vaccine remains valid; must be positive
     * @throws IllegalArgumentException if any parameter is null, blank, or invalid
     */
    public VaccineType(String id, String name, Species applicableSpecies, int validityDays) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("VaccineType ID must not be null or blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Vaccine type name must not be null or blank.");
        }
        if (applicableSpecies == null) {
            throw new IllegalArgumentException("Applicable species must not be null.");
        }
        if (validityDays <= 0) {
            throw new IllegalArgumentException("Validity days must be positive.");
        }
        this.id = id;
        this.name = name;
        this.applicableSpecies = applicableSpecies;
        this.validityDays = validityDays;
    }

    /**
     * Constructs a new VaccineType with the given name, applicable species, and validity period.
     * A unique ID is generated automatically at construction time.
     *
     * @param name              the name of the vaccine (e.g., "Rabies", "FVRCP"); must not be null or blank
     * @param applicableSpecies the species this vaccine applies to; must not be null
     * @param validityDays      the number of days the vaccine remains valid; must be positive
     * @throws IllegalArgumentException if any parameter is null, blank, or invalid
     */
    public VaccineType(String name, Species applicableSpecies, int validityDays) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Vaccine type name must not be null or blank.");
        }
        if (applicableSpecies == null) {
            throw new IllegalArgumentException("Applicable species must not be null.");
        }
        if (validityDays <= 0) {
            throw new IllegalArgumentException("Validity days must be positive.");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.applicableSpecies = applicableSpecies;
        this.validityDays = validityDays;
    }

    /**
     * Returns the unique identifier of this vaccine type.
     * The ID is generated automatically at construction time and never changes.
     *
     * @return the UUID string identifying this vaccine type
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of this vaccine type.
     *
     * @return the vaccine type name
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the name of this vaccine type.
     * Typically called by {@code VaccineTypeCatalogService} during an update operation.
     *
     * @param name the new name; must not be null or blank
     * @throws IllegalArgumentException if {@code name} is null or blank
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Vaccine type name must not be null or blank.");
        }
        this.name = name;
    }

    /**
     * Returns the species this vaccine type applies to.
     *
     * @return the applicable {@link Species}
     */
    public Species getApplicableSpecies() {
        return applicableSpecies;
    }

    /**
     * Updates the applicable species of this vaccine type.
     *
     * @param applicableSpecies the new applicable species; must not be null
     * @throws IllegalArgumentException if {@code applicableSpecies} is null
     */
    public void setApplicableSpecies(Species applicableSpecies) {
        if (applicableSpecies == null) {
            throw new IllegalArgumentException("Applicable species must not be null.");
        }
        this.applicableSpecies = applicableSpecies;
    }

    /**
     * Returns the number of days this vaccine remains valid before a booster is required.
     *
     * @return the validity period in days
     */
    public int getValidityDays() {
        return validityDays;
    }

    /**
     * Updates the validity period of this vaccine type.
     *
     * @param validityDays the new validity period in days; must be positive
     * @throws IllegalArgumentException if {@code validityDays} is not positive
     */
    public void setValidityDays(int validityDays) {
        if (validityDays <= 0) {
            throw new IllegalArgumentException("Validity days must be positive.");
        }
        this.validityDays = validityDays;
    }

    /**
     * Returns a string representation of this vaccine type including its ID, name, species, and validity.
     *
     * @return a human-readable description of this vaccine type
     */
    @Override
    public String toString() {
        return "VaccineType[id=" + id + ", name=" + name
                + ", species=" + applicableSpecies + ", validityDays=" + validityDays + "]";
    }
}
