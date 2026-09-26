package com.gms.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Outcome of GET /api/appointment/doctor/{id}/available-slots?date=YYYY-MM-DD.
 * Total slot count + remaining slots make it trivial for the UI to render the
 * "fully booked" message without re-deriving it from the slot list.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotsResponse {
    private Long doctorId;
    private LocalDate date;
    private LocalTime dayStartTime;
    private LocalTime dayEndTime;
    private int totalSlots;
    private int remainingSlots;
    private List<AppointmentSlot> slots;
}
