package com.vaxicare.dao;

import com.vaxicare.exception.VaxiCareException;
import com.vaxicare.model.Centre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Vaccination Centres and Geographic Hubs.
 */
public class CentreDAO {

    public List<Centre> getAllCentres() throws VaxiCareException {
        List<Centre> list = new ArrayList<>();
        String sql = "SELECT centre_id, name, address, district, phone, operating_hours, " +
                     "is_emergency_hub, latitude, longitude FROM centres";

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Centre(
                    rs.getInt("centre_id"),
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getString("district"),
                    rs.getString("phone"),
                    rs.getString("operating_hours"),
                    rs.getInt("is_emergency_hub") == 1,
                    rs.getDouble("latitude"),
                    rs.getDouble("longitude")
                ));
            }
        } catch (SQLException e) {
            throw new VaxiCareException("Failed to fetch centres: " + e.getMessage(), e);
        }
        return list;
    }

    public Centre getCentreById(int centreId) throws VaxiCareException {
        String sql = "SELECT centre_id, name, address, district, phone, operating_hours, " +
                     "is_emergency_hub, latitude, longitude FROM centres WHERE centre_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, centreId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Centre(
                        rs.getInt("centre_id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("district"),
                        rs.getString("phone"),
                        rs.getString("operating_hours"),
                        rs.getInt("is_emergency_hub") == 1,
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                    );
                }
            }
        } catch (SQLException e) {
            throw new VaxiCareException("Failed to fetch centre with ID " + centreId, e);
        }
        return null;
    }

    public Centre getCentreByName(String name) throws VaxiCareException {
        String sql = "SELECT centre_id, name, address, district, phone, operating_hours, " +
                     "is_emergency_hub, latitude, longitude FROM centres WHERE LOWER(name) LIKE ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Centre(
                        rs.getInt("centre_id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("district"),
                        rs.getString("phone"),
                        rs.getString("operating_hours"),
                        rs.getInt("is_emergency_hub") == 1,
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                    );
                }
            }
        } catch (SQLException e) {
            throw new VaxiCareException("Failed to fetch centre: " + name, e);
        }
        return null;
    }

    public int getAvailableStock(int centreId, int vaccineId) throws VaxiCareException {
        String sql = "SELECT available_doses FROM vaccine_inventory WHERE centre_id = ? AND vaccine_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, centreId);
            ps.setInt(2, vaccineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("available_doses");
                }
            }
        } catch (SQLException e) {
            throw new VaxiCareException("Failed to check vaccine inventory: " + e.getMessage(), e);
        }
        return 0; // Default zero stock if no row found
    }

    public void decrementStock(Connection conn, int centreId, int vaccineId) throws SQLException {
        String sql = "UPDATE vaccine_inventory SET available_doses = CASE WHEN available_doses > 0 THEN available_doses - 1 ELSE 0 END " +
                     "WHERE centre_id = ? AND vaccine_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, centreId);
            ps.setInt(2, vaccineId);
            ps.executeUpdate();
        }
    }
}
