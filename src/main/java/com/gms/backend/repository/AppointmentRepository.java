package com.gms.backend.repository;

import com.gms.backend.entity.Appointment;
import com.gms.backend.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByAppointmentId(Long appointmentId);

    List<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(Long doctorId);

    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate appointmentDate);

    /**
     * Used for overlap detection: any SCHEDULED appointment for this doctor on
     * this date whose [start_time, end_time) intersects [proposedStart, proposedEnd).
     */
    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Long doctorId,
            LocalDate appointmentDate,
            AppointmentStatus status,
            LocalTime proposedEnd,
            LocalTime proposedStart);

    List<Appointment> findByDoctorIdAndAppointmentDateAndStatus(
            Long doctorId, LocalDate appointmentDate, AppointmentStatus status);

    /**
     * Count of SCHEDULED appointments that overlap a given slot range for
     * one doctor on one date. Used by the capacity-aware "is slot free"
     * check — a slot is full when this count >= schedule.capacityPerSlot.
     *
     * <p>Two intervals [a,b) and [c,d) overlap iff a < d AND c < b.</p>
     */
    @Query("SELECT COUNT(a) FROM Appointment a "
            + "WHERE a.doctorId = :doctorId "
            + "AND a.appointmentDate = :date "
            + "AND a.status = :status "
            + "AND a.startTime < :proposedEnd "
            + "AND a.endTime > :proposedStart")
    long countOverlapping(@Param("doctorId") Long doctorId,
                          @Param("date") LocalDate date,
                          @Param("status") AppointmentStatus status,
                          @Param("proposedStart") LocalTime proposedStart,
                          @Param("proposedEnd") LocalTime proposedEnd);

    /**
     * For each distinct date in [from, to] that this doctor has at least one
     * SCHEDULED appointment on. Used by the calendar view's "show only dates
     * that already have a booking" indicator (admins usually want to see
     * busy days highlighted differently than fully-empty days).
     */
    @Query("SELECT DISTINCT a.appointmentDate FROM Appointment a "
            + "WHERE a.doctorId = :doctorId "
            + "AND a.appointmentDate BETWEEN :from AND :to "
            + "AND a.status = :status")
    List<LocalDate> findBusyDates(@Param("doctorId") Long doctorId,
                                  @Param("from") LocalDate from,
                                  @Param("to") LocalDate to,
                                  @Param("status") AppointmentStatus status);
}

