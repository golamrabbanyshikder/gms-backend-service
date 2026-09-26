package com.gms.backend.service;

import com.gms.backend.dto.DoctorScheduleRequest;
import com.gms.backend.entity.DoctorSchedule;
import com.gms.backend.exception.DoctorNotFoundException;
import com.gms.backend.exception.ScheduleNotFoundException;
import com.gms.backend.repository.DoctorRepository;
import com.gms.backend.repository.DoctorScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DoctorScheduleService {

    @Autowired
    private DoctorScheduleRepository scheduleRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    public DoctorSchedule createSchedule(DoctorScheduleRequest request) {
        doctorRepository.findByDoctorId(request.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + request.getDoctorId()));

        if (request.getEndTime().isBefore(request.getStartTime())
                || request.getEndTime().equals(request.getStartTime())) {
            throw new IllegalArgumentException("Schedule end time must be after start time");
        }

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctorId(request.getDoctorId());
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setCapacityPerSlot(request.getCapacityPerSlot() != null
                ? request.getCapacityPerSlot() : 1);
        return scheduleRepository.save(schedule);
    }

    public DoctorSchedule updateSchedule(Long scheduleId, DoctorScheduleRequest request) {
        DoctorSchedule schedule = scheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found with ID: " + scheduleId));

        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        if (request.getCapacityPerSlot() != null && request.getCapacityPerSlot() > 0) {
            schedule.setCapacityPerSlot(request.getCapacityPerSlot());
        }
        return scheduleRepository.save(schedule);
    }

    /**
     * Dedicated capacity-only PATCH. Lets an admin promote a window from
     * 1-per-slot to N-per-slot (e.g. for a flu-clinic surge) without
     * re-submitting the full schedule DTO and risking a typo on the time
     * fields.
     */
    @Transactional
    public DoctorSchedule updateCapacity(Long scheduleId, int newCapacity) {
        if (newCapacity < 1) {
            throw new IllegalArgumentException("capacityPerSlot must be >= 1");
        }
        DoctorSchedule schedule = scheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found with ID: " + scheduleId));
        schedule.setCapacityPerSlot(newCapacity);
        return scheduleRepository.save(schedule);
    }

    public List<DoctorSchedule> getSchedulesByDoctor(Long doctorId) {
        return scheduleRepository.findByDoctorId(doctorId);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        DoctorSchedule schedule = scheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found with ID: " + scheduleId));
        scheduleRepository.delete(schedule);
    }
}
