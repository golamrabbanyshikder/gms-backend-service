package com.gms.backend.repository;

import com.gms.backend.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByPrescriptionId(Long prescriptionId);
    List<Prescription> findByPatientId(Long patientId);
    List<Prescription> findByDoctorId(Long doctorId);

    @Query("select distinct p.patientId from Prescription p where p.doctorId = :doctorId")
    List<Long> findDistinctPatientIdByDoctorId(@Param("doctorId") Long doctorId);
}
