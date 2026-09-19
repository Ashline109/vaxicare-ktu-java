package com.vaxicare.model;

/**
 * Concrete Viral Vector formulation (e.g. Covishield / ChAdOx1).
 * Demonstrates: Polymorphism, dynamic method dispatch for handling protocols.
 */
public class ViralVectorVaccine extends Vaccine {
    private String vectorVirus;

    public ViralVectorVaccine() {
        super();
        setPlatformType("Viral Vector");
    }

    public ViralVectorVaccine(int vaccineId, String name, String manufacturer, 
                              int dosesRequired, int minGapDays, int approvedAgeMin, 
                              double efficacyRate, String storageTemp, String description,
                              String vectorVirus) {
        super(vaccineId, name, manufacturer, "Viral Vector", dosesRequired, minGapDays, 
              approvedAgeMin, efficacyRate, storageTemp, description);
        this.vectorVirus = vectorVirus;
    }

    @Override
    public String getColdChainHandlingProtocol() {
        return "Viral Vector Protocol: Maintain steady +2°C to +8°C. Protect from direct illumination. Do NOT freeze; discard immediately if frozen.";
    }

    public String getVectorVirus() {
        return vectorVirus;
    }

    public void setVectorVirus(String vectorVirus) {
        this.vectorVirus = vectorVirus;
    }
}
