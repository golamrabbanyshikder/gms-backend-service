package com.gms.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * One bookable slot inside a doctor's day, returned by the available-slots
 * endpoint and consumed by the booking form's dropdown.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSlot {
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
}
