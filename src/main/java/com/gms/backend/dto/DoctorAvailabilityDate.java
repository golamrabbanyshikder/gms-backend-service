package com.gms.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * One day's availability for a doctor — used by the booking UI's calendar
 * view. {@code bookedSlots} + {@code remainingSlots} = totalSlots that day;
 * {@code fullyBooked} lets the UI render the day greyed out without doing
 * the math client-side.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailabilityDate {
    private LocalDate date;
    private int bookedSlots;
    private int remainingSlots;
    private boolean fullyBooked;
}
