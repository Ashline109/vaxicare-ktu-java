package com.vaxicare.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model representing a scheduled or completed vaccination inoculation session.
 * Connects Patient, Vaccine, and Centre entities.
 */
public class Appointment {
    private int appointmentId;
    private int patientId;
    private int vaccineId;
    private int centreId;
    private int doseNumber;
    private LocalDate appointmentDate;
    private String status; // SCHEDULED, COMPLETED, CANCELLED
    private LocalDateTime bookingTimestamp;

    // Supplementary display attributes (populated via SQL joins)
    private String patientName;
    private String patientPhone;
    private String vaccineName;
    private String centreName;

    public Appointment() {}

    public Appointment(int appointmentId, int patientId, int vaccineId, int centreId, 
                       int doseNumber, LocalDate appointmentDate, String status) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.vaccineId = vaccineId;
        this.centreId = centreId;
        this.doseNumber = doseNumber;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.bookingTimestamp = LocalDateTime.now();
    }

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getVaccineId() { return vaccineId; }
    public void setVaccineId(int vaccineId) { this.vaccineId = vaccineId; }

    public int getCentreId() { return centreId; }
    public void setCentreId(int centreId) { this.centreId = centreId; }

    public int getDoseNumber() { return doseNumber; }
    public void setDoseNumber(int doseNumber) { this.doseNumber = doseNumber; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getBookingTimestamp() { return bookingTimestamp; }
    public void setBookingTimestamp(LocalDateTime bookingTimestamp) { this.bookingTimestamp = bookingTimestamp; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public String getVaccineName() { return vaccineName; }
    public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }

    public String getCentreName() { return centreName; }
    public void setCentreName(String centreName) { this.centreName = centreName; }
}
