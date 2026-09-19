package com.vaxicare.service;

import com.vaxicare.model.AefiReport;
import java.util.PriorityQueue;

/**
 * Service managing adverse event clinical triage using Multithreading and Priority Queues.
 * Demonstrates: Multithreading (Runnable, Thread, Daemon), Collections (PriorityQueue),
 * and Thread Synchronization (synchronized, wait, notify) for KTU S3 Java OOP syllabus.
 */
public class AefiTriageService {
    private static AefiTriageService instance;
    private final PriorityQueue<AefiReport> triageQueue = new PriorityQueue<>();
    private final Thread workerThread;
    private volatile boolean running = true;

    private AefiTriageService() {
        // Initialize background daemon worker thread
        workerThread = new Thread(new TriageWorker(), "AEFI-Triage-Worker");
        workerThread.setDaemon(true); // Daemon thread terminates when main app exits
        workerThread.start();
    }

    public static synchronized AefiTriageService getInstance() {
        if (instance == null) {
            instance = new AefiTriageService();
        }
        return instance;
    }

    /**
     * Enqueues an AEFI report into the synchronized PriorityQueue and wakes up the triage worker.
     */
    public void submitReportForTriage(AefiReport report) {
        synchronized (triageQueue) {
            triageQueue.offer(report);
            System.out.println("[TriageService] Enqueued report ID " + report.getReportId() + 
                               " with Severity: " + report.getSeverity() + " (Priority " + report.getTriagePriority() + ")");
            triageQueue.notify(); // Wake up worker thread
        }
    }

    public int getPendingQueueSize() {
        synchronized (triageQueue) {
            return triageQueue.size();
        }
    }

    public void stopWorker() {
        running = false;
        synchronized (triageQueue) {
            triageQueue.notifyAll();
        }
    }

    /**
     * Background Worker executing multi-threaded triage processing.
     */
    private class TriageWorker implements Runnable {
        @Override
        public void run() {
            System.out.println("[AEFI-Triage-Worker] Background emergency monitoring thread initiated.");
            while (running) {
                AefiReport currentReport = null;

                synchronized (triageQueue) {
                    while (triageQueue.isEmpty() && running) {
                        try {
                            triageQueue.wait(); // Sleep until new report arrives
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }

                    if (!running) break;
                    currentReport = triageQueue.poll();
                }

                if (currentReport != null) {
                    processEmergencyTriage(currentReport);
                }
            }
        }

        private void processEmergencyTriage(AefiReport report) {
            try {
                // Simulate triage analysis time based on severity
                if ("SEVERE".equalsIgnoreCase(report.getSeverity())) {
                    report.dispatchEmergencyAlert(
                        report.getCallbackPhone(),
                        "CRITICAL: Immediate medical officer dispatch required for " + 
                        (report.getPatientName() != null ? report.getPatientName() : "Patient") + 
                        ". Symptoms: " + report.getSymptoms()
                    );
                    Thread.sleep(500); // Fast clinical intervention
                } else {
                    System.out.println("[AEFI-Triage-Worker] Logged routine post-vaccine symptoms for review: " + 
                                       report.getSymptoms() + " (Callback: " + report.getCallbackPhone() + ")");
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
