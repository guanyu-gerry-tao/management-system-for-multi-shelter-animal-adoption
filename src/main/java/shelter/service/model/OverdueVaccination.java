package shelter.service.model;

import shelter.domain.VaccineType;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a vaccination that is overdue for a specific animal.
 * Provides details about when the vaccine was last administered and when it was due.
 * Results are naturally ordered by due date ascending, so the most overdue items appear first when sorted.
 */
public class OverdueVaccination implements Comparable<OverdueVaccination> {

    private final VaccineType vaccineType;
    private final LocalDate lastAdministered;
    private final LocalDate dueDate;

    /**
     * Constructs an OverdueVaccination with the given vaccine type, last administered date, and due date.
     * {@code lastAdministered} may be null if the animal has never received this vaccine;
     * {@code vaccineType} and {@code dueDate} must not be null.
     *
     * @param vaccineType      the vaccine type that is overdue; must not be null
     * @param lastAdministered the date the vaccine was last given, or null if never administered
     * @param dueDate          the date by which the vaccine should have been administered; must not be null
     * @throws IllegalArgumentException if {@code vaccineType} or {@code dueDate} is null
     */
    public OverdueVaccination(VaccineType vaccineType, LocalDate lastAdministered, LocalDate dueDate) {
        if (vaccineType == null) {
            throw new IllegalArgumentException("Vaccine type must not be null.");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date must not be null.");
        }
        this.vaccineType = vaccineType;
        this.lastAdministered = lastAdministered;
        this.dueDate = dueDate;
    }

    /**
     * Returns the vaccine type that is overdue.
     * Never null for a validly constructed record.
     *
     * @return the overdue vaccine type
     */
    public VaccineType getVaccineType() {
        return vaccineType;
    }

    /**
     * Returns the date the vaccine was last administered, or null if never given.
     * A null value indicates the animal has never received this vaccine.
     *
     * @return the last administered date, or null
     */
    public LocalDate getLastAdministered() {
        return lastAdministered;
    }

    /**
     * Returns the date by which the vaccine should have been administered.
     * Never null for a validly constructed record.
     *
     * @return the due date
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Compares this overdue vaccination to another by due date ascending.
     * Earlier due dates are ordered first, placing the most overdue items at the top of a sorted list.
     *
     * @param other the other OverdueVaccination to compare to
     * @return a negative number if this due date is earlier, positive if later, zero if equal
     */
    @Override
    public int compareTo(OverdueVaccination other) {
        return this.dueDate.compareTo(other.dueDate);
    }

    /**
     * Returns a string representation of this overdue vaccination including vaccine type, last administered date, and due date.
     *
     * @return a human-readable description of this overdue vaccination
     */
    @Override
    public String toString() {
        return "OverdueVaccination[vaccine=" + vaccineType.getName()
                + ", lastAdministered=" + (lastAdministered != null ? lastAdministered : "never")
                + ", dueDate=" + dueDate + "]";
    }

    /**
     * Returns true if the given object is an OverdueVaccination with the same vaccine type, last administered date, and due date.
     * Equality is value-based since overdue vaccination records have no unique ID.
     *
     * @param o the object to compare
     * @return true if all fields are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OverdueVaccination)) return false;
        OverdueVaccination other = (OverdueVaccination) o;
        return Objects.equals(vaccineType, other.vaccineType)
                && Objects.equals(lastAdministered, other.lastAdministered)
                && Objects.equals(dueDate, other.dueDate);
    }

    /**
     * Returns a hash code based on vaccine type, last administered date, and due date.
     * Consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(vaccineType, lastAdministered, dueDate);
    }
}
