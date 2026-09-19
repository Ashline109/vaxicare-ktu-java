package com.vaxicare.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Singleton Database Connection Manager for VaxiCare.
 * Demonstrates: Singleton Design Pattern, JDBC connection lifecycle,
 * Resource management, and automatic offline SQLite/Memory fallback for KTU Lab Viva.
 */
public class DBConnection {
    private static DBConnection instance;
    private static Properties props = new Properties();
    private static boolean useSqliteFallback = false;
    private static boolean sqliteInitialized = false;

    static {
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                // Default fallback configuration
                props.setProperty("db.type", "mysql");
                props.setProperty("db.mysql.driver", "com.mysql.cj.jdbc.Driver");
                props.setProperty("db.mysql.url", "jdbc:mysql://localhost:3306/vaxicare_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
                props.setProperty("db.mysql.user", "root");
                props.setProperty("db.mysql.password", "root");
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not read db.properties, using fallback defaults: " + e.getMessage());
        }
    }

    private DBConnection() {
        // Private constructor for Singleton pattern
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Obtains an active JDBC Connection.
     * Tries MySQL first. If connection fails, automatically switches to SQLite fallback.
     */
    public Connection getConnection() throws SQLException {
        if (!useSqliteFallback) {
            try {
                String driver = props.getProperty("db.mysql.driver", "com.mysql.cj.jdbc.Driver");
                Class.forName(driver);
                String url = props.getProperty("db.mysql.url");
                String user = props.getProperty("db.mysql.user", "root");
                String pass = props.getProperty("db.mysql.password", "root");
                return DriverManager.getConnection(url, user, pass);
            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("Notice: MySQL connection unavailable (" + e.getMessage() + "). Switching to SQLite/Offline Mode.");
                useSqliteFallback = true;
            }
        }

        // SQLite / Offline Mode for easy demonstration without database server setup
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:vaxicare_local.db");
            if (!sqliteInitialized) {
                initializeSqliteSchema(conn);
                sqliteInitialized = true;
            }
            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    /**
     * Initializes SQLite schema with initial test data when MySQL server is not running.
     */
    private synchronized void initializeSqliteSchema(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE, password TEXT, full_name TEXT, phone TEXT, role TEXT, created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS patients (" +
                    "patient_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER UNIQUE, dob TEXT, aadhar_no TEXT UNIQUE, blood_group TEXT, medical_notes TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS vaccines (" +
                    "vaccine_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE, manufacturer TEXT, platform_type TEXT, doses_required INTEGER, min_gap_days INTEGER, approved_age_min INTEGER, efficacy_rate REAL, storage_temp TEXT, description TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS centres (" +
                    "centre_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, address TEXT, district TEXT, phone TEXT, operating_hours TEXT, is_emergency_hub INTEGER, latitude REAL, longitude REAL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS vaccine_inventory (" +
                    "inventory_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "centre_id INTEGER, vaccine_id INTEGER, available_doses INTEGER, last_updated DATETIME DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS appointments (" +
                    "appointment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id INTEGER, vaccine_id INTEGER, centre_id INTEGER, dose_number INTEGER, appointment_date TEXT, status TEXT, booking_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS aefi_reports (" +
                    "report_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id INTEGER, vaccine_id INTEGER, severity TEXT, symptoms TEXT, callback_phone TEXT, triage_status TEXT, reported_at DATETIME DEFAULT CURRENT_TIMESTAMP, triage_officer TEXT)");

            // Seed initial records if users table is empty
            var rs = stmt.executeQuery("SELECT count(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO users (username, password, full_name, phone, role) VALUES " +
                        "('arya_prakash', 'pass123', 'Arya Prakash', '+91 9847123456', 'PATIENT'), " +
                        "('admin', 'admin123', 'Dr. Radhakrishnan (Nodal Officer)', '+91 495 2371475', 'ADMIN')");

                stmt.execute("INSERT INTO patients (user_id, dob, aadhar_no, blood_group, medical_notes) VALUES " +
                        "(1, '1998-05-14', '7482-9104-5512', 'B+', 'No chronic complications')");

                stmt.execute("INSERT INTO vaccines (name, manufacturer, platform_type, doses_required, min_gap_days, approved_age_min, efficacy_rate, storage_temp, description) VALUES " +
                        "('Covishield', 'Serum Institute of India (AstraZeneca)', 'Viral Vector', 2, 84, 18, 81.3, '2°C to 8°C', 'Recombinant viral vector platform'), " +
                        "('Covaxin', 'Bharat Biotech / ICMR', 'Inactivated Virion', 2, 28, 12, 77.8, '2°C to 8°C', 'Whole-virion inactivated Vero-cell platform'), " +
                        "('Corbevax', 'Biological E. Limited', 'Protein Subunit', 2, 28, 5, 80.0, '2°C to 8°C', 'Protein subunit vaccine'), " +
                        "('Hepatitis B', 'GlaxoSmithKline', 'Recombinant', 3, 30, 0, 95.0, '2°C to 8°C', 'Hepatitis B immunization'), " +
                        "('MMR', 'Serum Institute / Merck', 'Live Attenuated', 2, 90, 1, 97.0, '2°C to 8°C', 'Measles, Mumps, Rubella'), " +
                        "('Influenza Flu Shot', 'Abbott / Sanofi', 'Inactivated Split', 1, 365, 1, 60.0, '2°C to 8°C', 'Seasonal flu vaccine')");

                stmt.execute("INSERT INTO centres (name, address, district, phone, operating_hours, is_emergency_hub, latitude, longitude) VALUES " +
                        "('District General Hospital', 'Mananchira, Kozhikode, Kerala - 673001', 'Kozhikode', '+91 495 2720250', '09:00 AM – 04:30 PM', 0, 11.2530, 75.7780), " +
                        "('Govt. Medical College Immunization Hub', 'Medical College PO, Kozhikode - 673008', 'Kozhikode', '+91 495 2355331', '08:30 AM – 06:00 PM', 1, 11.2723, 75.8360), " +
                        "('Cheruthuruthy Primary Health Centre', 'Cheruthuruthy, Thrissur - 679531', 'Thrissur', '+91 4884 262330', '09:00 AM – 02:00 PM', 0, 10.7485, 76.2810), " +
                        "('Aster MIMS Vaccination Clinic', 'Govindapuram, Kozhikode - 673016', 'Kozhikode', '+91 495 2488000', '08:00 AM – 08:00 PM', 0, 11.2460, 75.8010)");

                stmt.execute("INSERT INTO vaccine_inventory (centre_id, vaccine_id, available_doses) VALUES " +
                        "(1, 1, 150), (1, 2, 80), (2, 1, 300), (2, 2, 200), (3, 2, 45), (4, 1, 100)");

                stmt.execute("INSERT INTO appointments (patient_id, vaccine_id, centre_id, dose_number, appointment_date, status) VALUES " +
                        "(1, 1, 1, 1, '2026-05-10', 'COMPLETED'), (1, 1, 1, 2, '2026-08-15', 'SCHEDULED')");
            }
        } catch (SQLException e) {
            System.err.println("Failed to initialize SQLite fallback: " + e.getMessage());
        }
    }
}
