package com.gms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorId;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String specialization;

    @NotNull
    @Column(name = "hospital_id", nullable = false)
    private Long hospitalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", insertable = false, updatable = false)
    @JsonIgnore
    private Hospital hospital;

    @NotBlank
    @Column(nullable = false)
    private String licenseNumber;

    /**
     * Average consultation time per patient, in minutes. Defaults to 10.
     * Drives slot-count = (availability-window-minutes / this) for the day,
     * see AppointmentService.avgMinutesPerPatient(doctorId).
     *
     * Nullable in DB so {@code ddl-auto=update} can add the column to tables
     * that already have doctor rows - existing rows store NULL and the
     * service falls back to 10 when reading. New inserts always populate it.
     */
    @Column(name = "avg_minutes_per_patient")
    private Integer avgMinutesPerPatient = 10;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
