package com.vaxicare.model;

/**
 * Admin / Medical Nodal Officer class inheriting from Person.
 * Demonstrates: Hierarchical inheritance and polymorphism.
 */
public class Admin extends Person {
    private String department;
    private String designation;

    public Admin() {
        super();
    }

    public Admin(int userId, String username, String fullName, String phone, 
                 String department, String designation) {
        super(userId, username, fullName, phone, "ADMIN");
        this.department = department;
        this.designation = designation;
    }

    @Override
    public String getRoleDescription() {
        return "Clinical Administrator [" + designation + " - " + department + "]";
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }
}
