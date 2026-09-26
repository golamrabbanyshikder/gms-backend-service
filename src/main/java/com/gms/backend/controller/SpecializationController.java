package com.gms.backend.controller;

import com.gms.backend.entity.Specialization;
import com.gms.backend.service.SpecializationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/specialization")
public class SpecializationController {

    @Autowired
    private SpecializationService specializationService;

    @PostMapping
    public ResponseEntity<Specialization> createSpecialization(@Valid @RequestBody Specialization specialization) {
        return ResponseEntity.status(HttpStatus.CREATED).body(specializationService.createSpecialization(specialization));
    }

    @PutMapping("/{specializationId}")
    public ResponseEntity<Specialization> updateSpecialization(@PathVariable Long specializationId, @Valid @RequestBody Specialization specialization) {
        return ResponseEntity.ok(specializationService.updateSpecialization(specializationId, specialization));
    }

    @GetMapping("/{specializationId}")
    public ResponseEntity<Specialization> getSpecializationById(@PathVariable Long specializationId) {
        return ResponseEntity.ok(specializationService.getSpecializationById(specializationId));
    }

    @GetMapping
    public ResponseEntity<List<Specialization>> getAllSpecializations() {
        return ResponseEntity.ok(specializationService.getAllSpecializations());
    }

    @DeleteMapping("/{specializationId}")
    public ResponseEntity<Void> deleteSpecialization(@PathVariable Long specializationId) {
        specializationService.deleteSpecialization(specializationId);
        return ResponseEntity.noContent().build();
    }
}
