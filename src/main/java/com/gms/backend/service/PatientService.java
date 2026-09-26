package com.gms.backend.service;

import com.gms.backend.dto.PatientHistoryItem;
import com.gms.backend.dto.PatientHistoryResponse;
import com.gms.backend.entity.Patient;
import com.gms.backend.entity.Prescription;
import com.gms.backend.entity.Report;
import com.gms.backend.exception.PatientNotFoundException;
import com.gms.backend.repository.PatientRepository;
import com.gms.backend.repository.PrescriptionRepository;
import com.gms.backend.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private ReportRepository reportRepository;

    public Patient registerPatient(Patient patient) {
        patient.setCreatedAt(LocalDateTime.now());
        return patientRepository.save(patient);
    }

    public Patient updatePatient(Long patientId, Patient patientDetails) {
        Patient patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + patientId));
        
        patient.setFullName(patientDetails.getFullName());
        patient.setNationalId(patientDetails.getNationalId());
        patient.setDateOfBirth(patientDetails.getDateOfBirth());
        patient.setGender(patientDetails.getGender());
        patient.setMobileNo(patientDetails.getMobileNo());
        patient.setBloodGroup(patientDetails.getBloodGroup());
        patient.setAddress(patientDetails.getAddress());
        patient.setUpdatedAt(LocalDateTime.now());
        
        return patientRepository.save(patient);
    }

    public Patient getPatientById(Long patientId) {
        return patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + patientId));
    }

    public Patient getPatientByNationalId(String nationalId) {
        return patientRepository.findByNationalId(nationalId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with National ID: " + nationalId));
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public void deletePatient(Long patientId) {
        Patient patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + patientId));
        patientRepository.delete(patient);
    }

    /**
     * Distinct patients this doctor has ever prescribed to or filed a report for.
     * Reads patient_id as a plain projection (not full Prescription/Report
     * entities) - hydrating those entities first registers lazy Patient proxies
     * in the open-in-view session, which findAllById() then reuses instead of
     * plain entities, and Jackson can't serialize a raw Hibernate proxy.
     */
    public List<Patient> getPatientsByDoctorId(Long doctorId) {
        Set<Long> patientIds = new LinkedHashSet<>();
        patientIds.addAll(prescriptionRepository.findDistinctPatientIdByDoctorId(doctorId));
        patientIds.addAll(reportRepository.findDistinctPatientIdByDoctorId(doctorId));
        return patientRepository.findAllById(patientIds);
    }

    public PatientHistoryResponse getPatientHistory(Long patientId) {
        Patient patient = patientRepository.findByPatientId(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + patientId));

        List<Prescription> prescriptions = prescriptionRepository.findByPatientId(patientId);
        List<Report> reports = reportRepository.findByPatientId(patientId);

        List<PatientHistoryItem> items = new ArrayList<>();

        for (Prescription prescription : prescriptions) {
            items.add(new PatientHistoryItem(
                    "PRESCRIPTION",
                    prescription.getPrescriptionId(),
                    "Prescription",
                    prescription.getMedicineList(),
                    prescription.getDoctorId(),
                    prescription.getHospitalId(),
                    prescription.getCreatedAt()
            ));
        }

        for (Report report : reports) {
            items.add(new PatientHistoryItem(
                    "REPORT",
                    report.getReportId(),
                    report.getReportType(),
                    report.getFileName(),
                    report.getDoctorId(),
                    null,
                    report.getCreatedAt()
            ));
        }

        items.sort(Comparator.comparing(PatientHistoryItem::getDate).reversed());

        Set<LocalDate> visitDates = new HashSet<>();
        for (PatientHistoryItem item : items) {
            visitDates.add(item.getDate().toLocalDate());
        }

        return new PatientHistoryResponse(patient, visitDates.size(), items);
    }
}
