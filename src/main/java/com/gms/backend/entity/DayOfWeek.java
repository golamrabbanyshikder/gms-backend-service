package com.gms.backend.entity;

/**
 * Day-of-week values for {@link DoctorSchedule}. Mapped to integers 1..7
 * matching {@code java.time.DayOfWeek#getValue()} so a schedule row can be
 * resolved to a concrete date by ordinal lookup at booking time.
 */
public enum DayOfWeek {
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    SUNDAY(7);

    private final int value;

    DayOfWeek(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DayOfWeek fromJavaTime(java.time.DayOfWeek dow) {
        return DayOfWeek.values()[dow.getValue() - 1];
    }
}
