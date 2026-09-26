package com.gms.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prescriptionId;

    @NotNull
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    @JsonIgnore
    private Patient patient;

    @NotNull
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", insertable = false, updatable = false)
    @JsonIgnore
    private Doctor doctor;

    @NotNull
    @Column(name = "hospital_id", nullable = false)
    private Long hospitalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", insertable = false, updatable = false)
    @JsonIgnore
    private Hospital hospital;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String medicineList;

    // Comma-separated disease names selected from the admin-maintained Disease
    // list on Create/Upload Prescription. Nullable - a prescription may have
    // none tagged. Plain delimited text, same convention as medicineList.
    @Column(columnDefinition = "TEXT")
    private String diseases;

    @NotBlank
    @Column(nullable = false)
    private String instructions;

    // Nullable: manually-created prescriptions (Create Prescription form) have
    // no source document. "Upload Prescription" OCRs the file for its text AND
    // stores the original image here (file-system-service id) so the scan can
    // be previewed later - mirrors Report.fileReference.
    @Column(name = "file_reference")
    private String fileReference;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate followUpDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
