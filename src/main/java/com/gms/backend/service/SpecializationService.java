package com.gms.backend.service;

import com.gms.backend.entity.Specialization;
import com.gms.backend.exception.SpecializationNotFoundException;
import com.gms.backend.repository.SpecializationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Service
public class SpecializationService {

    @Autowired
    private SpecializationRepository specializationRepository;

    public Specialization createSpecialization(Specialization specialization) {
        return specializationRepository.save(specialization);
    }

    public Specialization updateSpecialization(Long specializationId, Specialization details) {
        Specialization specialization = specializationRepository.findBySpecializationId(specializationId)
                .orElseThrow(() -> new SpecializationNotFoundException("Specialization not found with ID: " + specializationId));
        specialization.setName(details.getName());
        return specializationRepository.save(specialization);
    }

    public Specialization getSpecializationById(Long specializationId) {
        return specializationRepository.findBySpecializationId(specializationId)
                .orElseThrow(() -> new SpecializationNotFoundException("Specialization not found with ID: " + specializationId));
    }

    public List<Specialization> getAllSpecializations() {
        return specializationRepository.findAll().stream()
                .sorted(Comparator.comparing(Specialization::getName))
                .toList();
    }

    public void deleteSpecialization(Long specializationId) {
        Specialization specialization = specializationRepository.findBySpecializationId(specializationId)
                .orElseThrow(() -> new SpecializationNotFoundException("Specialization not found with ID: " + specializationId));
        specializationRepository.delete(specialization);
    }
}
