package com.vaxicare.model.interfaces;

import java.time.LocalDate;

/**
 * Interface representing scheduling and interval verification capabilities.
 * Demonstrates: Interface abstraction, loose coupling in KTU S3 Java OOP syllabus.
 */
public interface Schedulable {
    /**
     * Determines whether a patient is eligible for the specified dose.
     * @param currentDose The dose being requested (1, 2, or booster).
     * @param daysSinceLastDose Elapsed days since previous inoculation.
     * @return true if dosage gap criteria is satisfied.
     */
    boolean isEligibleForDose(int currentDose, long daysSinceLastDose);

    /**
     * Calculates the earliest recommended date for the next dose.
     * @param lastDoseDate Date of the preceding dose.
     * @param completedDose The dose number just completed.
     * @return Earliest eligible appointment date.
     */
    LocalDate calculateNextDoseDate(LocalDate lastDoseDate, int completedDose);
}
