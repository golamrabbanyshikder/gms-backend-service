package com.gms.backend.repository;

import com.gms.backend.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByReportId(Long reportId);
    List<Report> findByPatientId(Long patientId);
    List<Report> findByDoctorId(Long doctorId);

    @Query("select distinct r.patientId from Report r where r.doctorId = :doctorId")
    List<Long> findDistinctPatientIdByDoctorId(@Param("doctorId") Long doctorId);
}
