package com.vaxicare.dao;

import com.vaxicare.exception.InsufficientGapException;
import com.vaxicare.exception.SlotUnavailableException;
import com.vaxicare.exception.VaxiCareException;
import com.vaxicare.model.Appointment;
import com.vaxicare.model.Vaccine;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Appointment Bookings and Dose Tracking.
 * Demonstrates: Transaction processing, ACID compliance, custom exception throwing,
 * and database relationship queries.
 */
public class AppointmentDAO {
    private final VaccineDAO vaccineDAO = new VaccineDAO();
    private final CentreDAO centreDAO = new CentreDAO();

    /**
     * Schedules a new vaccination appointment with rigorous validation and transactional rollback.
     */
    public int bookAppointment(Appointment appt) throws VaxiCareException {
        Vaccine vaccine = vaccineDAO.getVaccineById(appt.getVaccineId());
        if (vaccine == null) {
            throw new VaxiCareException("Invalid vaccine selected (ID " + appt.getVaccineId() + ")");
        }

        // 1. Check Previous Inoculation History
        List<Appointment> history = getAppointmentsByPatient(appt.getPatientId());
        LocalDate lastDoseDate = null;
        int completedDoses = 0;

        for (Appointment prev : history) {
            if (prev.getVaccineId() == appt.getVaccineId()) {
                if ("COMPLETED".equalsIgnoreCase(prev.getStatus()) || "SCHEDULED".equalsIgnoreCase(prev.getStatus())) {
                    completedDoses++;
                    if (lastDoseDate == null || prev.getAppointmentDate().isAfter(lastDoseDate)) {
                        lastDoseDate = prev.getAppointmentDate();
                    }
                }
            }
        }

        int targetDoseNumber = completedDoses + 1;
        appt.setDoseNumber(targetDoseNumber);

        // 2. Validate Dose Gap if scheduling subsequent dose
        if (lastDoseDate != null && targetDoseNumber > 1) {
            long daysElapsed = ChronoUnit.DAYS.between(lastDoseDate, appt.getAppointmentDate());
            if (daysElapsed < vaccine.getMinGapDays()) {
                throw new InsufficientGapException(vaccine.getName(), vaccine.getMinGapDays(), daysElapsed);
            }
        }

        // 3. Check Available Stock at Centre
        int currentStock = centreDAO.getAvailableStock(appt.getCentreId(), appt.getVaccineId());
        if (currentStock <= 0) {
            var centre = centreDAO.getCentreById(appt.getCentreId());
            String centreName = centre != null ? centre.getName() : "Centre #" + appt.getCentreId();
            throw new SlotUnavailableException(centreName, vaccine.getName());
        }

        // 4. Transactional Booking & Inventory Decrement
        String insertSql = "INSERT INTO appointments (patient_id, vaccine_id, centre_id, dose_number, appointment_date, status) " +
                           "VALUES (?, ?, ?, ?, ?, 'SCHEDULED')";

        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Begin transaction

            int generatedId = -1;
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, appt.getPatientId());
                ps.setInt(2, appt.getVaccineId());
                ps.setInt(3, appt.getCentreId());
                ps.setInt(4, targetDoseNumber);
                ps.setString(5, appt.getAppointmentDate().toString());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    }
                }
            }

            // Decrement stock in same transaction
            centreDAO.decrementStock(conn, appt.getCentreId(), appt.getVaccineId());

            conn.commit(); // Commit transaction
            appt.setAppointmentId(generatedId);
            return generatedId;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignored */ }
            }
            throw new VaxiCareException("Failed to confirm appointment booking: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* ignored */ }
            }
        }
    }

    public List<Appointment> getAppointmentsByPatient(int patientId) throws VaxiCareException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.appointment_id, a.patient_id, a.vaccine_id, a.centre_id, a.dose_number, " +
                     "a.appointment_date, a.status, a.booking_timestamp, v.name AS vaccine_name, c.name AS centre_name " +
                     "FROM appointments a " +
                     "JOIN vaccines v ON a.vaccine_id = v.vaccine_id " +
                     "JOIN centres c ON a.centre_id = c.centre_id " +
                     "WHERE a.patient_id = ? ORDER BY a.appointment_date DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Appointment a = new Appointment();
                    a.setAppointmentId(rs.getInt("appointment_id"));
                    a.setPatientId(rs.getInt("patient_id"));
                    a.setVaccineId(rs.getInt("vaccine_id"));
                    a.setCentreId(rs.getInt("centre_id"));
                    a.setDoseNumber(rs.getInt("dose_number"));
                    String dStr = rs.getString("appointment_date");
                    a.setAppointmentDate(dStr != null ? LocalDate.parse(dStr.substring(0, 10)) : LocalDate.now());
                    a.setStatus(rs.getString("status"));
                    a.setVaccineName(rs.getString("vaccine_name"));
                    a.setCentreName(rs.getString("centre_name"));
                    list.add(a);
                }
            }
        } catch (SQLException e) {
            throw new VaxiCareException("Failed to fetch appointment history for patient " + patientId, e);
        }
        return list;
    }

    public List<Appointment> getAllAppointments() throws VaxiCareException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.appointment_id, a.patient_id, a.vaccine_id, a.centre_id, a.dose_number, " +
                     "a.appointment_date, a.status, u.full_name AS patient_name, u.phone AS patient_phone, " +
                     "v.name AS vaccine_name, c.name AS centre_name " +
                     "FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.patient_id " +
                     "JOIN users u ON p.user_id = u.user_id " +
                     "JOIN vaccines v ON a.vaccine_id = v.vaccine_id " +
                     "JOIN centres c ON a.centre_id = c.centre_id " +
                     "ORDER BY a.appointment_date DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Appointment a = new Appointment();
                a.setAppointmentId(rs.getInt("appointment_id"));
                a.setPatientId(rs.getInt("patient_id"));
                a.setVaccineId(rs.getInt("vaccine_id"));
                a.setCentreId(rs.getInt("centre_id"));
                a.setDoseNumber(rs.getInt("dose_number"));
                String dStr = rs.getString("appointment_date");
                a.setAppointmentDate(dStr != null ? LocalDate.parse(dStr.substring(0, 10)) : LocalDate.now());
                a.setStatus(rs.getString("status"));
                a.setPatientName(rs.getString("patient_name"));
                a.setPatientPhone(rs.getString("patient_phone"));
                a.setVaccineName(rs.getString("vaccine_name"));
                a.setCentreName(rs.getString("centre_name"));
                list.add(a);
            }
        } catch (SQLException e) {
            throw new VaxiCareException("Failed to load appointment records: " + e.getMessage(), e);
        }
        return list;
    }

    public void updateStatus(int appointmentId, String status) throws VaxiCareException {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, appointmentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new VaxiCareException("Failed to update appointment status: " + e.getMessage(), e);
        }
    }
}
