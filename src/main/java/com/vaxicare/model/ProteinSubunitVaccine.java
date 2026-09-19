package com.vaxicare.model;

/**
 * Concrete Protein Sub-unit formulation (e.g. Corbevax).
 * Demonstrates: Polymorphism in medical vaccine classes.
 */
public class ProteinSubunitVaccine extends Vaccine {
    private String antigenTarget;

    public ProteinSubunitVaccine() {
        super();
        setPlatformType("Protein Subunit");
    }

    public ProteinSubunitVaccine(int vaccineId, String name, String manufacturer,
                                 int dosesRequired, int minGapDays, int approvedAgeMin,
                                 double efficacyRate, String storageTemp, String description,
                                 String antigenTarget) {
        super(vaccineId, name, manufacturer, "Protein Subunit", dosesRequired, minGapDays,
              approvedAgeMin, efficacyRate, storageTemp, description);
        this.antigenTarget = antigenTarget;
    }

    @Override
    public String getColdChainHandlingProtocol() {
        return "Protein Subunit Protocol: Keep at +2°C to +8°C. Stable formulation suitable for regional pediatric camps. Inspect visually for particulates.";
    }

    public String getAntigenTarget() {
        return antigenTarget;
    }

    public void setAntigenTarget(String antigenTarget) {
        this.antigenTarget = antigenTarget;
    }
}
