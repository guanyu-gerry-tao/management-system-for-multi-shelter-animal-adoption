package shelter.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a prospective adopter seeking to adopt an animal from a shelter.
 * An adopter holds personal information, lifestyle context (living space and daily schedule),
 * and explicit adoption preferences that are used during the animal matching process.
 */
public class Adopter implements Comparable<Adopter> {

    private final String id;
    private final String name;
    private final LivingSpace livingSpace;
    private final DailySchedule dailySchedule;
    private final String personalNotes;
    private final AdopterPreferences preferences;
    private final List<String> adoptedAnimalIds;

    /**
     * Constructs a new Adopter with the given personal information and preferences.
     * {@code name}, {@code livingSpace}, {@code dailySchedule}, and {@code preferences} are
     * required; {@code personalNotes} may be {@code null} if the adopter provides no extra context.
     *
     * @param name          the adopter's full name; must not be null or blank
     * @param livingSpace   the adopter's living space type; must not be null
     * @param dailySchedule the adopter's typical daily schedule; must not be null
     * @param personalNotes any additional context provided by the adopter; may be null
     * @param preferences   the adopter's adoption preferences; must not be null
     * @throws IllegalArgumentException if any required parameter is null or {@code name} is blank
     */
    public Adopter(String name, LivingSpace livingSpace, DailySchedule dailySchedule,
                   String personalNotes, AdopterPreferences preferences) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Adopter name must not be null or blank.");
        }
        if (livingSpace == null) {
            throw new IllegalArgumentException("Living space must not be null.");
        }
        if (dailySchedule == null) {
            throw new IllegalArgumentException("Daily schedule must not be null.");
        }
        if (preferences == null) {
            throw new IllegalArgumentException("Adopter preferences must not be null.");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.livingSpace = livingSpace;
        this.dailySchedule = dailySchedule;
        this.personalNotes = personalNotes;
        this.preferences = preferences;
        this.adoptedAnimalIds = new ArrayList<>();
    }

    /**
     * Constructs a copy of the given adopter, preserving the same ID and all field values.
     * The adopted animal IDs list and preferences are defensively copied.
     *
     * @param other the adopter to copy; must not be null
     */
    public Adopter(Adopter other) {
        this.id = other.id;
        this.name = other.name;
        this.livingSpace = other.livingSpace;
        this.dailySchedule = other.dailySchedule;
        this.personalNotes = other.personalNotes;
        this.preferences = new AdopterPreferences(other.preferences);
        this.adoptedAnimalIds = new ArrayList<>(other.adoptedAnimalIds);
    }

    /**
     * Returns the unique identifier of this adopter.
     * The ID is generated automatically at construction and never changes.
     *
     * @return the UUID string identifying this adopter
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the full name of this adopter.
     *
     * @return the adopter's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of living space this adopter has.
     * Used by lifestyle-based matching strategies to filter compatible animals.
     *
     * @return the adopter's {@link LivingSpace}
     */
    public LivingSpace getLivingSpace() {
        return livingSpace;
    }

    /**
     * Returns the typical daily schedule of this adopter.
     * Used by lifestyle-based matching strategies to assess time available for the animal.
     *
     * @return the adopter's {@link DailySchedule}
     */
    public DailySchedule getDailySchedule() {
        return dailySchedule;
    }

    /**
     * Returns any personal notes provided by the adopter.
     * May be {@code null} if the adopter did not supply additional information.
     *
     * @return the adopter's personal notes, or {@code null}
     */
    public String getPersonalNotes() {
        return personalNotes;
    }

    /**
     * Returns the adoption preferences of this adopter.
     *
     * @return the adopter's {@link AdopterPreferences}
     */
    public AdopterPreferences getPreferences() {
        return preferences;
    }

    /**
     * Returns an unmodifiable list of IDs of animals this adopter has adopted.
     * Returns an empty list if the adopter has not yet adopted any animals.
     *
     * @return an unmodifiable list of adopted animal ID strings
     */
    public List<String> getAdoptedAnimalIds() {
        return Collections.unmodifiableList(adoptedAnimalIds);
    }

    /**
     * Records that this adopter has adopted an animal with the given ID.
     * Typically called by {@code AdoptionService} when an adoption request is approved.
     *
     * @param animalId the ID of the adopted animal; must not be null or blank
     * @throws IllegalArgumentException if {@code animalId} is null or blank
     */
    public void addAdoptedAnimalId(String animalId) {
        if (animalId == null || animalId.isBlank()) {
            throw new IllegalArgumentException("Animal ID must not be null or blank.");
        }
        adoptedAnimalIds.add(animalId);
    }

    /**
     * Returns a string representation of this adopter including their ID, name, and lifestyle context.
     *
     * @return a human-readable description of this adopter
     */
    @Override
    public String toString() {
        return "Adopter[id=" + id + ", name=" + name + ", livingSpace=" + livingSpace
                + ", dailySchedule=" + dailySchedule + ", adoptedAnimals=" + adoptedAnimalIds.size() + "]";
    }

    /**
     * Returns true if the given object is an Adopter with the same unique ID.
     * Adopter identity is determined solely by its UUID, consistent with entity semantics.
     *
     * @param o the object to compare
     * @return true if {@code o} is an Adopter with the same ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Adopter)) return false;
        Adopter other = (Adopter) o;
        return Objects.equals(id, other.id);
    }

    /**
     * Returns a hash code based on this adopter's unique ID.
     * Consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Compares this adopter to another by name in alphabetical order.
     * This natural ordering is useful for displaying adopters in sorted lists.
     *
     * @param other the other adopter to compare to
     * @return a negative number if this name comes first, positive if after, zero if equal
     */
    @Override
    public int compareTo(Adopter other) {
        return this.name.compareTo(other.name);
    }
}
