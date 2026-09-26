package com.gms.backend.dto;

import com.gms.backend.entity.Patient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientHistoryResponse {

    private Patient patient;
    private int visitCount;
    private List<PatientHistoryItem> items;
}
