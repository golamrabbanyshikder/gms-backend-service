package com.gms.backend.service;

import com.gms.backend.entity.Hospital;
import com.gms.backend.exception.HospitalNotFoundException;
import com.gms.backend.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    public Hospital registerHospital(Hospital hospital) {
        hospital.setCreatedAt(LocalDateTime.now());
        return hospitalRepository.save(hospital);
    }

    public Hospital updateHospital(Long hospitalId, Hospital hospitalDetails) {
        Hospital hospital = hospitalRepository.findByHospitalId(hospitalId)
                .orElseThrow(() -> new HospitalNotFoundException("Hospital not found with ID: " + hospitalId));
        
        hospital.setAddress(hospitalDetails.getAddress());
        hospital.setContactNumber(hospitalDetails.getContactNumber());
        hospital.setUpdatedAt(LocalDateTime.now());
        
        return hospitalRepository.save(hospital);
    }

    public Hospital getHospitalById(Long hospitalId) {
        return hospitalRepository.findByHospitalId(hospitalId)
                .orElseThrow(() -> new HospitalNotFoundException("Hospital not found with ID: " + hospitalId));
    }

    public Hospital getHospitalByName(String hospitalName) {
        return hospitalRepository.findByHospitalName(hospitalName)
                .orElseThrow(() -> new HospitalNotFoundException("Hospital not found with name: " + hospitalName));
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    public void deleteHospital(Long hospitalId) {
        Hospital hospital = hospitalRepository.findByHospitalId(hospitalId)
                .orElseThrow(() -> new HospitalNotFoundException("Hospital not found with ID: " + hospitalId));
        hospitalRepository.delete(hospital);
    }
}
