package com.vaxicare.exception;

/**
 * Exception thrown when a vaccination centre has depleted inventory
 * or has reached maximum daily patient capacity.
 */
public class SlotUnavailableException extends VaxiCareException {
    private final String centreName;
    private final String vaccineName;

    public SlotUnavailableException(String centreName, String vaccineName) {
        super("No available inoculation slots or stock at " + centreName + " for vaccine: " + vaccineName, 
              "ERR_SLOT_UNAVAILABLE");
        this.centreName = centreName;
        this.vaccineName = vaccineName;
    }

    public String getCentreName() {
        return centreName;
    }

    public String getVaccineName() {
        return vaccineName;
    }
}
