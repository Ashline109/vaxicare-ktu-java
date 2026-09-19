package com.vaxicare.exception;

/**
 * Exception thrown when a patient attempts to book a subsequent vaccine dose
 * prior to completing the statutory minimum waiting interval.
 * Demonstrates: Domain-specific exception handling in medical applications.
 */
public class InsufficientGapException extends VaxiCareException {
    private final int requiredGapDays;
    private final long actualDaysElapsed;

    public InsufficientGapException(String vaccineName, int requiredGapDays, long actualDaysElapsed) {
        super("Cannot schedule next dose for " + vaccineName + ". Required interval is " + 
              requiredGapDays + " days, but only " + actualDaysElapsed + " days have elapsed.", 
              "ERR_INSUFFICIENT_GAP");
        this.requiredGapDays = requiredGapDays;
        this.actualDaysElapsed = actualDaysElapsed;
    }

    public int getRequiredGapDays() {
        return requiredGapDays;
    }

    public long getActualDaysElapsed() {
        return actualDaysElapsed;
    }
}
