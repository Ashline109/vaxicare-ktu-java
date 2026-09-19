package com.vaxicare.dao;

import com.vaxicare.exception.VaxiCareException;
import com.vaxicare.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Vaccines.
 * Demonstrates: Factory Method pattern with OOP Polymorphism to instantiate
 * specialized Vaccine subclasses (ViralVectorVaccine, InactivatedVaccine, ProteinSubunitVaccine).
 */
public class VaccineDAO {

    public List<Vaccine> getAllVaccines() throws VaxiCareException {
        List<Vaccine> list = new ArrayList<>();
        String sql = "SELECT vaccine_id, name, manufacturer, platform_type, doses_required, " +
                     "min_gap_days, approved_age_min, efficacy_rate, storage_temp, description FROM vaccines";

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToVaccine(rs));
            }
        } catch (SQLException e) {
            throw new VaxiCareException("Failed to fetch vaccines: " + e.getMessage(), e);
        }
        return list;
    }

    public Vaccine getVaccineById(int vaccineId) throws VaxiCareException {
        String sql = "SELECT vaccine_id, name, manufacturer, platform_type, doses_required, " +
                     "min_gap_days, approved_age_min, efficacy_rate, storage_temp, description FROM vaccines WHERE vaccine_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, vaccineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVaccine(rs);
                }
            }
        } catch (SQLException e) {
            throw new VaxiCareException("Failed to fetch vaccine with ID " + vaccineId, e);
        }
        return null;
    }

    public Vaccine getVaccineByName(String name) throws VaxiCareException {
        String sql = "SELECT vaccine_id, name, manufacturer, platform_type, doses_required, " +
                     "min_gap_days, approved_age_min, efficacy_rate, storage_temp, description FROM vaccines WHERE LOWER(name) LIKE ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVaccine(rs);
                }
            }
        } catch (SQLException e) {
            throw new VaxiCareException("Failed to fetch vaccine: " + name, e);
        }
        return null;
    }

    /**
     * Instantiates appropriate polymorphic subclass based on platform_type.
     */
    private Vaccine mapResultSetToVaccine(ResultSet rs) throws SQLException {
        int id = rs.getInt("vaccine_id");
        String name = rs.getString("name");
        String manufacturer = rs.getString("manufacturer");
        String platform = rs.getString("platform_type");
        int doses = rs.getInt("doses_required");
        int minGap = rs.getInt("min_gap_days");
        int age = rs.getInt("approved_age_min");
        double efficacy = rs.getDouble("efficacy_rate");
        String storage = rs.getString("storage_temp");
        String desc = rs.getString("description");

        if (platform != null && platform.toLowerCase().contains("viral vector")) {
            return new ViralVectorVaccine(id, name, manufacturer, doses, minGap, age, efficacy, storage, desc, "ChAdOx1-S");
        } else if (platform != null && platform.toLowerCase().contains("inactivated")) {
            return new InactivatedVaccine(id, name, manufacturer, doses, minGap, age, efficacy, storage, desc, "Algel-IMDG Adjuvant");
        } else if (platform != null && platform.toLowerCase().contains("protein")) {
            return new ProteinSubunitVaccine(id, name, manufacturer, doses, minGap, age, efficacy, storage, desc, "Receptor Binding Domain");
        } else {
            // Anonymous class or general concrete subclass
            return new Vaccine(id, name, manufacturer, platform, doses, minGap, age, efficacy, storage, desc) {
                @Override
                public String getColdChainHandlingProtocol() {
                    return "General Clinical Protocol: Store at 2°C to 8°C. Do not freeze.";
                }
            };
        }
    }
}
