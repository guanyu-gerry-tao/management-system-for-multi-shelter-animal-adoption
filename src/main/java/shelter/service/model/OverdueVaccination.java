package shelter.service.model;

import shelter.domain.VaccineType;

import java.time.LocalDate;

/**
 * Represents a vaccination that is overdue for a specific animal.
 * Provides details about when the vaccine was last administered and when it was due.
 */
public class OverdueVaccination {

    private final VaccineType vaccineType;
    private final LocalDate lastAdministered;
    private final LocalDate dueDate;

    /**
     * Constructs an OverdueVaccination with the given vaccine type, last administered date, and due date.
     * {@code lastAdministered} may be null if the animal has never received this vaccine.
     *
     * @param vaccineType      the vaccine type that is overdue
     * @param lastAdministered the date the vaccine was last given, or null if never administered
     * @param dueDate          the date by which the vaccine should have been administered
     */
    public OverdueVaccination(VaccineType vaccineType, LocalDate lastAdministered, LocalDate dueDate) {
        this.vaccineType = vaccineType;
        this.lastAdministered = lastAdministered;
        this.dueDate = dueDate;
    }

    /**
     * Returns the vaccine type that is overdue.
     *
     * @return the overdue vaccine type
     */
    public VaccineType getVaccineType() {
        return vaccineType;
    }

    /**
     * Returns the date the vaccine was last administered, or null if never given.
     *
     * @return the last administered date, or null
     */
    public LocalDate getLastAdministered() {
        return lastAdministered;
    }

    /**
     * Returns the date by which the vaccine should have been administered.
     *
     * @return the due date
     */
    public LocalDate getDueDate() {
        return dueDate;
    }
}
