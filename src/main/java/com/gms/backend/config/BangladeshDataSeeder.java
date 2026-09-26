package com.gms.backend.config;

import com.gms.backend.entity.DayOfWeek;
import com.gms.backend.entity.Doctor;
import com.gms.backend.entity.DoctorSchedule;
import com.gms.backend.entity.Hospital;
import com.gms.backend.entity.Specialization;
import com.gms.backend.repository.DoctorRepository;
import com.gms.backend.repository.DoctorScheduleRepository;
import com.gms.backend.repository.HospitalRepository;
import com.gms.backend.repository.SpecializationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds the backend-service database with real Bangladeshi hospitals,
 * medical specializations, doctors with Bangladeshi names, and weekly
 * schedules. Runs once at startup; skips everything that already exists.
 *
 * <p>The hospital and specialty names match the Bangla→English phrase
 * dictionary in gateway-service so the offline voice-fill path resolves
 * real hospital and specialty entries instead of falling through to
 * "first match".</p>
 *
 * <p>Order matters: specializations must exist before doctors reference
 * them by name, and hospitals must exist before doctors reference them by
 * ID. {@link Order} puts this after the api-key seeder so the service is
 * fully wired before rows land.</p>
 */
@Component
@Order(10)
public class BangladeshDataSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(BangladeshDataSeeder.class);

    private final HospitalRepository hospitalRepository;
    private final SpecializationRepository specializationRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;

    public BangladeshDataSeeder(HospitalRepository hospitalRepository,
                                SpecializationRepository specializationRepository,
                                DoctorRepository doctorRepository,
                                DoctorScheduleRepository scheduleRepository) {
        this.hospitalRepository = hospitalRepository;
        this.specializationRepository = specializationRepository;
        this.doctorRepository = doctorRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            seedSpecializations();
            seedHospitals();
            seedDoctorsAndSchedules();
            logger.info("BangladeshDataSeeder: seed complete. hospitals={}, specializations={}, doctors={}",
                    hospitalRepository.count(),
                    specializationRepository.count(),
                    doctorRepository.count());
        } catch (Exception e) {
            // Don't kill the app on a seed failure — log loud and let the
            // operator decide. Booking can still run against whatever data
            // already exists.
            logger.error("BangladeshDataSeeder failed: {}", e.getMessage(), e);
        }
    }

    // ====================================================================
    // Specializations
    // ====================================================================

    private static final List<String> SPECIALIZATIONS = List.of(
            "Cardiology",
            "Neurology",
            "Orthopedics",
            "Pediatrics",
            "Dermatology",
            "Gynecology",
            "Oncology",
            "Psychiatry",
            "Ophthalmology",
            "ENT",
            "General Medicine",
            "Nephrology",
            "Hepatology",
            "Dentistry",
            "Urology"
    );

    private void seedSpecializations() {
        for (String name : SPECIALIZATIONS) {
            if (specializationRepository.findByNameIgnoreCase(name).isEmpty()) {
                Specialization s = new Specialization();
                s.setName(name);
                specializationRepository.save(s);
            }
        }
    }

    // ====================================================================
    // Hospitals — real Bangladeshi institutions. Address/city/state/zip/
    // contact are illustrative; the project doesn't validate them, only
    // hospitalName is unique.
    // ====================================================================

    /** key = hospital name (used for upsert), value = [address, city, state, zip, phone] */
    private static final Map<String, String[]> HOSPITALS = new LinkedHashMap<>();
    static {
        HOSPITALS.put("Square Hospital",                new String[]{"18/F, Bir Uttam Qazi Nuruzzaman Sarak", "Dhaka", "Dhaka",     "1205", "+880-2-8159457"});
        HOSPITALS.put("Apollo Hospital Dhaka",           new String[]{"Plot 81, Block E, Bashundhara R/A",     "Dhaka", "Dhaka",     "1229", "+880-2-8401661"});
        HOSPITALS.put("United Hospital",                 new String[]{"Plot 15, Road 71, Gulshan-2",           "Dhaka", "Dhaka",     "1212", "+880-2-8836000"});
        HOSPITALS.put("BIRDEM General Hospital",         new String[]{"122, Kazi Nazrul Islam Avenue",         "Dhaka", "Dhaka",     "1000", "+880-2-58616611"});
        HOSPITALS.put("Dhaka Medical College Hospital",  new String[]{"Bakshibh, Secretariat Road",            "Dhaka", "Dhaka",     "1000", "+880-2-55165000"});
        HOSPITALS.put("City General Hospital",           new String[]{"Mohammadpur, Town Hall",                "Dhaka", "Dhaka",     "1207", "+880-2-9123256"});
        HOSPITALS.put("Bangabandhu Sheikh Mujib Medical University", new String[]{"Shahbag",                  "Dhaka", "Dhaka",     "1000", "+880-2-55165760"});
        HOSPITALS.put("Evercare Hospital Dhaka",         new String[]{"Plot 81, Block H, Bashundhara R/A",     "Dhaka", "Dhaka",     "1229", "+880-2-55037242"});
        HOSPITALS.put("Labaid Specialized Hospital",     new String[]{"House 1, Road 4, Dhanmondi",            "Dhaka", "Dhaka",     "1205", "+880-2-9676356"});
        HOSPITALS.put("Popular Diagnostic Center",       new String[]{"House 16, Road 2, Dhanmondi",           "Dhaka", "Dhaka",     "1205", "+880-2-9662749"});
        HOSPITALS.put("Ibrahim Cardiac Hospital",        new String[]{"Shahbag",                               "Dhaka", "Dhaka",     "1000", "+880-2-9674021"});
        HOSPITALS.put("National Heart Foundation",       new String[]{"Mirpur",                                "Dhaka", "Dhaka",     "1216", "+880-2-9002020"});
        HOSPITALS.put("Chittagong Medical College",      new String[]{"Panchlaish",                            "Chattogram", "Chattogram", "4203", "+880-31-619500"});
        HOSPITALS.put("Rajshahi Medical College",        new String[]{"Laxmipur",                              "Rajshahi",   "Rajshahi",   "6000", "+880-721-775200"});
        HOSPITALS.put("Khulna Medical College",          new String[]{"Boyra",                                 "Khulna",     "Khulna",     "9000", "+880-41-760350"});
        HOSPITALS.put("Sylhet MAG Osmani Medical College", new String[]{"Chowhatta",                           "Sylhet",     "Sylhet",     "3100", "+880-821-713555"});
        HOSPITALS.put("Rangpur Medical College",         new String[]{"Rangpur Sadar",                         "Rangpur",    "Rangpur",    "5400", "+880-521-63355"});
        HOSPITALS.put("Barishal Sher-e-Bangla Medical College", new String[]{"South Alekanda",                    "Barishal",   "Barishal",   "8200", "+880-431-21735"});
        HOSPITALS.put("Mymensingh Medical College",      new String[]{"Charpara",                              "Mymensingh", "Mymensingh", "2200", "+880-911-66099"});
    }

    private void seedHospitals() {
        for (Map.Entry<String, String[]> e : HOSPITALS.entrySet()) {
            if (hospitalRepository.findByHospitalName(e.getKey()).isEmpty()) {
                Hospital h = new Hospital();
                h.setHospitalName(e.getKey());
                String[] v = e.getValue();
                h.setAddress(v[0]);
                h.setCity(v[1]);
                h.setState(v[2]);
                h.setZipCode(v[3]);
                h.setContactNumber(v[4]);
                hospitalRepository.save(h);
            }
        }
    }

    // ====================================================================
    // Doctors — Bangladeshi names, mapped to (hospital, specialization)
    // License numbers are illustrative; the column is non-null but the
    // service doesn't validate the format.
    // ====================================================================

    /** (hospital, specialization, fullName, licenseNumber, avgMinutesPerPatient) */
    private static final List<Object[]> DOCTORS = List.of(
            // Cardiology
            row("Square Hospital",                "Cardiology",       "Dr. Mohammad Farooque",          "BMDC-CARD-1001", 10),
            row("Square Hospital",                "Cardiology",       "Dr. Nazrul Islam",                "BMDC-CARD-1002", 10),
            row("Ibrahim Cardiac Hospital",       "Cardiology",       "Dr. AKM Fazlur Rahman",          "BMDC-CARD-1003", 10),
            row("National Heart Foundation",      "Cardiology",       "Dr. Sohel Ahmed",                 "BMDC-CARD-1004", 10),
            row("United Hospital",                "Cardiology",       "Dr. Nurun Nahar",                 "BMDC-CARD-1005", 10),
            row("Apollo Hospital Dhaka",          "Cardiology",       "Dr. Tahsin Rahman",               "BMDC-CARD-1006", 10),
            row("Labaid Specialized Hospital",    "Cardiology",       "Dr. Md. Shamsul Haque",           "BMDC-CARD-1007", 10),
            row("Evercare Hospital Dhaka",        "Cardiology",       "Dr. AFM Saidur Rahman",           "BMDC-CARD-1008", 10),

            // Neurology
            row("Square Hospital",                "Neurology",        "Dr. Quazi Deen Mohammad",         "BMDC-NEUR-2001", 15),
            row("Apollo Hospital Dhaka",          "Neurology",        "Dr. Hasan Zahidur Rahman",        "BMDC-NEUR-2002", 15),
            row("BSMMU",                          "Neurology",        "Dr. Md. Moniruzzaman Bhuiyan",    "BMDC-NEUR-2003", 15),
            row("United Hospital",                "Neurology",        "Dr. Rumana Habib",                "BMDC-NEUR-2004", 15),
            row("BIRDEM General Hospital",        "Neurology",        "Dr. Mohammod Nazrul Islam",       "BMDC-NEUR-2005", 15),

            // Orthopedics
            row("Popular Diagnostic Center",      "Orthopedics",      "Dr. Md. Abdul Gani",              "BMDC-ORTH-3001", 10),
            row("Square Hospital",                "Orthopedics",      "Dr. Amimul Ehsan",                "BMDC-ORTH-3002", 10),
            row("Apollo Hospital Dhaka",          "Orthopedics",      "Dr. Mainul Haque",                "BMDC-ORTH-3003", 10),
            row("United Hospital",                "Orthopedics",      "Dr. Sk. Nurul Alam",              "BMDC-ORTH-3004", 10),
            row("Labaid Specialized Hospital",    "Orthopedics",      "Dr. AKM Mosharraf Hossain",       "BMDC-ORTH-3005", 10),

            // Pediatrics
            row("Dhaka Medical College Hospital", "Pediatrics",       "Dr. Probir Kumar Saha",           "BMDC-PED-4001", 10),
            row("Square Hospital",                "Pediatrics",       "Dr. Khwaja Abdul Hannan",         "BMDC-PED-4002", 10),
            row("Apollo Hospital Dhaka",          "Pediatrics",       "Dr. Shaheen Akhter",              "BMDC-PED-4003", 10),
            row("United Hospital",                "Pediatrics",       "Dr. Sufia Khanam",                "BMDC-PED-4004", 10),
            row("Chittagong Medical College",     "Pediatrics",       "Dr. Pranab Kumar Chowdhury",      "BMDC-PED-4005", 10),

            // Dermatology
            row("Square Hospital",                "Dermatology",      "Dr. Md. Elias Hossain",           "BMDC-DERM-5001", 10),
            row("Labaid Specialized Hospital",    "Dermatology",      "Dr. Rehana Rahim",                "BMDC-DERM-5002", 10),
            row("Apollo Hospital Dhaka",          "Dermatology",      "Dr. Mahbubul Islam",              "BMDC-DERM-5003", 10),

            // Gynecology
            row("Square Hospital",                "Gynecology",       "Dr. Sharmin Afroze",              "BMDC-GYN-6001", 15),
            row("BSMMU",                          "Gynecology",       "Dr. Ferdousi Begum",              "BMDC-GYN-6002", 15),
            row("Popular Diagnostic Center",      "Gynecology",       "Dr. Afroza Chowdhury",            "BMDC-GYN-6003", 15),
            row("Rajshahi Medical College",       "Gynecology",       "Dr. Rowshan Ara",                 "BMDC-GYN-6004", 15),

            // Oncology
            row("BIRDEM General Hospital",        "Oncology",         "Dr. Md. Moarraf Hossen",          "BMDC-ONC-7001", 20),
            row("BSMMU",                          "Oncology",         "Dr. AKM Hamidur Rahman",          "BMDC-ONC-7002", 20),
            row("Apollo Hospital Dhaka",          "Oncology",         "Dr. Lutful Aziz",                 "BMDC-ONC-7003", 20),

            // Psychiatry
            row("BSMMU",                          "Psychiatry",       "Dr. Jhunu Shamsunnahar",          "BMDC-PSY-8001", 20),
            row("Pabna Mental Hospital",          "Psychiatry",       "Dr. Md. Harun-Ar-Rashid",         "BMDC-PSY-8002", 20),
            row("Square Hospital",                "Psychiatry",       "Dr. Kamrul Hasan",                "BMDC-PSY-8003", 20),

            // Ophthalmology
            row("Bangabandhu Sheikh Mujib Medical University", "Ophthalmology", "Dr. Md. Nazrul Islam",  "BMDC-OPH-9001", 10),
            row("Square Hospital",                "Ophthalmology",    "Dr. Sarwar Jahan",                "BMDC-OPH-9002", 10),
            row("National Institute of Ophthalmology", "Ophthalmology", "Dr. Md. Shafi Khan",             "BMDC-OPH-9003", 10),

            // ENT
            row("Square Hospital",                "ENT",              "Dr. Md. Ashraful Islam",          "BMDC-ENT-1101", 10),
            row("Apollo Hospital Dhaka",          "ENT",              "Dr. Pran Gopal Datta",            "BMDC-ENT-1102", 10),
            row("Popular Diagnostic Center",      "ENT",              "Dr. Md. Delwar Hossain",          "BMDC-ENT-1103", 10),

            // General Medicine
            row("Dhaka Medical College Hospital", "General Medicine", "Dr. Khan Abul Kalam Azad",        "BMDC-GM-1201", 10),
            row("Square Hospital",                "General Medicine", "Dr. Tabassum Tahmina",            "BMDC-GM-1202", 10),
            row("Chittagong Medical College",     "General Medicine", "Dr. Md. Nurul Amin",              "BMDC-GM-1203", 10),
            row("Khulna Medical College",         "General Medicine", "Dr. Shahinul Islam",              "BMDC-GM-1204", 10),

            // Nephrology
            row("BIRDEM General Hospital",        "Nephrology",       "Dr. Muhammad Nazrul Islam",       "BMDC-NEPH-1301", 15),
            row("BSMMU",                          "Nephrology",       "Dr. Md. Nazim Uddin",             "BMDC-NEPH-1302", 15),
            row("Apollo Hospital Dhaka",          "Nephrology",       "Dr. Tanvir Ahmed",                "BMDC-NEPH-1303", 15),

            // Hepatology
            row("BIRDEM General Hospital",        "Hepatology",       "Dr. Mamun-Al-Mahtab",             "BMDC-HEP-1401", 20),
            row("BSMMU",                          "Hepatology",       "Dr. Md. Abdur Rahim",             "BMDC-HEP-1402", 20),

            // Dentistry
            row("Square Hospital",                "Dentistry",        "Dr. Md. Shamsul Alam",            "BMDC-DEN-1501", 10),
            row("Labaid Specialized Hospital",    "Dentistry",        "Dr. Tahmina Akhter",              "BMDC-DEN-1502", 10),

            // Urology
            row("Square Hospital",                "Urology",          "Dr. M. A. Salam",                 "BMDC-URO-1601", 15),
            row("BSMMU",                          "Urology",          "Dr. AKM Khurshidul Alam",         "BMDC-URO-1602", 15)
    );

    private static Object[] row(String hospital, String spec, String name, String license, int avgMins) {
        return new Object[]{hospital, spec, name, license, avgMins};
    }

    private void seedDoctorsAndSchedules() {
        for (Object[] r : DOCTORS) {
            String hospitalName = (String) r[0];
            String specName     = (String) r[1];
            String doctorName   = (String) r[2];
            String license      = (String) r[3];
            int    avgMins      = (Integer) r[4];

            // Idempotency check by license number — doctors are unique by
            // license, not by name. We re-use the existing row when one
            // exists so we don't pile up duplicates on every restart.
            List<Doctor> existing = doctorRepository.findAll();
            Doctor doctor = existing.stream()
                    .filter(d -> license.equals(d.getLicenseNumber()))
                    .findFirst()
                    .orElse(null);
            if (doctor == null) {
                Hospital hospital = hospitalRepository.findByHospitalName(hospitalName).orElse(null);
                if (hospital == null) {
                    logger.warn("Skipping doctor {} — hospital '{}' not found", doctorName, hospitalName);
                    continue;
                }
                doctor = new Doctor();
                doctor.setName(doctorName);
                doctor.setSpecialization(specName);
                doctor.setHospitalId(hospital.getHospitalId());
                doctor.setLicenseNumber(license);
                doctor.setAvgMinutesPerPatient(avgMins);
                doctor = doctorRepository.save(doctor);
            }

            // Always (re)seed the schedule so any time-table changes are
            // picked up on next restart. We delete the existing rows first
            // so we don't double up on weekday windows.
            scheduleRepository.findByDoctorId(doctor.getDoctorId()).forEach(s -> scheduleRepository.delete(s));
            seedDefaultSchedule(doctor.getDoctorId(), specName);
        }
    }

    /**
     * Default weekly schedule: morning + evening windows on weekdays, plus
     * a half-day Saturday morning. Mirrors typical Bangladeshi private
     * hospital hours (8am-2pm, 5pm-9pm). Hospitals are closed Friday
     * morning; we give the doctors a single Friday window so the form
     * still has slots to show.
     */
    private void seedDefaultSchedule(Long doctorId, String specialization) {
        // Long-consultation specialties get fewer but wider windows so each
        // slot still fits. Others get the standard short-window pattern.
        boolean longConsult = List.of("Oncology", "Psychiatry", "Hepatology")
                .contains(specialization);

        // Weekday morning (8-14)
        addSchedule(doctorId, DayOfWeek.SUNDAY,    LocalTime.of(8, 0),  LocalTime.of(14, 0));
        addSchedule(doctorId, DayOfWeek.MONDAY,    LocalTime.of(8, 0),  LocalTime.of(14, 0));
        addSchedule(doctorId, DayOfWeek.TUESDAY,   LocalTime.of(8, 0),  LocalTime.of(14, 0));
        addSchedule(doctorId, DayOfWeek.WEDNESDAY, LocalTime.of(8, 0),  LocalTime.of(14, 0));
        // Friday morning is light — many doctors run a short clinic.
        addSchedule(doctorId, DayOfWeek.FRIDAY,    LocalTime.of(9, 0),  LocalTime.of(13, 0));

        // Weekday evening (17-21) — common split-shift in BD hospitals.
        addSchedule(doctorId, DayOfWeek.SUNDAY,    LocalTime.of(17, 0), LocalTime.of(21, 0));
        addSchedule(doctorId, DayOfWeek.MONDAY,    LocalTime.of(17, 0), LocalTime.of(21, 0));
        addSchedule(doctorId, DayOfWeek.TUESDAY,   LocalTime.of(17, 0), LocalTime.of(21, 0));
        addSchedule(doctorId, DayOfWeek.WEDNESDAY, LocalTime.of(17, 0), LocalTime.of(21, 0));
        addSchedule(doctorId, DayOfWeek.THURSDAY,  LocalTime.of(17, 0), LocalTime.of(21, 0));

        if (longConsult) {
            // For long-consult specialties: add a Thursday morning window
            // so we still hit ~5 weekday windows after dropping Friday.
            addSchedule(doctorId, DayOfWeek.THURSDAY, LocalTime.of(8, 0), LocalTime.of(13, 0));
        } else {
            // Otherwise: full-day Thursday + Saturday morning.
            addSchedule(doctorId, DayOfWeek.THURSDAY, LocalTime.of(8, 0),  LocalTime.of(14, 0));
            addSchedule(doctorId, DayOfWeek.SATURDAY, LocalTime.of(9, 0),  LocalTime.of(13, 0));
            addSchedule(doctorId, DayOfWeek.SATURDAY, LocalTime.of(17, 0), LocalTime.of(20, 0));
        }
    }

    private void addSchedule(Long doctorId, DayOfWeek day, LocalTime start, LocalTime end) {
        DoctorSchedule s = new DoctorSchedule();
        s.setDoctorId(doctorId);
        s.setDayOfWeek(day);
        s.setStartTime(start);
        s.setEndTime(end);
        s.setCapacityPerSlot(1);
        scheduleRepository.save(s);
    }
}