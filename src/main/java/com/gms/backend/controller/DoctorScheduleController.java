package com.gms.backend.controller;

import com.gms.backend.dto.DoctorScheduleRequest;
import com.gms.backend.entity.DoctorSchedule;
import com.gms.backend.service.DoctorScheduleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor-schedule")
public class DoctorScheduleController {

    @Autowired
    private DoctorScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<DoctorSchedule> createSchedule(@Valid @RequestBody DoctorScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.createSchedule(request));
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<DoctorSchedule> updateSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody DoctorScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.updateSchedule(scheduleId, request));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<DoctorSchedule>> getSchedulesByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByDoctor(doctorId));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Dedicated capacity-only update. Body: {@code {"capacityPerSlot": 5}}.
     * Lets an admin promote a window to a higher capacity (e.g. flu-clinic
     * surge) without re-submitting the full schedule DTO and risking a typo
     * on the time fields.
     */
    @PatchMapping("/{scheduleId}/capacity")
    public ResponseEntity<DoctorSchedule> updateCapacity(
            @PathVariable Long scheduleId,
            @RequestBody java.util.Map<String, Integer> body) {
        Integer capacity = body == null ? null : body.get("capacityPerSlot");
        if (capacity == null || capacity < 1) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(scheduleService.updateCapacity(scheduleId, capacity));
    }
}
