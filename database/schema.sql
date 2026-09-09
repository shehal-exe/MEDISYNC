-- MEDISYNC - Database Schema

CREATE DATABASE IF NOT EXISTS medisync_db;
USE medisync_db;

-- 1. User Table
CREATE TABLE IF NOT EXISTS User (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Role Table
CREATE TABLE IF NOT EXISTS Role (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_name ENUM('PATIENT', 'PHARMACIST') NOT NULL,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_role (user_id, role_name)
);

-- 3. PatientProfile Table
CREATE TABLE IF NOT EXISTS PatientProfile (
    patient_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    contact_number VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE
);

-- 4. PharmacistProfile Table
CREATE TABLE IF NOT EXISTS PharmacistProfile (
    pharmacist_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    license_number VARCHAR(100) NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE
);

-- 5. Medicine Table (Master Catalog)
CREATE TABLE IF NOT EXISTS Medicine (
    medicine_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_medicine_name (name)
);

-- 6. InventoryBatch Table
CREATE TABLE IF NOT EXISTS InventoryBatch (
    batch_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id BIGINT NOT NULL,
    batch_number VARCHAR(100) NOT NULL,
    expiry_date DATE NOT NULL,
    quantity_in_stock INT NOT NULL CHECK (quantity_in_stock >= 0),
    unit_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (medicine_id) REFERENCES Medicine(medicine_id) ON DELETE RESTRICT,
    UNIQUE KEY uk_medicine_batch (medicine_id, batch_number),
    INDEX idx_expiry_date (expiry_date)
);

-- 7. PatientMedicine Table
CREATE TABLE IF NOT EXISTS PatientMedicine (
    patient_medicine_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    dosage VARCHAR(100),
    instructions TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES PatientProfile(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES Medicine(medicine_id) ON DELETE RESTRICT
);

-- 8. MedicationSchedule Table
CREATE TABLE IF NOT EXISTS MedicationSchedule (
    schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_medicine_id BIGINT NOT NULL,
    frequency VARCHAR(50) NOT NULL, -- e.g., 'DAILY', 'WEEKLY'
    time_of_day TIME NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    FOREIGN KEY (patient_medicine_id) REFERENCES PatientMedicine(patient_medicine_id) ON DELETE CASCADE
);

-- 9. MedicationLog Table
CREATE TABLE IF NOT EXISTS MedicationLog (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    log_time TIMESTAMP NOT NULL,
    status ENUM('TAKEN', 'MISSED', 'SKIPPED') NOT NULL,
    FOREIGN KEY (schedule_id) REFERENCES MedicationSchedule(schedule_id) ON DELETE CASCADE
);

-- 10. PharmacistProfile (verifier reference correction for Prescription)
-- (Handled by references in Prescription table)

-- 11. Prescription Table
CREATE TABLE IF NOT EXISTS Prescription (
    prescription_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    verified_by BIGINT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_path VARCHAR(500) NOT NULL,
    status ENUM('PENDING', 'VERIFIED', 'REJECTED') DEFAULT 'PENDING',
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES PatientProfile(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (verified_by) REFERENCES PharmacistProfile(pharmacist_id) ON DELETE SET NULL
);

-- 12. PrescriptionItem Table
CREATE TABLE IF NOT EXISTS PrescriptionItem (
    prescription_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    prescribed_quantity INT,
    dosage_instructions TEXT,
    FOREIGN KEY (prescription_id) REFERENCES Prescription(prescription_id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES Medicine(medicine_id) ON DELETE RESTRICT
);

-- 13. Notification Table
CREATE TABLE IF NOT EXISTS Notification (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE
);

-- 14. Customer Table
CREATE TABLE IF NOT EXISTS Customer (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 15. Sale Table
CREATE TABLE IF NOT EXISTS Sale (
    sale_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NULL,
    patient_id BIGINT NULL,
    pharmacist_id BIGINT NOT NULL,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE SET NULL,
    FOREIGN KEY (patient_id) REFERENCES PatientProfile(patient_id) ON DELETE SET NULL,
    FOREIGN KEY (pharmacist_id) REFERENCES PharmacistProfile(pharmacist_id) ON DELETE RESTRICT
);

-- 16. SaleItem Table
CREATE TABLE IF NOT EXISTS SaleItem (
    sale_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price_at_sale DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES Sale(sale_id) ON DELETE CASCADE,
    FOREIGN KEY (batch_id) REFERENCES InventoryBatch(batch_id) ON DELETE RESTRICT
);

-- 17. Invoice Table
CREATE TABLE IF NOT EXISTS Invoice (
    invoice_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id BIGINT NOT NULL UNIQUE,
    invoice_number VARCHAR(100) NOT NULL UNIQUE,
    issue_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tax_amount DECIMAL(10, 2) DEFAULT 0.00,
    grand_total DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES Sale(sale_id) ON DELETE RESTRICT
);

-- 18. InvoiceItem Table
CREATE TABLE IF NOT EXISTS InvoiceItem (
    invoice_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    description VARCHAR(255),
    amount DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (invoice_id) REFERENCES Invoice(invoice_id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES Medicine(medicine_id) ON DELETE RESTRICT
);

-- 19. PasswordResetToken Table
CREATE TABLE IF NOT EXISTS PasswordResetToken (
    token_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE
);

-- 20. AuditLog Table
CREATE TABLE IF NOT EXISTS AuditLog (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    action VARCHAR(255) NOT NULL,
    table_affected VARCHAR(100),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE SET NULL,
    INDEX idx_audit_timestamp (timestamp)
);
