package com.gms.backend.repository;

import com.gms.backend.entity.Doctor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByDoctorId(Long doctorId);
    List<Doctor> findByHospitalId(Long hospitalId);

    /**
     * Acquires a pessimistic write lock on the doctor row for the current
     * transaction. Used by booking flows so two concurrent bookings for the
     * same doctor serialize on this row instead of racing through the
     * "is slot free" check simultaneously. Lock is released when the
     * surrounding transaction commits or rolls back.
     *
     * <p>Pairing the lock with a DB-level unique constraint on
     * (doctor_id, appointment_date, start_time) where status=SCHEDULED
     * gives belt-and-braces protection — the lock avoids most races, the
     * constraint catches the rare case where two transactions slip past the
     * lock (e.g. requests landing on two different replicas of the same
     * service in a future scaled-out deploy).</p>
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Doctor d WHERE d.doctorId = :doctorId")
    Optional<Doctor> findByDoctorIdForUpdate(@Param("doctorId") Long doctorId);
}
