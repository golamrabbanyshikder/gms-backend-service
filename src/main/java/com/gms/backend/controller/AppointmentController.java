package com.gms.backend.controller;

import com.gms.backend.dto.AppointmentRequest;
import com.gms.backend.dto.AvailableSlotsResponse;
import com.gms.backend.dto.DoctorAvailabilityDate;
import com.gms.backend.entity.Appointment;
import com.gms.backend.entity.AppointmentStatus;
import com.gms.backend.exception.PatientNotFoundException;
import com.gms.backend.repository.PatientRepository;
import com.gms.backend.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientRepository patientRepository;

    @PostMapping
    public ResponseEntity<Appointment> bookAppointment(@Valid @RequestBody AppointmentRequest request) {
        // Validate patient exists before booking - avoids orphaned appointments
        // that fail at history-time only.
        patientRepository.findByPatientId(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + request.getPatientId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.bookAppointment(request));
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<Appointment> getAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(appointmentId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId));
    }

    @GetMapping("/doctor/{doctorId}/available-slots")
    public ResponseEntity<AvailableSlotsResponse> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(doctorId, date));
    }

    /**
     * Returns the next {@code days} (default 14, max 60) dates starting today
     * for which this doctor has a schedule row, along with per-date slot
     * counts and a fully-booked flag. Drives the booking UI's calendar
     * highlight — green = has open slots, grey = scheduled but full, dates
     * the doctor doesn't work are simply omitted.
     */
    @GetMapping("/doctor/{doctorId}/availability")
    public ResponseEntity<List<DoctorAvailabilityDate>> getDoctorAvailability(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "14") int days) {
        return ResponseEntity.ok(appointmentService.doctorAvailability(doctorId, days));
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Appointment> cancelAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(appointmentId));
    }

    @PatchMapping("/{appointmentId}/status")
    public ResponseEntity<Appointment> updateStatus(
            @PathVariable Long appointmentId,
            @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateStatus(appointmentId, status));
    }
}
