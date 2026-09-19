-- ============================================================================
-- KTU S3 OOP JAVA MINI PROJECT DATABASE SCHEMA
-- Project: VaxiCare — Smart Vaccination & Clinical Care Management System
-- Database: MySQL (Compatible with MySQL 8.0+ / MariaDB / SQLite)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS vaxicare_db;
USE vaxicare_db;

-- Drop tables in reverse order of foreign key dependencies
DROP TABLE IF EXISTS aefi_reports;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS vaccine_inventory;
DROP TABLE IF EXISTS centres;
DROP TABLE IF EXISTS vaccines;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS users;

-- 1. Users Table (Core Authentication & System Users)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    role ENUM('PATIENT', 'ADMIN') NOT NULL DEFAULT 'PATIENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Patients Table (Extends Person entity in OOP with Patient-specific fields)
CREATE TABLE patients (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    dob DATE NOT NULL,
    aadhar_no VARCHAR(16) NOT NULL UNIQUE,
    blood_group VARCHAR(5) DEFAULT 'O+',
    medical_notes TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. Vaccines Table (Vaccine Entity hierarchy representation in RDBMS)
CREATE TABLE vaccines (
    vaccine_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    manufacturer VARCHAR(100) NOT NULL,
    platform_type VARCHAR(50) NOT NULL, -- Viral Vector, Inactivated, Protein Subunit, etc.
    doses_required INT NOT NULL DEFAULT 2,
    min_gap_days INT NOT NULL DEFAULT 28,
    approved_age_min INT NOT NULL DEFAULT 18,
    efficacy_rate DECIMAL(4,1) NOT NULL,
    storage_temp VARCHAR(30) NOT NULL,
    description TEXT
);

-- 4. Centres Table (Designated Vaccination Hubs and Emergency Facilities)
CREATE TABLE centres (
    centre_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    district VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    operating_hours VARCHAR(100) NOT NULL,
    is_emergency_hub BOOLEAN DEFAULT FALSE,
    latitude DECIMAL(9, 6) NOT NULL,
    longitude DECIMAL(9, 6) NOT NULL
);

-- 5. Vaccine Inventory (Real-Time Stock per Centre)
CREATE TABLE vaccine_inventory (
    inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    centre_id INT NOT NULL,
    vaccine_id INT NOT NULL,
    available_doses INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (centre_id) REFERENCES centres(centre_id) ON DELETE CASCADE,
    FOREIGN KEY (vaccine_id) REFERENCES vaccines(vaccine_id) ON DELETE CASCADE,
    UNIQUE KEY uq_centre_vaccine (centre_id, vaccine_id)
);

-- 6. Appointments Table (Booking, Multi-dose tracking, Status)
CREATE TABLE appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    vaccine_id INT NOT NULL,
    centre_id INT NOT NULL,
    dose_number INT NOT NULL DEFAULT 1,
    appointment_date DATE NOT NULL,
    status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    booking_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (vaccine_id) REFERENCES vaccines(vaccine_id) ON DELETE CASCADE,
    FOREIGN KEY (centre_id) REFERENCES centres(centre_id) ON DELETE CASCADE
);

-- 7. AEFI Reports Table (Post-Vaccination Adverse Events & Clinical Triage)
CREATE TABLE aefi_reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    vaccine_id INT NOT NULL,
    severity ENUM('MILD', 'MODERATE', 'SEVERE') NOT NULL,
    symptoms TEXT NOT NULL,
    callback_phone VARCHAR(15) NOT NULL,
    triage_status ENUM('PENDING', 'CALL_INITIATED', 'RESOLVED') NOT NULL DEFAULT 'PENDING',
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    triage_officer VARCHAR(100),
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (vaccine_id) REFERENCES vaccines(vaccine_id) ON DELETE CASCADE
);

-- ============================================================================
-- DATABASE VIEWS
-- ============================================================================

-- View: Complete Appointment Dashboard
CREATE OR REPLACE VIEW vw_appointment_details AS
SELECT 
    a.appointment_id,
    u.full_name AS patient_name,
    u.phone AS patient_phone,
    v.name AS vaccine_name,
    c.name AS centre_name,
    a.dose_number,
    a.appointment_date,
    a.status,
    a.booking_timestamp
FROM appointments a
JOIN patients p ON a.patient_id = p.patient_id
JOIN users u ON p.user_id = u.user_id
JOIN vaccines v ON a.vaccine_id = v.vaccine_id
JOIN centres c ON a.centre_id = c.centre_id;

-- View: Live Centre Inventory Summary
CREATE OR REPLACE VIEW vw_centre_stock_summary AS
SELECT 
    c.centre_id,
    c.name AS centre_name,
    c.district,
    v.name AS vaccine_name,
    vi.available_doses,
    vi.last_updated
FROM vaccine_inventory vi
JOIN centres c ON vi.centre_id = c.centre_id
JOIN vaccines v ON vi.vaccine_id = v.vaccine_id;

-- ============================================================================
-- TRIGGERS
-- ============================================================================

DELIMITER $$
CREATE TRIGGER trg_after_appointment_insert
AFTER INSERT ON appointments
FOR EACH ROW
BEGIN
    -- Decrement stock by 1 when appointment is scheduled
    UPDATE vaccine_inventory 
    SET available_doses = GREATEST(0, available_doses - 1)
    WHERE centre_id = NEW.centre_id AND vaccine_id = NEW.vaccine_id;
END$$
DELIMITER ;

-- ============================================================================
-- SEED DATA FOR TESTING & VIVA DEMO
-- ============================================================================

-- Sample Users
INSERT INTO users (username, password, full_name, phone, role) VALUES
('arya_prakash', 'pass123', 'Arya Prakash', '+91 9847123456', 'PATIENT'),
('rahul_menon', 'pass123', 'Rahul Menon', '+91 9847654321', 'PATIENT'),
('admin', 'admin123', 'Dr. Radhakrishnan (Nodal Officer)', '+91 495 2371475', 'ADMIN');

-- Sample Patients
INSERT INTO patients (user_id, dob, aadhar_no, blood_group, medical_notes) VALUES
(1, '1998-05-14', '7482-9104-5512', 'B+', 'No known drug allergies. Mild childhood asthma.'),
(2, '2001-11-22', '8910-3321-4490', 'O+', 'Healthy, no prior chronic complications.');

-- Authorized Vaccines
INSERT INTO vaccines (name, manufacturer, platform_type, doses_required, min_gap_days, approved_age_min, efficacy_rate, storage_temp, description) VALUES
('Covishield', 'Serum Institute of India (AstraZeneca)', 'Viral Vector', 2, 84, 18, 81.3, '2°C to 8°C', 'Recombinant viral vector platform triggering robust spike-protein cellular immunity.'),
('Covaxin', 'Bharat Biotech / ICMR', 'Inactivated Virion', 2, 28, 12, 77.8, '2°C to 8°C', 'Whole-virion inactivated Vero-cell platform presenting broad antigen epitopes.'),
('Corbevax', 'Biological E. Limited', 'Protein Subunit', 2, 28, 5, 80.0, '2°C to 8°C', 'Recombinant protein sub-unit vaccine suitable for pediatric inoculations and boosters.'),
('Hepatitis B', 'GlaxoSmithKline / Panacea', 'Recombinant HBsAg', 3, 30, 0, 95.0, '2°C to 8°C', 'Recombinant vaccine providing life-long protection against chronic liver cirrhosis and HBV.'),
('MMR', 'Serum Institute / Merck', 'Live Attenuated', 2, 90, 1, 97.0, '2°C to 8°C', 'Trivalent live attenuated vaccine immunization against Measles, Mumps, and Rubella.'),
('Influenza Flu Shot', 'Abbott / Sanofi Pasteur', 'Inactivated Split', 1, 365, 1, 60.0, '2°C to 8°C', 'Quadrivalent seasonal influenza vaccine updated annually for active regional strains.');

-- Designated Centres
INSERT INTO centres (name, address, district, phone, operating_hours, is_emergency_hub, latitude, longitude) VALUES
('District General Hospital', 'Mananchira, Kozhikode, Kerala - 673001', 'Kozhikode', '+91 495 2720250', '09:00 AM – 04:30 PM (Mon – Sat)', FALSE, 11.2530, 75.7780),
('Govt. Medical College Immunization Hub', 'Medical College PO, Kozhikode, Kerala - 673008', 'Kozhikode', '+91 495 2355331', '08:30 AM – 06:00 PM (Daily)', TRUE, 11.2723, 75.8360),
('Cheruthuruthy Primary Health Centre', 'Cheruthuruthy, Thrissur, Kerala - 679531', 'Thrissur', '+91 4884 262330', '09:00 AM – 02:00 PM (Mon – Fri)', FALSE, 10.7485, 76.2810),
('Aster MIMS Vaccination Clinic', 'Mini Bypass Road, Govindapuram, Kozhikode - 673016', 'Kozhikode', '+91 495 2488000', '08:00 AM – 08:00 PM (Daily)', FALSE, 11.2460, 75.8010);

-- Inventory Stock
INSERT INTO vaccine_inventory (centre_id, vaccine_id, available_doses) VALUES
(1, 1, 150), (1, 2, 80), (1, 4, 60),
(2, 1, 300), (2, 2, 200), (2, 3, 120), (2, 5, 90),
(3, 2, 45), (3, 4, 30),
(4, 1, 100), (4, 3, 75), (4, 6, 150);

-- Sample Completed & Upcoming Appointments
INSERT INTO appointments (patient_id, vaccine_id, centre_id, dose_number, appointment_date, status) VALUES
(1, 1, 1, 1, '2026-05-10', 'COMPLETED'),
(1, 1, 1, 2, '2026-08-15', 'SCHEDULED'),
(2, 2, 2, 1, '2026-09-20', 'SCHEDULED');

-- Sample AEFI Report
INSERT INTO aefi_reports (patient_id, vaccine_id, severity, symptoms, callback_phone, triage_status) VALUES
(1, 1, 'MILD', 'Low-grade fever (100.2 F) and localized left arm tenderness lasting 24 hours.', '+91 9847123456', 'RESOLVED');
