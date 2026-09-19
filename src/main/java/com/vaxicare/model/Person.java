package com.vaxicare.model;

/**
 * Abstract Base Class representing any human user within the VaxiCare clinical platform.
 * Demonstrates: Abstraction, Encapsulation, Base Class for Inheritance (KTU S3 syllabus).
 */
public abstract class Person {
    private int id;
    private String username;
    private String fullName;
    private String phone;
    private String role; // "PATIENT" or "ADMIN"

    // Default Constructor
    public Person() {}

    // Parameterized Constructor demonstrating super(...) initialization
    public Person(int id, String username, String fullName, String phone, String role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
    }

    // Abstract method to be implemented by child classes (Dynamic Method Dispatch)
    public abstract String getRoleDescription();

    // Getters and Setters with encapsulation validation
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username != null && !username.trim().isEmpty()) {
            this.username = username.trim();
        }
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            this.fullName = fullName.trim();
        }
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Person [ID=" + id + ", Name=" + fullName + ", Phone=" + phone + ", Role=" + role + "]";
    }
}
