package com.gms.backend.service;

import com.gms.backend.entity.Prescription;
import com.gms.backend.exception.PrescriptionNotFoundException;
import com.gms.backend.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    public Prescription createPrescription(Prescription prescription) {
        prescription.setCreatedAt(LocalDateTime.now());
        return prescriptionRepository.save(prescription);
    }

    public Prescription updatePrescription(Long prescriptionId, Prescription prescriptionDetails) {
        Prescription prescription = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found with ID: " + prescriptionId));
        
        prescription.setMedicineList(prescriptionDetails.getMedicineList());
        prescription.setInstructions(prescriptionDetails.getInstructions());
        prescription.setFollowUpDate(prescriptionDetails.getFollowUpDate());
        prescription.setUpdatedAt(LocalDateTime.now());
        
        return prescriptionRepository.save(prescription);
    }

    public Prescription getPrescriptionById(Long prescriptionId) {
        return prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found with ID: " + prescriptionId));
    }

    public List<Prescription> getPrescriptionsByPatientId(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    public List<Prescription> getPrescriptionsByDoctorId(Long doctorId) {
        return prescriptionRepository.findByDoctorId(doctorId);
    }

    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    public void deletePrescription(Long prescriptionId) {
        Prescription prescription = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found with ID: " + prescriptionId));
        prescriptionRepository.delete(prescription);
    }
}
