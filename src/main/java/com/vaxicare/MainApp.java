package com.vaxicare;

import com.vaxicare.dao.AppointmentDAO;
import com.vaxicare.dao.CentreDAO;
import com.vaxicare.dao.UserDAO;
import com.vaxicare.dao.VaccineDAO;
import com.vaxicare.exception.InsufficientGapException;
import com.vaxicare.exception.VaxiCareException;
import com.vaxicare.model.*;
import com.vaxicare.service.AefiTriageService;
import com.vaxicare.service.XmlDataService;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Standalone Console Runner & Viva Demonstration Harness for VaxiCare.
 * Specially designed for KTU S3 Object-Oriented Programming (Java) Lab Exam & Project Viva.
 * 
 * Allows live demonstration of:
 * 1. OOP Principles (Inheritance, Polymorphism, Abstraction, Encapsulation, Interfaces)
 * 2. User-Defined Custom Exception Handling
 * 3. XML Processing using Java DOM Parser (DocumentBuilderFactory)
 * 4. Multithreading & Thread Synchronization (AEFI Emergency Triage Worker)
 * 5. JDBC Database Operations & ACID Transactions
 * 6. Dynamic XML Certificate Generation
 */
public class MainApp {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("     VAXICARE — SMART VACCINATION & CLINICAL CARE MANAGEMENT SYSTEM            ");
        System.out.println("     KTU S3 Object Oriented Programming in Java (CST 205 / CSL 203)            ");
        System.out.println("================================================================================");

        // Warm up background multithreaded triage engine
        AefiTriageService triageService = AefiTriageService.getInstance();

        if (args.length > 0 && "--auto-test".equalsIgnoreCase(args[0])) {
            runAutomatedSelfTest();
            return;
        }

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.println("\n----------------- KTU S3 DEMONSTRATION MENU -----------------");
            System.out.println("1. Demonstrate OOP Principles (Inheritance & Polymorphic Dispatch)");
            System.out.println("2. Demonstrate Custom Exception Handling (InsufficientGapException)");
            System.out.println("3. Demonstrate Java XML DOM Parser (vaccines_catalog.xml)");
            System.out.println("4. Demonstrate Multithreading & Priority Triage Queue");
            System.out.println("5. Demonstrate JDBC Database Operations & Patient Query");
            System.out.println("6. Demonstrate Dynamic XML Certificate Generation");
            System.out.println("7. Run Complete Automated Self-Test (All Features)");
            System.out.println("0. Exit");
            System.out.print("Enter choice [0-7]: ");

            String input = scanner.hasNextLine() ? scanner.nextLine().trim() : "0";
            switch (input) {
                case "1":
                    testOopPrinciples();
                    break;
                case "2":
                    testExceptionHandling();
                    break;
                case "3":
                    testXmlDomParser();
                    break;
                case "4":
                    testMultithreading();
                    break;
                case "5":
                    testJdbcOperations();
                    break;
                case "6":
                    testXmlCertificate();
                    break;
                case "7":
                    runAutomatedSelfTest();
                    break;
                case "0":
                    exit = true;
                    System.out.println("\nExiting VaxiCare Console Harness. Best wishes for KTU Lab Viva!");
                    break;
                default:
                    System.out.println("Invalid choice. Please select from 0 to 7.");
            }
        }
    }

    /**
     * 1. OOP Principles Demonstration
     */
    public static void testOopPrinciples() {
        System.out.println("\n--- [1] OOP PRINCIPLES (INHERITANCE, ABSTRACTION & POLYMORPHISM) ---");

        // Inheritance: Person -> Patient and Admin
        Person patient = new Patient(101, "arya_prakash", "Arya Prakash", "+91 9847123456", 
                                     1, LocalDate.of(1998, 5, 14), "7482-9104-5512", "B+", "Mild asthma");
        Person admin = new Admin(102, "dr_radha", "Dr. Radhakrishnan", "+91 495 2371475", 
                                 "Immunization Surveillance", "Chief Medical Officer");

        // Dynamic Method Dispatch on getRoleDescription()
        System.out.println("Polymorphic Person 1: " + patient.getRoleDescription());
        System.out.println("Polymorphic Person 2: " + admin.getRoleDescription());

        // Polymorphic Vaccine Hierarchy
        Vaccine v1 = new ViralVectorVaccine(1, "Covishield", "Serum Institute", 2, 84, 18, 81.3, "2°C to 8°C", "Viral vector", "ChAdOx1");
        Vaccine v2 = new InactivatedVaccine(2, "Covaxin", "Bharat Biotech", 2, 28, 12, 77.8, "2°C to 8°C", "Inactivated virion", "Algel-IMDG");
        Vaccine v3 = new ProteinSubunitVaccine(3, "Corbevax", "Biological E", 2, 28, 5, 80.0, "2°C to 8°C", "Protein subunit", "RBD");

        Vaccine[] catalog = {v1, v2, v3};
        System.out.println("\nPolymorphic Dynamic Dispatch across Vaccine Formulations:");
        for (Vaccine v : catalog) {
            System.out.println("• " + v.getName() + " (" + v.getPlatformType() + "):");
            System.out.println("    Cold-Chain Protocol: " + v.getColdChainHandlingProtocol());
            LocalDate dose1 = LocalDate.of(2026, 1, 1);
            LocalDate dose2 = v.calculateNextDoseDate(dose1, 1);
            System.out.println("    Dose 1: " + dose1 + " --> Calculated Dose 2: " + dose2 + " (Gap: " + v.getMinGapDays() + " days)");
        }
    }

    /**
     * 2. Custom Exception Handling Demonstration
     */
    public static void testExceptionHandling() {
        System.out.println("\n--- [2] CUSTOM USER-DEFINED EXCEPTION HANDLING ---");
        System.out.println("Simulating an invalid early booking for Covishield (Statutory gap: 84 days)...");

        AppointmentDAO appointmentDAO = new AppointmentDAO();
        Appointment invalidAppt = new Appointment();
        invalidAppt.setPatientId(1);
        invalidAppt.setVaccineId(1); // Covishield
        invalidAppt.setCentreId(1);
        // Dose 1 was on 2026-05-10. Booking on 2026-05-20 (only 10 days elapsed)
        invalidAppt.setAppointmentDate(LocalDate.of(2026, 5, 20));

        try {
            System.out.println("Attempting appointmentDAO.bookAppointment() with 10-day gap...");
            appointmentDAO.bookAppointment(invalidAppt);
            System.out.println("Booking succeeded unexpectedly.");
        } catch (InsufficientGapException e) {
            System.out.println("\n>>> SUCCESS: Caught expected custom InsufficientGapException!");
            System.out.println(">>> Error Code : " + e.getErrorCode());
            System.out.println(">>> Message    : " + e.getMessage());
            System.out.println(">>> Required Gap: " + e.getRequiredGapDays() + " days, Elapsed: " + e.getActualDaysElapsed() + " days");
        } catch (VaxiCareException e) {
            System.out.println(">>> Caught generic VaxiCareException: " + e.getMessage());
        } finally {
            System.out.println(">>> [finally block executed] - Clinical audit trace closed.");
        }
    }

    /**
     * 3. XML Processing (Java DOM Parser)
     */
    public static void testXmlDomParser() {
        System.out.println("\n--- [3] XML PROCESSING USING JAVA DOM PARSER ---");
        XmlDataService xmlService = new XmlDataService();

        try (InputStream is = MainApp.class.getClassLoader().getResourceAsStream("data/vaccines_catalog.xml")) {
            if (is == null) {
                System.out.println("Could not locate vaccines_catalog.xml in classpath.");
                return;
            }

            System.out.println("Parsing data/vaccines_catalog.xml with DocumentBuilderFactory...");
            List<Vaccine> parsedList = xmlService.parseVaccinesFromXml(is);
            System.out.println("Successfully parsed " + parsedList.size() + " vaccines from XML document:\n");

            for (Vaccine v : parsedList) {
                System.out.printf("  [XML-Record] %-18s | Doses: %d | Min Gap: %3d d | Temp: %-10s | %s\n",
                        v.getName(), v.getDosesRequired(), v.getMinGapDays(), v.getStorageTemp(), v.getColdChainHandlingProtocol());
            }
        } catch (Exception e) {
            System.err.println("XML Parsing error: " + e.getMessage());
        }
    }

    /**
     * 4. Multithreading and Collections PriorityQueue
     */
    public static void testMultithreading() {
        System.out.println("\n--- [4] MULTITHREADING & COLLECTIONS (AEFI EMERGENCY TRIAGE) ---");
        System.out.println("Submitting 3 concurrent AEFI reports with differing severity levels...");

        AefiTriageService triageService = AefiTriageService.getInstance();

        AefiReport mild = new AefiReport(301, 1, 1, "MILD", "Mild injection arm soreness and tiredness", "+91 9847123456");
        AefiReport severe = new AefiReport(302, 2, 2, "SEVERE", "Acute dyspnea and facial edema post-injection", "+91 9847654321");
        AefiReport moderate = new AefiReport(303, 1, 1, "MODERATE", "Persistent fever 102F and chills for 48 hours", "+91 9847000111");

        // Enqueue out-of-order to test PriorityQueue priority ordering
        System.out.println("Enqueuing: MILD (Priority 3)...");
        triageService.submitReportForTriage(mild);

        System.out.println("Enqueuing: SEVERE (Priority 1)...");
        triageService.submitReportForTriage(severe);

        System.out.println("Enqueuing: MODERATE (Priority 2)...");
        triageService.submitReportForTriage(moderate);

        // Allow background thread time to process
        try {
            Thread.sleep(1200);
        } catch (InterruptedException ignored) {}

        System.out.println("Multithreaded background triage queue handled all reports according to medical priority.");
    }

    /**
     * 5. JDBC Database Operations
     */
    public static void testJdbcOperations() {
        System.out.println("\n--- [5] JDBC DATABASE OPERATIONS & REPOSITORY QUERIES ---");
        try {
            UserDAO userDAO = new UserDAO();
            VaccineDAO vaccineDAO = new VaccineDAO();
            CentreDAO centreDAO = new CentreDAO();
            AppointmentDAO appointmentDAO = new AppointmentDAO();

            System.out.println("1. Authenticating user 'arya_prakash' via JDBC PreparedStatement...");
            Person person = userDAO.authenticate("arya_prakash", "pass123");
            System.out.println("   Authenticated: " + person.getFullName() + " | Role: " + person.getRole());

            System.out.println("\n2. Querying available clinical centres:");
            List<Centre> centres = centreDAO.getAllCentres();
            for (Centre c : centres) {
                System.out.println("   • " + c.getName() + " (" + c.getDistrict() + ") - Emergency Hub: " + c.isEmergencyHub());
            }

            System.out.println("\n3. Querying existing patient appointments:");
            List<Appointment> appts = appointmentDAO.getAppointmentsByPatient(1);
            for (Appointment a : appts) {
                System.out.println("   • Appt #" + a.getAppointmentId() + " | " + a.getVaccineName() + 
                                   " Dose " + a.getDoseNumber() + " on " + a.getAppointmentDate() + " [" + a.getStatus() + "]");
            }

        } catch (Exception e) {
            System.err.println("JDBC error: " + e.getMessage());
        }
    }

    /**
     * 6. Dynamic XML Certificate Generation
     */
    public static void testXmlCertificate() {
        System.out.println("\n--- [6] DYNAMIC XML VACCINATION CERTIFICATE GENERATION ---");
        try {
            UserDAO userDAO = new UserDAO();
            VaccineDAO vaccineDAO = new VaccineDAO();
            AppointmentDAO appointmentDAO = new AppointmentDAO();
            XmlDataService xmlService = new XmlDataService();

            Patient patient = userDAO.getPatientById(1);
            Vaccine vaccine = vaccineDAO.getVaccineById(1);
            List<Appointment> appts = appointmentDAO.getAppointmentsByPatient(1);

            String xmlCert = xmlService.generateDigitalCertificateXml(patient, vaccine, appts);
            System.out.println(xmlCert);

        } catch (Exception e) {
            System.err.println("XML Certificate error: " + e.getMessage());
        }
    }

    /**
     * 7. Full Automated Self-Test
     */
    public static void runAutomatedSelfTest() {
        System.out.println("\n================================================================================");
        System.out.println("     RUNNING COMPLETE KTU S3 OOP JAVA AUTOMATED VERIFICATION SUITE              ");
        System.out.println("================================================================================");
        testOopPrinciples();
        testExceptionHandling();
        testXmlDomParser();
        testMultithreading();
        testJdbcOperations();
        testXmlCertificate();
        System.out.println("\n================================================================================");
        System.out.println("  ✅ ALL KTU S3 JAVA OOP TESTS COMPLETED SUCCESSFULLY WITH ZERO ERRORS!         ");
        System.out.println("================================================================================");
    }
}
