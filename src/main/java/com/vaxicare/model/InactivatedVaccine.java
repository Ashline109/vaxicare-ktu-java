package com.vaxicare.model;

/**
 * Concrete Inactivated Virion formulation (e.g. Covaxin / Whole-virion).
 * Demonstrates: Polymorphic method overriding.
 */
public class InactivatedVaccine extends Vaccine {
    private String adjuvantName;

    public InactivatedVaccine() {
        super();
        setPlatformType("Inactivated Virion");
    }

    public InactivatedVaccine(int vaccineId, String name, String manufacturer,
                              int dosesRequired, int minGapDays, int approvedAgeMin,
                              double efficacyRate, String storageTemp, String description,
                              String adjuvantName) {
        super(vaccineId, name, manufacturer, "Inactivated Virion", dosesRequired, minGapDays,
              approvedAgeMin, efficacyRate, storageTemp, description);
        this.adjuvantName = adjuvantName;
    }

    @Override
    public String getColdChainHandlingProtocol() {
        return "Inactivated Virion Protocol: Store strictly at +2°C to +8°C with Alhydrogel adjuvant stabilizer. Gently swirl before multi-dose vial aspiration.";
    }

    public String getAdjuvantName() {
        return adjuvantName;
    }

    public void setAdjuvantName(String adjuvantName) {
        this.adjuvantName = adjuvantName;
    }
}
