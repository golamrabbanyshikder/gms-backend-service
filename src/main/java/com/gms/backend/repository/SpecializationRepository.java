package com.gms.backend.repository;

import com.gms.backend.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {
    Optional<Specialization> findBySpecializationId(Long specializationId);
    Optional<Specialization> findByNameIgnoreCase(String name);
}
