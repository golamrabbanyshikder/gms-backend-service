package com.gms.backend.service;

import com.gms.backend.dto.AppointmentRequest;
import com.gms.backend.dto.AppointmentSlot;
import com.gms.backend.dto.AvailableSlotsResponse;
import com.gms.backend.dto.DoctorAvailabilityDate;
import com.gms.backend.entity.Appointment;
import com.gms.backend.entity.AppointmentStatus;
import com.gms.backend.entity.DayOfWeek;
import com.gms.backend.entity.Doctor;
import com.gms.backend.entity.DoctorSchedule;
import com.gms.backend.exception.AppointmentNotFoundException;
import com.gms.backend.exception.DoctorNotFoundException;
import com.gms.backend.exception.SlotUnavailableException;
import com.gms.backend.repository.AppointmentRepository;
import com.gms.backend.repository.DoctorRepository;
import com.gms.backend.repository.DoctorScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Booking engine — both patient self-booking and receptionist-assisted booking
 * go through this same service. Only the inbound {@link AppointmentRequest#bookedBy}
 * tag and {@link AppointmentRequest#bookedByUsername} differ.
 *
 * <p>Slot generation algorithm:</p>
 * <ol>
 *   <li>Look up all {@link DoctorSchedule} rows for the doctor's day-of-week.</li>
 *   <li>For each window [start, end), slice into
 *       {@code windowMinutes / doctor.avgMinutesPerPatient} equal slots.</li>
 *   <li>Mark each slot available/unavailable based on existing SCHEDULED
 *       appointments' [start, end) overlap.</li>
 * </ol>
 */
@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorScheduleRepository scheduleRepository;

    // ---------- Query / slot listing ----------

    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with ID: " + appointmentId));
    }

    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patientId);
    }

    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(doctorId);
    }

    /**
     * Returns the full list of bookable slots for a doctor on a given date with
     * availability flags. Each slot's start/end is computed by slicing the
     * doctor's availability windows for that day into N equal pieces of size
     * {@code doctor.avgMinutesPerPatient}. A slot is marked unavailable when
     * the count of overlapping SCHEDULED appointments reaches
     * {@code schedule.capacityPerSlot} (default 1).
     */
    public AvailableSlotsResponse getAvailableSlots(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));

        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorIdAndDayOfWeek(
                doctorId, DayOfWeek.fromJavaTime(date.getDayOfWeek()));

        if (schedules.isEmpty()) {
            throw new SlotUnavailableException(
                    "Dr. " + doctor.getName() + " is not available on " + date + ". Please choose another date.");
        }

        int avgMinutes = avgMinutesPerPatient(doctor);

        List<AppointmentSlot> slots = new ArrayList<>();
        LocalTime dayStart = null;
        LocalTime dayEnd = null;

        // Sort windows by start so the returned slot order is chronological
        // even when the underlying schedules list is unordered.
        schedules.sort(Comparator.comparing(DoctorSchedule::getStartTime));

        for (DoctorSchedule window : schedules) {
            if (window.getEndTime().isBefore(window.getStartTime())
                    || window.getEndTime().equals(window.getStartTime())) {
                continue;
            }
            long windowMinutes = Duration.between(window.getStartTime(), window.getEndTime()).toMinutes();
            if (windowMinutes < avgMinutes) {
                // Window too small for even one slot - skip silently so a
                // misconfigured split-shift doesn't poison the day.
                continue;
            }
            int slotsInWindow = (int) (windowMinutes / avgMinutes);
            int capacity = capacityPerSlot(window);

            for (int i = 0; i < slotsInWindow; i++) {
                LocalTime slotStart = window.getStartTime().plusMinutes((long) avgMinutes * i);
                LocalTime slotEnd = slotStart.plusMinutes(avgMinutes);
                long overlapping = appointmentRepository.countOverlapping(
                        doctorId, date, AppointmentStatus.SCHEDULED, slotStart, slotEnd);
                boolean available = overlapping < capacity;
                slots.add(new AppointmentSlot(slotStart, slotEnd, available));

                if (dayStart == null || slotStart.isBefore(dayStart)) {
                    dayStart = slotStart;
                }
                if (dayEnd == null || slotEnd.isAfter(dayEnd)) {
                    dayEnd = slotEnd;
                }
            }
        }

        long remaining = slots.stream().filter(AppointmentSlot::isAvailable).count();

        return new AvailableSlotsResponse(
                doctorId, date,
                dayStart, dayEnd,
                slots.size(), (int) remaining, slots);
    }

    /**
     * Returns the next {@code days} dates (from today inclusive) on which
     * this doctor has at least one schedule row, plus whether the doctor
     * has any slots left on each date. Powers the calendar highlight in
     * the booking UI — green = has open slots, grey = scheduled but full,
     * neutral = no schedule that day.
     */
    public List<DoctorAvailabilityDate> doctorAvailability(Long doctorId, int days) {
        Doctor doctor = doctorRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));
        if (days <= 0) days = 14;
        if (days > 60) days = 60; // sanity cap so a bad client can't ask for a year

        List<DoctorSchedule> all = scheduleRepository.findByDoctorId(doctorId);
        List<LocalDate> out = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < days; i++) {
            LocalDate d = today.plusDays(i);
            DayOfWeek dow = DayOfWeek.fromJavaTime(d.getDayOfWeek());
            for (DoctorSchedule s : all) {
                if (s.getDayOfWeek() == dow) {
                    out.add(d);
                    break;
                }
            }
        }
        List<DoctorAvailabilityDate> result = new ArrayList<>(out.size());
        for (LocalDate d : out) {
            AvailableSlotsResponse resp;
            try {
                resp = getAvailableSlots(doctorId, d);
            } catch (SlotUnavailableException ex) {
                result.add(new DoctorAvailabilityDate(d, 0, 0, false));
                continue;
            }
            result.add(new DoctorAvailabilityDate(d, resp.getTotalSlots() - resp.getRemainingSlots(),
                    resp.getRemainingSlots(), resp.getRemainingSlots() == 0));
        }
        return result;
    }

    // ---------- Booking ----------

    /**
     * Book an appointment. If {@code preferredTime} is provided, the slot at
     * that time is taken (when still available); otherwise the next free
     * slot in the day is auto-assigned. Throws {@link SlotUnavailableException}
     * when the doctor has no schedule that day or the day is fully booked.
     *
     * <p>Race-condition-free: acquires a pessimistic write lock on the doctor
     * row at the start of the transaction so two concurrent bookings for the
     * same doctor serialize on that lock instead of racing through the
     * "is slot free" overlap check. The DB-level unique constraint on
     * (doctor_id, appointment_date, start_time) backs this up against the
     * rare case where two service replicas both miss the lock.</p>
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Appointment bookAppointment(AppointmentRequest request) {
        // Lock the doctor row for the duration of this transaction. Any other
        // booking for the same doctor blocks here until we commit/rollback.
        Doctor doctor = doctorRepository.findByDoctorIdForUpdate(request.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + request.getDoctorId()));

        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorIdAndDayOfWeek(
                request.getDoctorId(), DayOfWeek.fromJavaTime(request.getAppointmentDate().getDayOfWeek()));

        if (schedules.isEmpty()) {
            throw new SlotUnavailableException(
                    "Dr. " + doctor.getName() + " is not available on " + request.getAppointmentDate()
                            + ". Please choose another date.");
        }

        int avgMinutes = avgMinutesPerPatient(doctor);

        // Recompute the slot list INSIDE the locked transaction so the count
        // we read is consistent with what we'll insert.
        List<AppointmentSlot> liveSlots = new ArrayList<>();
        LocalTime dayStart = null;
        LocalTime dayEnd = null;
        for (DoctorSchedule window : schedules) {
            if (window.getEndTime().isBefore(window.getStartTime())
                    || window.getEndTime().equals(window.getStartTime())) {
                continue;
            }
            long windowMinutes = Duration.between(window.getStartTime(), window.getEndTime()).toMinutes();
            if (windowMinutes < avgMinutes) continue;
            int slotsInWindow = (int) (windowMinutes / avgMinutes);
            int capacity = capacityPerSlot(window);
            for (int i = 0; i < slotsInWindow; i++) {
                LocalTime slotStart = window.getStartTime().plusMinutes((long) avgMinutes * i);
                LocalTime slotEnd = slotStart.plusMinutes(avgMinutes);
                long overlapping = appointmentRepository.countOverlapping(
                        request.getDoctorId(), request.getAppointmentDate(),
                        AppointmentStatus.SCHEDULED, slotStart, slotEnd);
                boolean available = overlapping < capacity;
                liveSlots.add(new AppointmentSlot(slotStart, slotEnd, available));
                if (dayStart == null || slotStart.isBefore(dayStart)) dayStart = slotStart;
                if (dayEnd == null || slotEnd.isAfter(dayEnd)) dayEnd = slotEnd;
            }
        }

        long remaining = liveSlots.stream().filter(AppointmentSlot::isAvailable).count();
        if (liveSlots.isEmpty() || remaining == 0) {
            throw new SlotUnavailableException(
                    "আজকের জন্য আর কোনো অ্যাপয়েন্টমেন্ট স্লট খালি নেই। অনুগ্রহ করে পরবর্তী দিন বেছে নিন।"
                            + " | Dr. " + doctor.getName() + " has no remaining slots on "
                            + request.getAppointmentDate() + ".");
        }

        // Capture into final locals so the lambda below compiles. dayStart/dayEnd
        // are mutated inside the slot-generation loop above.
        final LocalTime finalDayStart = dayStart;
        final LocalTime finalDayEnd = dayEnd;

        LocalTime startTime;
        LocalTime endTime;
        if (request.getPreferredTime() != null) {
            // Patient picked a specific time - find the slot that contains it,
            // and reject when that exact slot is already taken.
            AppointmentSlot picked = liveSlots.stream()
                    .filter(s -> !s.getStartTime().isAfter(request.getPreferredTime())
                            && s.getEndTime().isAfter(request.getPreferredTime()))
                    .findFirst()
                    .orElseThrow(() -> new SlotUnavailableException(
                            "Requested time " + request.getPreferredTime()
                                    + " is outside Dr. " + doctor.getName() + "'s working hours on "
                                    + request.getAppointmentDate() + " (working hours: "
                                    + finalDayStart + "-" + finalDayEnd + ")."));
            if (!picked.isAvailable()) {
                throw new SlotUnavailableException(
                        "The requested slot " + picked.getStartTime() + "-" + picked.getEndTime()
                                + " is already booked. Please choose another time.");
            }
            startTime = picked.getStartTime();
            endTime = picked.getEndTime();
        } else {
            // Auto-assign the earliest available slot.
            AppointmentSlot next = liveSlots.stream()
                    .filter(AppointmentSlot::isAvailable)
                    .min(Comparator.comparing(AppointmentSlot::getStartTime))
                    .orElseThrow(() -> new SlotUnavailableException(
                            "Dr. " + doctor.getName() + " has no remaining slots on "
                                    + request.getAppointmentDate() + "."));
            startTime = next.getStartTime();
            endTime = next.getEndTime();
        }

        // One final capacity re-check inside the lock, just before INSERT, to
        // cover the (theoretical) gap between count and insert under the
        // explicit isolation level.
        long finalOverlap = appointmentRepository.countOverlapping(
                request.getDoctorId(), request.getAppointmentDate(),
                AppointmentStatus.SCHEDULED, startTime, endTime);
        int finalCapacity = capacityForWindow(schedules, startTime);
        if (finalOverlap >= finalCapacity) {
            throw new SlotUnavailableException(
                    "The slot " + startTime + "-" + endTime + " was just booked by another patient. "
                            + "Please pick another time.");
        }

        Appointment appointment = new Appointment();
        appointment.setPatientId(request.getPatientId());
        appointment.setDoctorId(request.getDoctorId());
        appointment.setHospitalId(doctor.getHospitalId());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setReasonForVisit(request.getReasonForVisit());
        appointment.setContactNumber(request.getContactNumber());
        appointment.setContactEmail(request.getContactEmail());
        appointment.setBookedBy(request.getBookedBy());
        appointment.setBookedByUsername(request.getBookedByUsername() != null
                ? request.getBookedByUsername() : "system");
        appointment.setVoiceTranscript(request.getVoiceTranscript());

        try {
            return appointmentRepository.saveAndFlush(appointment);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // The DB unique constraint fired — somebody else won the race even
            // despite the lock (e.g. service was scaled out). Translate to the
            // same domain exception the rest of the flow handles.
            throw new SlotUnavailableException(
                    "The slot " + startTime + "-" + endTime + " was just booked by another patient. "
                            + "Please pick another time.");
        }
    }

    /** Slot capacity from a single window — defaults to 1 when unconfigured. */
    private int capacityPerSlot(DoctorSchedule window) {
        Integer c = window.getCapacityPerSlot();
        return (c != null && c > 0) ? c : 1;
    }

    /**
     * Look up the capacity of the window that contains {@code at} so the
     * just-before-INSERT overlap re-check uses the same number the earlier
     * per-slot calculation used.
     */
    private int capacityForWindow(List<DoctorSchedule> windows, LocalTime at) {
        for (DoctorSchedule w : windows) {
            if (!at.isBefore(w.getStartTime()) && at.isBefore(w.getEndTime())) {
                return capacityPerSlot(w);
            }
        }
        return 1;
    }

    // ---------- Lifecycle / cancel ----------

    @Transactional
    public Appointment cancelAppointment(Long appointmentId) {
        Appointment appointment = getAppointmentById(appointmentId);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return appointment;
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(java.time.LocalDateTime.now());
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment updateStatus(Long appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = getAppointmentById(appointmentId);
        appointment.setStatus(newStatus);
        appointment.setUpdatedAt(java.time.LocalDateTime.now());
        return appointmentRepository.save(appointment);
    }

    // ---------- Helpers ----------

    private int avgMinutesPerPatient(Doctor doctor) {
        Integer avg = doctor.getAvgMinutesPerPatient();
        return (avg != null && avg > 0) ? avg : 10;
    }

    public Optional<Doctor> getDoctor(Long doctorId) {
        return doctorRepository.findByDoctorId(doctorId);
    }
}
