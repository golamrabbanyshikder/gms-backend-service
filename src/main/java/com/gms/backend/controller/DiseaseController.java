package com.gms.backend.controller;

import com.gms.backend.entity.Disease;
import com.gms.backend.service.DiseaseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/disease")
public class DiseaseController {

    @Autowired
    private DiseaseService diseaseService;

    @PostMapping
    public ResponseEntity<Disease> createDisease(@Valid @RequestBody Disease disease) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diseaseService.createDisease(disease));
    }

    @PutMapping("/{diseaseId}")
    public ResponseEntity<Disease> updateDisease(@PathVariable Long diseaseId, @Valid @RequestBody Disease disease) {
        return ResponseEntity.ok(diseaseService.updateDisease(diseaseId, disease));
    }

    @GetMapping("/{diseaseId}")
    public ResponseEntity<Disease> getDiseaseById(@PathVariable Long diseaseId) {
        return ResponseEntity.ok(diseaseService.getDiseaseById(diseaseId));
    }

    @GetMapping
    public ResponseEntity<List<Disease>> getAllDiseases() {
        return ResponseEntity.ok(diseaseService.getAllDiseases());
    }

    @DeleteMapping("/{diseaseId}")
    public ResponseEntity<Void> deleteDisease(@PathVariable Long diseaseId) {
        diseaseService.deleteDisease(diseaseId);
        return ResponseEntity.noContent().build();
    }
}
