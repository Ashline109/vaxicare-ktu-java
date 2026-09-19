package com.vaxicare.service;

import com.vaxicare.exception.VaxiCareException;
import com.vaxicare.model.Appointment;
import com.vaxicare.model.Patient;
import com.vaxicare.model.Vaccine;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service orchestrating clinical business logic, dose gap verification,
 * and immunization status calculation.
 * Demonstrates: Encapsulation of business logic, Collections (Map, List).
 */
public class VaccinationService {

    /**
     * Immunization completion status summary.
     */
    public static class ImmunizationStatus {
        public final String statusLabel; // "NOT_VACCINATED", "PARTIALLY_VACCINATED", "FULLY_VACCINATED", "BOOSTER_ELIGIBLE"
        public final int completedDoses;
        public final int totalDosesRequired;
        public final LocalDate nextEligibleDate;
        public final String vaccineName;

        public ImmunizationStatus(String statusLabel, int completedDoses, int totalDosesRequired, 
                                  LocalDate nextEligibleDate, String vaccineName) {
            this.statusLabel = statusLabel;
            this.completedDoses = completedDoses;
            this.totalDosesRequired = totalDosesRequired;
            this.nextEligibleDate = nextEligibleDate;
            this.vaccineName = vaccineName;
        }
    }

    /**
     * Evaluates patient's vaccination status for a given vaccine.
     */
    public ImmunizationStatus evaluateStatus(Patient patient, Vaccine vaccine, List<Appointment> history) {
        if (vaccine == null) {
            return new ImmunizationStatus("NOT_VACCINATED", 0, 0, LocalDate.now(), "None");
        }

        int completed = 0;
        LocalDate lastDoseDate = null;

        for (Appointment a : history) {
            if (a.getVaccineId() == vaccine.getVaccineId() && "COMPLETED".equalsIgnoreCase(a.getStatus())) {
                completed++;
                if (lastDoseDate == null || a.getAppointmentDate().isAfter(lastDoseDate)) {
                    lastDoseDate = a.getAppointmentDate();
                }
            }
        }

        if (completed == 0) {
            return new ImmunizationStatus("NOT_VACCINATED", 0, vaccine.getDosesRequired(), LocalDate.now(), vaccine.getName());
        }

        if (completed < vaccine.getDosesRequired()) {
            LocalDate nextDate = vaccine.calculateNextDoseDate(lastDoseDate, completed);
            return new ImmunizationStatus("PARTIALLY_VACCINATED", completed, vaccine.getDosesRequired(), nextDate, vaccine.getName());
        }

        // Check for precautionary booster (e.g. 180 days after final standard dose)
        long daysSinceLast = ChronoUnit.DAYS.between(lastDoseDate, LocalDate.now());
        if (daysSinceLast >= 180) {
            return new ImmunizationStatus("BOOSTER_ELIGIBLE", completed, vaccine.getDosesRequired(), LocalDate.now(), vaccine.getName());
        }

        return new ImmunizationStatus("FULLY_VACCINATED", completed, vaccine.getDosesRequired(), null, vaccine.getName());
    }
}
