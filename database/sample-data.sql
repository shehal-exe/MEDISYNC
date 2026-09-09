-- MEDISYNC - Sample Data (Demo Purposes Only)
USE medisync_db;

-- Users (Passwords are hashed 'password123' via bcrypt, but we'll use a dummy hash string here for demo data purposes since actual passwords should not be in plaintext)
-- NEVER USE THESE HASHES IN PRODUCTION.
INSERT INTO User (email, password_hash) VALUES 
('patient1@example.com', '$2a$10$xyzDummyHashForPassword123xyzDummyHashForPassword123x'),
('pharmacist1@example.com', '$2a$10$xyzDummyHashForPassword123xyzDummyHashForPassword123x');

-- Roles
INSERT INTO Role (user_id, role_name) VALUES 
(1, 'PATIENT'),
(2, 'PHARMACIST');

-- PatientProfile
INSERT INTO PatientProfile (user_id, first_name, last_name, date_of_birth, contact_number) VALUES 
(1, 'John', 'Doe', '1990-05-15', '555-0100');

-- PharmacistProfile
INSERT INTO PharmacistProfile (user_id, first_name, last_name, license_number) VALUES 
(2, 'Alice', 'Smith', 'PHARM-889900');

-- Medicine
INSERT INTO Medicine (name, manufacturer, description) VALUES 
('Paracetamol 500mg', 'PharmaCorp', 'Pain reliever and fever reducer'),
('Amoxicillin 250mg', 'HealthMed', 'Antibiotic'),
('Lisinopril 10mg', 'HeartCare', 'ACE inhibitor for high blood pressure');

-- InventoryBatch
INSERT INTO InventoryBatch (medicine_id, batch_number, expiry_date, quantity_in_stock, unit_price) VALUES 
(1, 'BATCH-P-101', '2027-12-31', 500, 2.50),
(2, 'BATCH-A-201', '2025-06-30', 200, 15.00),
(3, 'BATCH-L-301', '2026-10-15', 100, 12.00);

-- PatientMedicine
INSERT INTO PatientMedicine (patient_id, medicine_id, dosage, instructions, is_active) VALUES 
(1, 1, '1 pill', 'Take after meals for fever', TRUE),
(1, 3, '1 pill', 'Take in the morning', TRUE);

-- MedicationSchedule
INSERT INTO MedicationSchedule (patient_medicine_id, frequency, time_of_day, start_date, end_date) VALUES 
(1, 'AS_NEEDED', '08:00:00', '2024-01-01', NULL),
(2, 'DAILY', '09:00:00', '2024-01-01', '2024-12-31');

-- MedicationLog
INSERT INTO MedicationLog (schedule_id, log_time, status) VALUES 
(2, '2024-10-15 09:05:00', 'TAKEN'),
(2, '2024-10-16 09:30:00', 'MISSED');

-- Prescription
INSERT INTO Prescription (patient_id, verified_by, upload_date, file_path, status, notes) VALUES 
(1, 1, '2024-10-10 10:00:00', '/uploads/prescriptions/patient1_presc1.pdf', 'VERIFIED', 'Valid prescription for Lisinopril');

-- PrescriptionItem
INSERT INTO PrescriptionItem (prescription_id, medicine_id, prescribed_quantity, dosage_instructions) VALUES 
(1, 3, 30, 'Take 1 pill every morning');

-- Notification
INSERT INTO Notification (user_id, message, is_read) VALUES 
(1, 'Welcome to MEDISYNC! Please update your profile.', FALSE),
(1, 'Reminder: Time to take your Lisinopril 10mg.', TRUE);

-- Customer
INSERT INTO Customer (name, phone_number) VALUES 
('Jane Roe', '555-0200');

-- Sale
INSERT INTO Sale (customer_id, patient_id, pharmacist_id, sale_date, total_amount) VALUES 
(1, NULL, 1, '2024-10-15 14:30:00', 5.00);

-- SaleItem
INSERT INTO SaleItem (sale_id, batch_id, quantity, price_at_sale) VALUES 
(1, 1, 2, 2.50);

-- Invoice
INSERT INTO Invoice (sale_id, invoice_number, tax_amount, grand_total) VALUES 
(1, 'INV-2024-10001', 0.00, 5.00);

-- InvoiceItem
INSERT INTO InvoiceItem (invoice_id, medicine_id, description, amount) VALUES 
(1, 1, 'Paracetamol 500mg', 5.00);
