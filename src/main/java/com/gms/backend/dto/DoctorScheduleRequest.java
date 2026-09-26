package com.gms.backend.dto;

import com.gms.backend.entity.DayOfWeek;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Inbound payload for create/update on DoctorSchedule. Used by both
 * single-row insert and bulk-replace endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorScheduleRequest {

    @NotNull
    private Long doctorId;

    @NotNull
    private DayOfWeek dayOfWeek;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    /**
     * Optional — how many patients can share each slot in this window.
     * Defaults to 1 when omitted (standard exclusive slots). Admin-tunable
     * so a flu-clinic doctor can opt into slots that hold e.g. 5 patients.
     */
    @Min(1)
    private Integer capacityPerSlot = 1;
}
