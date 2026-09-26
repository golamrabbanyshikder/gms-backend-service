package com.gms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A booked appointment between a patient and a doctor for a specific slot.
 * Slots are concrete LocalTime ranges — start inclusive, end exclusive — and
 * the slot length is the doctor's avgMinutesPerPatient at booking time. This
 * keeps overlap-prevention a simple range check on (doctorId, date, startTime)
 * without needing per-slot slot-id bookkeeping.
 */
@Entity
@Table(name = "appointments",
        indexes = {
                @Index(name = "idx_appt_doctor_date", columnList = "doctor_id, appointment_date"),
                @Index(name = "idx_appt_patient", columnList = "patient_id")
        },
        // DB-level backstop against double-booking. The pessimistic Doctor-row
        // lock in AppointmentService.bookAppointment already serializes
        // concurrent bookings for the same doctor, but this constraint catches
        // the rare case where two service replicas (or two writers bypassing
        // the service) slip past the lock. MySQL allows multiple NULLs in a
        // unique index so cancelled rows don't conflict on re-use of a slot.
        uniqueConstraints = @UniqueConstraint(
                name = "uk_appt_doctor_date_time",
                columnNames = {"doctor_id", "appointment_date", "start_time"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    @JsonIgnore
    private Patient patient;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", insertable = false, updatable = false)
    @JsonIgnore
    private Doctor doctor;

    @Column(name = "hospital_id", nullable = false)
    private Long hospitalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", insertable = false, updatable = false)
    @JsonIgnore
    private Hospital hospital;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 16)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(name = "reason_for_visit", length = 500)
    private String reasonForVisit;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "contact_email", length = 120)
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "booked_by", nullable = false, length = 16)
    private BookedBy bookedBy;

    /**
     * Display name of whoever booked the appointment — patient's nationalId
     * for self-bookings, the staff member's username for receptionist bookings.
     */
    @Column(name = "booked_by_username", nullable = false, length = 80)
    private String bookedByUsername;

    /**
     * Raw transcript captured when the appointment was booked via voice input.
     * Null for plain form/text bookings. Kept for audit + future re-parsing.
     */
    @Column(name = "voice_transcript", length = 2000)
    private String voiceTranscript;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
