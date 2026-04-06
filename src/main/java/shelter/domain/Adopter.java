package shelter.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents a prospective adopter seeking to adopt an animal from a shelter.
 * An adopter holds personal information, lifestyle context (living space and daily schedule),
 * and explicit adoption preferences that are used during the animal matching process.
 */
public class Adopter {

    private final String id;
    private final String name;
    private final LivingSpace livingSpace;
    private final DailySchedule dailySchedule;
    private final String personalNotes;
    private final AdopterPreferences preferences;
    private final List<String> adoptedAnimalIds;

    /**
     * Reconstruction constructor for deserializing an Adopter from persistent storage.
     * This constructor accepts an explicit {@code id} and a pre-populated list of adopted
     * animal IDs so that the full adopter state can be restored from CSV data.
     *
     * @param id               the pre-existing unique identifier; must not be null or blank
     * @param name             the adopter's full name; must not be null or blank
     * @param livingSpace      the adopter's living space type; must not be null
     * @param dailySchedule    the adopter's typical daily schedule; must not be null
     * @param personalNotes    any additional context provided by the adopter; may be null
     * @param preferences      the adopter's adoption preferences; must not be null
     * @param adoptedAnimalIds the list of animal IDs this adopter has previously adopted; may be empty
     * @throws IllegalArgumentException if any required parameter is null or {@code name} is blank
     */
    public Adopter(String id, String name, LivingSpace livingSpace, DailySchedule dailySchedule,
                   String personalNotes, AdopterPreferences preferences, List<String> adoptedAnimalIds) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Adopter ID must not be null or blank.");
        }
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
        this.id = id;
        this.name = name;
        this.livingSpace = livingSpace;
        this.dailySchedule = dailySchedule;
        this.personalNotes = personalNotes;
        this.preferences = preferences;
        this.adoptedAnimalIds = new ArrayList<>(adoptedAnimalIds != null ? adoptedAnimalIds : Collections.emptyList());
    }

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
}
