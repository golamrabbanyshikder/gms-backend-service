package com.gms.backend.service;

import com.gms.backend.entity.Disease;
import com.gms.backend.exception.DiseaseNotFoundException;
import com.gms.backend.repository.DiseaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Service
public class DiseaseService {

    @Autowired
    private DiseaseRepository diseaseRepository;

    public Disease createDisease(Disease disease) {
        return diseaseRepository.save(disease);
    }

    public Disease updateDisease(Long diseaseId, Disease details) {
        Disease disease = diseaseRepository.findByDiseaseId(diseaseId)
                .orElseThrow(() -> new DiseaseNotFoundException("Disease not found with ID: " + diseaseId));
        disease.setName(details.getName());
        return diseaseRepository.save(disease);
    }

    public Disease getDiseaseById(Long diseaseId) {
        return diseaseRepository.findByDiseaseId(diseaseId)
                .orElseThrow(() -> new DiseaseNotFoundException("Disease not found with ID: " + diseaseId));
    }

    public List<Disease> getAllDiseases() {
        return diseaseRepository.findAll().stream()
                .sorted(Comparator.comparing(Disease::getName))
                .toList();
    }

    public void deleteDisease(Long diseaseId) {
        Disease disease = diseaseRepository.findByDiseaseId(diseaseId)
                .orElseThrow(() -> new DiseaseNotFoundException("Disease not found with ID: " + diseaseId));
        diseaseRepository.delete(disease);
    }
}
