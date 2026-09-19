package com.vaxicare.model;

import com.vaxicare.model.interfaces.XmlExportable;
import java.time.LocalDate;
import java.time.Period;

/**
 * Concrete Patient class inheriting from Person.
 * Demonstrates: Single Inheritance, method overriding, super keyword, and interface implementation.
 */
public class Patient extends Person implements XmlExportable {
    private int patientId;
    private LocalDate dateOfBirth;
    private String aadharNumber;
    private String bloodGroup;
    private String medicalNotes;

    public Patient() {
        super();
    }

    public Patient(int userId, String username, String fullName, String phone,
                   int patientId, LocalDate dateOfBirth, String aadharNumber, 
                   String bloodGroup, String medicalNotes) {
        super(userId, username, fullName, phone, "PATIENT");
        this.patientId = patientId;
        this.dateOfBirth = dateOfBirth;
        this.aadharNumber = aadharNumber;
        this.bloodGroup = bloodGroup;
        this.medicalNotes = medicalNotes;
    }

    /**
     * Calculates patient's age in completed years using Java Time API.
     */
    public int getAge() {
        if (dateOfBirth == null) return 0;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    @Override
    public String getRoleDescription() {
        return "Registered Patient [Aadhar: " + aadharNumber + ", Age: " + getAge() + " yrs, Blood: " + bloodGroup + "]";
    }

    @Override
    public String toXmlFragment() {
        return "<patient id=\"" + patientId + "\">\n" +
               "    <name>" + escapeXml(getFullName()) + "</name>\n" +
               "    <phone>" + escapeXml(getPhone()) + "</phone>\n" +
               "    <dob>" + dateOfBirth + "</dob>\n" +
               "    <age>" + getAge() + "</age>\n" +
               "    <bloodGroup>" + escapeXml(bloodGroup) + "</bloodGroup>\n" +
               "    <aadhar>" + escapeXml(aadharNumber) + "</aadhar>\n" +
               "</patient>";
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getMedicalNotes() {
        return medicalNotes;
    }

    public void setMedicalNotes(String medicalNotes) {
        this.medicalNotes = medicalNotes;
    }
}
