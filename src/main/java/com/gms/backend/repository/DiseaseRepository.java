package com.gms.backend.repository;

import com.gms.backend.entity.Disease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DiseaseRepository extends JpaRepository<Disease, Long> {
    Optional<Disease> findByDiseaseId(Long diseaseId);
    Optional<Disease> findByNameIgnoreCase(String name);
}
