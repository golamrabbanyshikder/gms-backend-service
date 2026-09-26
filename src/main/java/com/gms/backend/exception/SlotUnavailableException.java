package com.gms.backend.exception;

/**
 * Thrown when a booking request cannot be satisfied — doctor fully booked on
 * that date, requested time outside the doctor's working hours, etc. Mapped
 * to 409 Conflict by {@link GlobalExceptionHandler}.
 */
public class SlotUnavailableException extends RuntimeException {
    public SlotUnavailableException(String message) {
        super(message);
    }
}
