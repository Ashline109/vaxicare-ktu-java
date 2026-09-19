package com.vaxicare.model.interfaces;

/**
 * Interface representing emergency clinical alerting & priority calculation.
 * Demonstrates: Interface design & integration with multithreaded priority triage.
 */
public interface Alertable {
    /**
     * Dispatches an emergency triage alert notification.
     * @param contactPhone Target emergency phone number.
     * @param alertMessage Clinical alert description.
     */
    void dispatchEmergencyAlert(String contactPhone, String alertMessage);

    /**
     * Calculates the numeric triage priority for priority queues.
     * @return Integer priority (1: Highest/Severe, 2: Moderate, 3: Mild)
     */
    int getTriagePriority();
}
