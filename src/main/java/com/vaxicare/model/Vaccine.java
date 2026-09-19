package com.vaxicare.model;

import com.vaxicare.model.interfaces.Schedulable;
import com.vaxicare.model.interfaces.XmlExportable;
import java.time.LocalDate;

/**
 * Abstract Base Class for all clinical vaccine formulations.
 * Demonstrates: Abstraction, Interface Implementation, Polymorphism.
 */
public abstract class Vaccine implements Schedulable, XmlExportable {
    private int vaccineId;
    private String name;
    private String manufacturer;
    private String platformType;
    private int dosesRequired;
    private int minGapDays;
    private int approvedAgeMin;
    private double efficacyRate;
    private String storageTemp;
    private String description;

    public Vaccine() {}

    public Vaccine(int vaccineId, String name, String manufacturer, String platformType,
                   int dosesRequired, int minGapDays, int approvedAgeMin,
                   double efficacyRate, String storageTemp, String description) {
        this.vaccineId = vaccineId;
        this.name = name;
        this.manufacturer = manufacturer;
        this.platformType = platformType;
        this.dosesRequired = dosesRequired;
        this.minGapDays = minGapDays;
        this.approvedAgeMin = approvedAgeMin;
        this.efficacyRate = efficacyRate;
        this.storageTemp = storageTemp;
        this.description = description;
    }

    /**
     * Abstract method enforcing specialized cold-chain and clinical handling.
     * Demonstrates dynamic method dispatch across vaccine types.
     */
    public abstract String getColdChainHandlingProtocol();

    @Override
    public boolean isEligibleForDose(int currentDose, long daysSinceLastDose) {
        if (currentDose <= 1) return true;
        return daysSinceLastDose >= this.minGapDays;
    }

    @Override
    public LocalDate calculateNextDoseDate(LocalDate lastDoseDate, int completedDose) {
        if (completedDose >= this.dosesRequired) {
            return null; // All scheduled doses completed
        }
        return lastDoseDate.plusDays(this.minGapDays);
    }

    @Override
    public String toXmlFragment() {
        return "<vaccine id=\"" + vaccineId + "\">\n" +
               "    <name>" + escapeXml(name) + "</name>\n" +
               "    <manufacturer>" + escapeXml(manufacturer) + "</manufacturer>\n" +
               "    <platform>" + escapeXml(platformType) + "</platform>\n" +
               "    <dosesRequired>" + dosesRequired + "</dosesRequired>\n" +
               "    <minIntervalDays>" + minGapDays + "</minIntervalDays>\n" +
               "    <efficacyRate>" + efficacyRate + "</efficacyRate>\n" +
               "    <storageTemp>" + escapeXml(storageTemp) + "</storageTemp>\n" +
               "    <protocol>" + escapeXml(getColdChainHandlingProtocol()) + "</protocol>\n" +
               "</vaccine>";
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // Getters and Setters
    public int getVaccineId() { return vaccineId; }
    public void setVaccineId(int vaccineId) { this.vaccineId = vaccineId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getPlatformType() { return platformType; }
    public void setPlatformType(String platformType) { this.platformType = platformType; }

    public int getDosesRequired() { return dosesRequired; }
    public void setDosesRequired(int dosesRequired) { this.dosesRequired = dosesRequired; }

    public int getMinGapDays() { return minGapDays; }
    public void setMinGapDays(int minGapDays) { this.minGapDays = minGapDays; }

    public int getApprovedAgeMin() { return approvedAgeMin; }
    public void setApprovedAgeMin(int approvedAgeMin) { this.approvedAgeMin = approvedAgeMin; }

    public double getEfficacyRate() { return efficacyRate; }
    public void setEfficacyRate(double efficacyRate) { this.efficacyRate = efficacyRate; }

    public String getStorageTemp() { return storageTemp; }
    public void setStorageTemp(String storageTemp) { this.storageTemp = storageTemp; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
