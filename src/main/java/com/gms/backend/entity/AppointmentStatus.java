package com.gms.backend.entity;

/**
 * Lifecycle states for a booked appointment.
 *
 * <p>Forced to VARCHAR (not a native MySQL ENUM column) because Hibernate 6's
 * default ENUM mapping can't be widened by ddl-auto=update when a new constant
 * is added later — same trap the User.role field already hit when PATIENT was
 * added. See {@code gms_tech_stack} memory for the prior incident.</p>
 */
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
