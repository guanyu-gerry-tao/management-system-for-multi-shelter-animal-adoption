package shelter.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a staff member operating the shelter management system.
 * In the current demo, a single hardcoded admin staff member is used for all operations.
 * The staff member is associated with audit log entries and notification records to track
 * who performed each action.
 */
public class Staff implements Comparable<Staff> {

    private final String id;
    private String name;
    private String role;

    /**
     * Constructs a Staff instance with no initial values set.
     * This no-arg constructor is provided for framework compatibility; callers should
     * use {@link #setName(String)} and {@link #setRole(String)} to populate the instance.
     */
    public Staff() {
        this.id = UUID.randomUUID().toString();
        this.name = null;
        this.role = null;
    }

    /**
     * Constructs a new Staff member with the given name and no role assigned.
     * A unique ID is generated automatically at construction time.
     *
     * @param name the staff member's full name; must not be null or blank
     * @throws IllegalArgumentException if {@code name} is null or blank
     */
    public Staff(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Staff name must not be null or blank.");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.role = null;
    }

    /**
     * Constructs a new Staff member with the given name and role.
     * A unique ID is generated automatically at construction time.
     *
     * @param name the staff member's full name; must not be null or blank
     * @param role the staff member's role (e.g., "Veterinarian", "Coordinator"); may be null
     * @throws IllegalArgumentException if {@code name} is null or blank
     */
    public Staff(String name, String role) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Staff name must not be null or blank.");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.role = role;
    }

    /**
     * Returns the unique identifier of this staff member.
     * The ID is generated automatically at construction time and never changes.
     *
     * @return the UUID string identifying this staff member
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the full name of this staff member.
     *
     * @return the staff member's name, or null if not yet set
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the name of this staff member.
     * Typically called after a no-arg construction to populate the instance.
     *
     * @param name the new name; must not be null or blank
     * @throws IllegalArgumentException if {@code name} is null or blank
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Staff name must not be null or blank.");
        }
        this.name = name;
    }

    /**
     * Returns the role of this staff member (e.g., "Veterinarian", "Coordinator").
     * May be null if no role has been assigned.
     *
     * @return the staff member's role, or null
     */
    public String getRole() {
        return role;
    }

    /**
     * Updates the role of this staff member.
     * May be set to null to clear the role assignment.
     *
     * @param role the new role, or null to clear
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns a string representation of this staff member including their ID, name, and role.
     *
     * @return a human-readable description of this staff member
     */
    @Override
    public String toString() {
        return "Staff[id=" + id + ", name=" + name + ", role=" + (role != null ? role : "none") + "]";
    }

    /**
     * Returns true if the given object is a Staff with the same ID.
     * Staff identity is determined solely by UUID since names and roles are mutable.
     *
     * @param o the object to compare
     * @return true if {@code o} is a Staff with the same ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Staff)) return false;
        Staff other = (Staff) o;
        return Objects.equals(id, other.id);
    }

    /**
     * Returns a hash code based on this staff member's unique ID.
     * Consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Copy constructor that creates a new Staff with all field values copied from {@code other}.
     * The copy shares the same {@code id} so it represents the same staff member.
     *
     * @param other the Staff instance to copy; must not be null
     */
    public Staff(Staff other) {
        this.id = other.id;
        this.name = other.name;
        this.role = other.role;
    }

    /**
     * Compares this staff member to another by name alphabetically.
     *
     * @param other the other Staff to compare to
     * @return a negative number if this name comes first, positive if later, zero if equal
     */
    @Override
    public int compareTo(Staff other) {
        return this.name.compareTo(other.name);
    }
}
