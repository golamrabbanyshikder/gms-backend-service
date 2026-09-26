package com.gms.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalTime;

/**
 * A weekly recurring availability window for a doctor — e.g. Mon 08:00-12:00,
 * Wed 14:00-18:00. A doctor can have multiple rows per day (split shifts).
 * Total slot count for a given date = sum over that day's matching rows of
 * (row-minutes / doctor.avgMinutesPerPatient).
 */
@Entity
@Table(name = "doctor_schedules",
        indexes = @Index(name = "idx_ds_doctor", columnList = "doctor_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @NotNull
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", insertable = false, updatable = false)
    @JsonIgnore
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * How many patients can be booked into each slot inside this window.
     * Defaults to 1 (exclusive slot per patient). Admin-configurable so a
     * doctor running a flu clinic can opt into slots that hold e.g. 5
     * patients concurrently. Nullable so {@code ddl-auto=update} can add the
     * column to tables that already have schedule rows — the service falls
     * back to 1 when reading, same pattern as Doctor.avgMinutesPerPatient.
     */
    @Column(name = "capacity_per_slot")
    private Integer capacityPerSlot = 1;
}
