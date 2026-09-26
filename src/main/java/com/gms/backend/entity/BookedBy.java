package com.gms.backend.entity;

/**
 * Which actor originated the booking — a patient via the self-service portal
 * or a receptionist/front-desk staff on behalf of a patient (walk-in / phone).
 *
 * <p>Same VARCHAR-storage rule as {@link AppointmentStatus} — forced VARCHAR so
 * a future constant addition doesn't require a manual schema migration.</p>
 */
public enum BookedBy {
    PATIENT,
    RECEPTIONIST
}
