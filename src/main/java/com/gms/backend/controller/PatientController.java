package com.gms.backend.controller;

import com.gms.backend.dto.PatientHistoryResponse;
import com.gms.backend.entity.Patient;
import com.gms.backend.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @PostMapping("/register")
    public ResponseEntity<Patient> registerPatient(@Valid @RequestBody Patient patient) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.registerPatient(patient));
    }

    @PutMapping("/{patientId}")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long patientId, @Valid @RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.updatePatient(patientId, patient));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientService.getPatientById(patientId));
    }

    @GetMapping("/{patientId}/history")
    public ResponseEntity<PatientHistoryResponse> getPatientHistory(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientService.getPatientHistory(patientId));
    }

    @GetMapping("/national-id/{nationalId}")
    public ResponseEntity<Patient> getPatientByNationalId(@PathVariable String nationalId) {
        return ResponseEntity.ok(patientService.getPatientByNationalId(nationalId));
    }

    @GetMapping("/by-doctor/{doctorId}")
    public ResponseEntity<List<Patient>> getPatientsByDoctorId(@PathVariable Long doctorId) {
        return ResponseEntity.ok(patientService.getPatientsByDoctorId(doctorId));
    }

    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long patientId) {
        patientService.deletePatient(patientId);
        return ResponseEntity.noContent().build();
    }
}
