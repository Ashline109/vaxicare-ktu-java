package com.vaxicare.dao;

import com.vaxicare.exception.VaxiCareException;
import com.vaxicare.model.AefiReport;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Adverse Events Following Immunization (AEFI).
 * Handles logging and emergency queue retrieval.
 */
public class AefiDAO {

    public int recordAefiReport(AefiReport report) throws VaxiCareException {
        String sql = "INSERT INTO aefi_reports (patient_id, vaccine_id, severity, symptoms, callback_phone, triage_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, report.getPatientId());
            ps.setInt(2, report.getVaccineId() > 0 ? report.getVaccineId() : 1);
            ps.setString(3, report.getSeverity());
            ps.setString(4, report.getSymptoms());
            ps.setString(5, report.getCallbackPhone());
            ps.setString(6, "PENDING");
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    report.setReportId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new VaxiCareException("Failed to log AEFI report: " + e.getMessage(), e);
        }
        return -1;
    }

    public List<AefiReport> getAllPendingReports() throws VaxiCareException {
        List<AefiReport> list = new ArrayList<>();
        String sql = "SELECT r.report_id, r.patient_id, r.vaccine_id, r.severity, r.symptoms, " +
                     "r.callback_phone, r.triage_status, r.reported_at, r.triage_officer, " +
                     "u.full_name AS patient_name, v.name AS vaccine_name " +
                     "FROM aefi_reports r " +
                     "JOIN patients p ON r.patient_id = p.patient_id " +
                     "JOIN users u ON p.user_id = u.user_id " +
                     "JOIN vaccines v ON r.vaccine_id = v.vaccine_id " +
                     "WHERE r.triage_status != 'RESOLVED' ORDER BY r.reported_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                AefiReport report = new AefiReport();
                report.setReportId(rs.getInt("report_id"));
                report.setPatientId(rs.getInt("patient_id"));
                report.setVaccineId(rs.getInt("vaccine_id"));
                report.setSeverity(rs.getString("severity"));
                report.setSymptoms(rs.getString("symptoms"));
                report.setCallbackPhone(rs.getString("callback_phone"));
                report.setTriageStatus(rs.getString("triage_status"));
                report.setTriageOfficer(rs.getString("triage_officer"));
                report.setPatientName(rs.getString("patient_name"));
                report.setVaccineName(rs.getString("vaccine_name"));
                list.add(report);
            }
        } catch (SQLException e) {
            throw new VaxiCareException("Failed to load pending AEFI reports: " + e.getMessage(), e);
        }
        return list;
    }

    public void updateTriageStatus(int reportId, String status, String officer) throws VaxiCareException {
        String sql = "UPDATE aefi_reports SET triage_status = ?, triage_officer = ? WHERE report_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, officer);
            ps.setInt(3, reportId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new VaxiCareException("Failed to update triage status: " + e.getMessage(), e);
        }
    }
}
