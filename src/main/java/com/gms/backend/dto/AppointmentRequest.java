package com.gms.backend.dto;

import com.gms.backend.entity.BookedBy;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Inbound payload for POST /api/appointment. {@code preferredTime} is optional
 * — when omitted the service auto-assigns the next available slot on the date.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {

    @NotNull
    private Long patientId;

    @NotNull
    private Long doctorId;

    @NotNull
    private LocalDate appointmentDate;

    private LocalTime preferredTime;

    private String reasonForVisit;

    private String contactNumber;

    private String contactEmail;

    @NotNull
    private BookedBy bookedBy;

    private String bookedByUsername;

    private String voiceTranscript;
}
