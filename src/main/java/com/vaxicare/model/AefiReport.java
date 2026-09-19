package com.vaxicare.model;

import com.vaxicare.model.interfaces.Alertable;
import com.vaxicare.model.interfaces.XmlExportable;
import java.time.LocalDateTime;

/**
 * Model representing an Adverse Event Following Immunization (AEFI).
 * Demonstrates: Interface implementation (Alertable, XmlExportable),
 * Comparable interface for Collections PriorityQueue sorting in Multithreading triage.
 */
public class AefiReport implements Alertable, Comparable<AefiReport>, XmlExportable {
    private int reportId;
    private int patientId;
    private int vaccineId;
    private String severity; // MILD, MODERATE, SEVERE
    private String symptoms;
    private String callbackPhone;
    private String triageStatus; // PENDING, CALL_INITIATED, RESOLVED
    private LocalDateTime reportedAt;
    private String triageOfficer;

    // Display fields
    private String patientName;
    private String vaccineName;

    public AefiReport() {
        this.reportedAt = LocalDateTime.now();
        this.triageStatus = "PENDING";
    }

    public AefiReport(int reportId, int patientId, int vaccineId, String severity, 
                      String symptoms, String callbackPhone) {
        this.reportId = reportId;
        this.patientId = patientId;
        this.vaccineId = vaccineId;
        this.severity = severity != null ? severity.toUpperCase() : "MILD";
        this.symptoms = symptoms;
        this.callbackPhone = callbackPhone;
        this.triageStatus = "PENDING";
        this.reportedAt = LocalDateTime.now();
    }

    @Override
    public int getTriagePriority() {
        if ("SEVERE".equalsIgnoreCase(severity)) {
            return 1; // Highest emergency priority
        } else if ("MODERATE".equalsIgnoreCase(severity)) {
            return 2;
        } else {
            return 3; // Mild symptoms
        }
    }

    @Override
    public void dispatchEmergencyAlert(String contactPhone, String alertMessage) {
        System.out.println("==================================================");
        System.out.println("🚨 EMERGENCY TRIAGE DISPATCH: " + alertMessage);
        System.out.println("📞 Alerting Clinical Team & Callback to: " + contactPhone);
        System.out.println("⏰ Timestamp: " + LocalDateTime.now());
        System.out.println("==================================================");
    }

    @Override
    public int compareTo(AefiReport other) {
        // Natural ordering based on triage priority (1 before 2, 2 before 3)
        return Integer.compare(this.getTriagePriority(), other.getTriagePriority());
    }

    @Override
    public String toXmlFragment() {
        return "<aefiReport id=\"" + reportId + "\" priority=\"" + getTriagePriority() + "\">\n" +
               "    <severity>" + escapeXml(severity) + "</severity>\n" +
               "    <symptoms>" + escapeXml(symptoms) + "</symptoms>\n" +
               "    <callbackPhone>" + escapeXml(callbackPhone) + "</callbackPhone>\n" +
               "    <status>" + escapeXml(triageStatus) + "</status>\n" +
               "    <reportedAt>" + reportedAt + "</reportedAt>\n" +
               "</aefiReport>";
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // Getters and Setters
    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getVaccineId() { return vaccineId; }
    public void setVaccineId(int vaccineId) { this.vaccineId = vaccineId; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { 
        this.severity = severity != null ? severity.toUpperCase() : "MILD"; 
    }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getCallbackPhone() { return callbackPhone; }
    public void setCallbackPhone(String callbackPhone) { this.callbackPhone = callbackPhone; }

    public String getTriageStatus() { return triageStatus; }
    public void setTriageStatus(String triageStatus) { this.triageStatus = triageStatus; }

    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }

    public String getTriageOfficer() { return triageOfficer; }
    public void setTriageOfficer(String triageOfficer) { this.triageOfficer = triageOfficer; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getVaccineName() { return vaccineName; }
    public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }
}
