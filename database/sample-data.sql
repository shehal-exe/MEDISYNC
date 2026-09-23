-- MEDISYNC - Comprehensive Sample Data (Demo Purposes Only)
USE medisync_db;

-- 1. CLEAN UP EXISTING DATA
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE AuditLog;
TRUNCATE TABLE PasswordResetToken;
TRUNCATE TABLE InvoiceItem;
TRUNCATE TABLE Invoice;
TRUNCATE TABLE SaleItem;
TRUNCATE TABLE Sale;
TRUNCATE TABLE Customer;
TRUNCATE TABLE Notification;
TRUNCATE TABLE PrescriptionItem;
TRUNCATE TABLE Prescription;
TRUNCATE TABLE MedicationLog;
TRUNCATE TABLE MedicationSchedule;
TRUNCATE TABLE PatientMedicine;
TRUNCATE TABLE InventoryBatch;
TRUNCATE TABLE Medicine;
TRUNCATE TABLE PharmacistProfile;
TRUNCATE TABLE PatientProfile;
TRUNCATE TABLE Role;
TRUNCATE TABLE User;
SET FOREIGN_KEY_CHECKS = 1;

-- 2. USERS & ROLES
-- Real Pharmacist Setup
INSERT INTO User (user_id, email, password_hash) VALUES 
(1, 'pharmacist_real@medisync.com', '\\\.U4yA.K.t1.oB12m02'); -- password123
INSERT INTO Role (user_id, role_name) VALUES (1, 'PHARMACIST');
INSERT INTO PharmacistProfile (pharmacist_id, user_id, first_name, last_name, license_number) VALUES 
(1, 1, 'Dr. Admin', 'Smith', 'PH-99999');

-- Dummy Patients
INSERT INTO User (user_id, email, password_hash) VALUES 
(2, 'test_patient@medisync.com', '\\\.U4yA.K.t1.oB12m02'),
(3, 'arun.thomas@medisync.test', '\\\.U4yA.K.t1.oB12m02'),
(4, 'anjali.menon@medisync.test', '\\\.U4yA.K.t1.oB12m02');

INSERT INTO Role (user_id, role_name) VALUES 
(2, 'PATIENT'),
(3, 'PATIENT'),
(4, 'PATIENT');

INSERT INTO PatientProfile (patient_id, user_id, first_name, last_name, date_of_birth, contact_number) VALUES 
(1, 2, 'John', 'Doe', '1985-08-20', '+1-555-0100'),
(2, 3, 'Arun', 'Thomas', '1992-03-10', '+1-555-0200'),
(3, 4, 'Anjali', 'Menon', '1988-11-25', '+1-555-0300');

-- 3. MEDICINES MASTER CATALOG
INSERT INTO Medicine (medicine_id, name, manufacturer, description) VALUES 
(1, 'Paracetamol 500mg', 'PharmaCorp', 'Pain reliever and fever reducer'),
(2, 'Amoxicillin 250mg', 'HealthMed', 'Broad-spectrum antibiotic'),
(3, 'Lisinopril 10mg', 'HeartCare', 'ACE inhibitor for high blood pressure'),
(4, 'Metformin 500mg', 'DiaPharma', 'Type 2 diabetes medication'),
(5, 'Ibuprofen 400mg', 'PharmaCorp', 'Nonsteroidal anti-inflammatory drug (NSAID)'),
(6, 'Atorvastatin 20mg', 'HeartCare', 'Statin for lowering cholesterol'),
(7, 'Levothyroxine 50mcg', 'EndoMed', 'Thyroid hormone replacement'),
(8, 'Omeprazole 20mg', 'GastroMed', 'Proton pump inhibitor for acid reflux');

-- 4. INVENTORY BATCHES (Varying stock levels & expiry dates to test alerts)
INSERT INTO InventoryBatch (batch_id, medicine_id, batch_number, expiry_date, quantity_in_stock, unit_price) VALUES 
(1, 1, 'B-PARA-001', '2025-12-31', 5000, 0.15),
(2, 2, 'B-AMOX-001', '2024-05-15', 5, 1.20),      -- Low stock!
(3, 3, 'B-LISI-001', '2026-10-01', 300, 0.80),
(4, 4, 'B-METF-001', '2025-08-20', 1200, 0.45),
(5, 5, 'B-IBUP-001', '2026-01-10', 800, 0.25),
(6, 6, 'B-ATOR-001', '2024-10-30', 40, 1.50),       -- Expiring soon!
(7, 7, 'B-LEVO-001', '2025-02-28', 150, 0.60),
(8, 8, 'B-OMEP-001', '2025-11-15', 600, 0.90);

-- 5. PATIENT MEDICINES (For John Doe, patient_id=1)
INSERT INTO PatientMedicine (patient_medicine_id, patient_id, medicine_id, dosage, instructions, is_active) VALUES 
(1, 1, 3, '1 Tablet', 'Take in the morning with water', TRUE),
(2, 1, 4, '1 Tablet', 'Take twice daily with meals', TRUE),
(3, 1, 1, '2 Tablets', 'Take only when having fever', FALSE);

-- 6. MEDICATION SCHEDULES
INSERT INTO MedicationSchedule (schedule_id, patient_medicine_id, frequency, time_of_day, start_date, end_date) VALUES 
(1, 1, 'DAILY', '08:00:00', '2024-01-01', '2025-01-01'),
(2, 2, 'DAILY', '09:00:00', '2024-01-01', '2025-01-01'),
(3, 2, 'DAILY', '20:00:00', '2024-01-01', '2025-01-01');

-- 7. MEDICATION LOGS (Adherence Demo)
INSERT INTO MedicationLog (log_id, schedule_id, log_time, status) VALUES 
(1, 1, '2024-10-20 08:05:00', 'TAKEN'),
(2, 2, '2024-10-20 09:10:00', 'TAKEN'),
(3, 3, '2024-10-20 20:00:00', 'MISSED'),
(4, 1, '2024-10-21 08:02:00', 'TAKEN'),
(5, 2, '2024-10-21 09:15:00', 'TAKEN'),
(6, 3, '2024-10-21 20:30:00', 'TAKEN');

-- 8. PRESCRIPTIONS
INSERT INTO Prescription (prescription_id, patient_id, verified_by, upload_date, file_path, status, notes) VALUES 
(1, 1, 1, '2024-10-10 10:00:00', '/uploads/prescriptions/dummy1.pdf', 'VERIFIED', 'Valid prescription for Lisinopril'),
(2, 2, NULL, '2024-10-22 09:30:00', '/uploads/prescriptions/dummy2.pdf', 'PENDING', 'Pending verification from Dr. Admin'),
(3, 3, 1, '2024-10-15 14:00:00', '/uploads/prescriptions/dummy3.pdf', 'REJECTED', 'Blurry image, please re-upload.');

INSERT INTO PrescriptionItem (prescription_item_id, prescription_id, medicine_id, prescribed_quantity, dosage_instructions) VALUES 
(1, 1, 3, 30, 'Take 1 pill every morning'),
(2, 2, 2, 14, 'Take 1 pill every 12 hours for 7 days');

-- 9. NOTIFICATIONS
INSERT INTO Notification (user_id, message, is_read) VALUES 
(2, 'Welcome to MEDISYNC! Please update your profile.', TRUE),
(2, 'Reminder: Time to take your Lisinopril 10mg.', FALSE),
(2, 'Your prescription has been VERIFIED by the pharmacist.', FALSE),
(1, 'New prescription uploaded by Arun Thomas requiring verification.', FALSE),
(1, 'Low Stock Alert: Amoxicillin 250mg is running low (5 left).', FALSE);

-- 10. CUSTOMERS & SALES (For Pharmacist Dashboard Demo)
INSERT INTO Customer (customer_id, name, phone_number) VALUES 
(1, 'Walk-in Customer 1', 'N/A'),
(2, 'Rahul Nair', '+1-555-0400');

INSERT INTO Sale (sale_id, customer_id, patient_id, pharmacist_id, sale_date, total_amount) VALUES 
(1, 1, NULL, 1, '2024-10-22 10:15:00', 1.50),
(2, 2, 2, 1, '2024-10-22 14:30:00', 24.00);

INSERT INTO SaleItem (sale_item_id, sale_id, batch_id, quantity, price_at_sale) VALUES 
(1, 1, 1, 10, 0.15),
(2, 2, 3, 30, 0.80);

-- END DEMO DATA
