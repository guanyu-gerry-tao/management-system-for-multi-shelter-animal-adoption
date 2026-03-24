package shelter.domain;

/**
 * Represents how much time an adopter typically spends at home each day.
 * This value is used by lifestyle-based matching strategies to determine whether
 * an animal's social and attention needs are compatible with the adopter's routine.
 */
public enum DailySchedule {

    /** The adopter is at home for most of the day. */
    HOME_MOST_OF_DAY,

    /** The adopter is away for part of the day, such as during work hours. */
    AWAY_PART_OF_DAY,

    /** The adopter is away for most of the day and has limited time at home. */
    AWAY_MOST_OF_DAY
}
