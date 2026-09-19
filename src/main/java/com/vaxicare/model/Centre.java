package com.vaxicare.model;

/**
 * Model representing a physical vaccination center or clinical emergency hub.
 * Encapsulates geographic coordinates and operating parameters.
 */
public class Centre {
    private int centreId;
    private String name;
    private String address;
    private String district;
    private String phone;
    private String operatingHours;
    private boolean emergencyHub;
    private double latitude;
    private double longitude;

    public Centre() {}

    public Centre(int centreId, String name, String address, String district, 
                  String phone, String operatingHours, boolean emergencyHub, 
                  double latitude, double longitude) {
        this.centreId = centreId;
        this.name = name;
        this.address = address;
        this.district = district;
        this.phone = phone;
        this.operatingHours = operatingHours;
        this.emergencyHub = emergencyHub;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getCentreId() { return centreId; }
    public void setCentreId(int centreId) { this.centreId = centreId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getOperatingHours() { return operatingHours; }
    public void setOperatingHours(String operatingHours) { this.operatingHours = operatingHours; }

    public boolean isEmergencyHub() { return emergencyHub; }
    public void setEmergencyHub(boolean emergencyHub) { this.emergencyHub = emergencyHub; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
