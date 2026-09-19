package com.vaxicare.dao;

import com.vaxicare.exception.AuthenticationException;
import com.vaxicare.exception.VaxiCareException;
import com.vaxicare.model.Admin;
import com.vaxicare.model.Patient;
import com.vaxicare.model.Person;

import java.sql.*;
import java.time.LocalDate;

/**
 * Data Access Object for user authentication and patient records.
 * Demonstrates: JDBC PreparedStatements, Transaction Management (commit/rollback),
 * and Polymorphic return types (Person -> Patient / Admin).
 */
public class UserDAO {

    /**
     * Authenticates credentials and returns a polymorphic Person object (Patient or Admin).
     */
    public Person authenticate(String username, String password) throws AuthenticationException {
        String sql = "SELECT user_id, username, password, full_name, phone, role FROM users WHERE username = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPass = rs.getString("password");
                    if (!storedPass.equals(password)) {
                        throw new AuthenticationException("Invalid password for user: " + username);
                    }

                    int userId = rs.getInt("user_id");
                    String fullName = rs.getString("full_name");
                    String phone = rs.getString("phone");
                    String role = rs.getString("role");

                    if ("ADMIN".equalsIgnoreCase(role)) {
                        return new Admin(userId, username, fullName, phone, "Immunization Surveillance", "Chief Medical Officer");
                    } else {
                        // Retrieve linked patient details
                        return getPatientByUserId(conn, userId, username, fullName, phone);
                    }
                } else {
                    throw new AuthenticationException("User not found: " + username);
                }
            }
        } catch (SQLException e) {
            throw new AuthenticationException("Database error during authentication: " + e.getMessage());
        }
    }

    private Patient getPatientByUserId(Connection conn, int userId, String username, String fullName, String phone) throws SQLException {
        String sql = "SELECT patient_id, dob, aadhar_no, blood_group, medical_notes FROM patients WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int patientId = rs.getInt("patient_id");
                    String dobStr = rs.getString("dob");
                    LocalDate dob = dobStr != null ? LocalDate.parse(dobStr.substring(0, 10)) : LocalDate.of(2000, 1, 1);
                    String aadhar = rs.getString("aadhar_no");
                    String bloodGroup = rs.getString("blood_group");
                    String notes = rs.getString("medical_notes");
                    return new Patient(userId, username, fullName, phone, patientId, dob, aadhar, bloodGroup, notes);
                }
            }
        }
        // Fallback default if patient row does not yet exist
        return new Patient(userId, username, fullName, phone, 0, LocalDate.of(1995, 1, 1), "N/A", "O+", "Standard Patient Profile");
    }

    public Patient getPatientById(int patientId) throws VaxiCareException {
        String sql = "SELECT p.patient_id, p.user_id, u.username, u.full_name, u.phone, p.dob, p.aadhar_no, p.blood_group, p.medical_notes " +
                     "FROM patients p JOIN users u ON p.user_id = u.user_id WHERE p.patient_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String username = rs.getString("username");
                    String fullName = rs.getString("full_name");
                    String phone = rs.getString("phone");
                    String dobStr = rs.getString("dob");
                    LocalDate dob = dobStr != null ? LocalDate.parse(dobStr.substring(0, 10)) : LocalDate.of(2000, 1, 1);
                    String aadhar = rs.getString("aadhar_no");
                    String bloodGroup = rs.getString("blood_group");
                    String notes = rs.getString("medical_notes");
                    return new Patient(userId, username, fullName, phone, patientId, dob, aadhar, bloodGroup, notes);
                }
            }
        } catch (SQLException e) {
            throw new VaxiCareException("Failed to fetch patient with ID: " + patientId, e);
        }
        return null;
    }

    /**
     * Registers a new Patient using JDBC Transaction (Atomicity).
     */
    public boolean registerPatient(Patient patient, String rawPassword) throws VaxiCareException {
        String userSql = "INSERT INTO users (username, password, full_name, phone, role) VALUES (?, ?, ?, ?, 'PATIENT')";
        String patientSql = "INSERT INTO patients (user_id, dob, aadhar_no, blood_group, medical_notes) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            int userId = -1;
            try (PreparedStatement psUser = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, patient.getUsername());
                psUser.setString(2, rawPassword);
                psUser.setString(3, patient.getFullName());
                psUser.setString(4, patient.getPhone());
                psUser.executeUpdate();

                try (ResultSet keys = psUser.getGeneratedKeys()) {
                    if (keys.next()) {
                        userId = keys.getInt(1);
                    }
                }
            }

            if (userId <= 0) {
                conn.rollback();
                throw new VaxiCareException("Failed to create user record during patient registration.");
            }

            try (PreparedStatement psPatient = conn.prepareStatement(patientSql, Statement.RETURN_GENERATED_KEYS)) {
                psPatient.setInt(1, userId);
                psPatient.setString(2, patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : "2000-01-01");
                psPatient.setString(3, patient.getAadharNumber() != null ? patient.getAadharNumber() : "TEMP-" + System.currentTimeMillis());
                psPatient.setString(4, patient.getBloodGroup() != null ? patient.getBloodGroup() : "O+");
                psPatient.setString(5, patient.getMedicalNotes() != null ? patient.getMedicalNotes() : "Registered online");
                psPatient.executeUpdate();

                try (ResultSet pKeys = psPatient.getGeneratedKeys()) {
                    if (pKeys.next()) {
                        patient.setPatientId(pKeys.getInt(1));
                    }
                }
            }

            conn.commit(); // Commit Transaction
            patient.setId(userId);
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignored */ }
            }
            throw new VaxiCareException("Registration failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* ignored */ }
            }
        }
    }
}
