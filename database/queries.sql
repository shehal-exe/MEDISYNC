-- MEDISYNC - Useful Queries

USE medisync_db;

-- 1. Patient Medicine Lookup
-- Get all active medicines for a specific patient (e.g., patient_id = 1)
SELECT pm.patient_medicine_id, m.name, pm.dosage, pm.instructions, ms.frequency, ms.time_of_day
FROM PatientMedicine pm
JOIN Medicine m ON pm.medicine_id = m.medicine_id
JOIN MedicationSchedule ms ON pm.patient_medicine_id = ms.patient_medicine_id
WHERE pm.patient_id = 1 AND pm.is_active = TRUE;

-- 2. Medicine Search
-- Search for a medicine by name
SELECT * FROM Medicine WHERE name LIKE '%Paracetamol%';

-- 3. Adherence Calculation
-- Calculate percentage of TAKEN medications for a specific schedule
SELECT 
    COUNT(CASE WHEN status = 'TAKEN' THEN 1 END) AS taken_count,
    COUNT(*) AS total_expected,
    (COUNT(CASE WHEN status = 'TAKEN' THEN 1 END) / COUNT(*)) * 100 AS adherence_percentage
FROM MedicationLog
WHERE schedule_id = 2;

-- 4. Low Stock Alert
-- Find inventory batches with stock below a threshold (e.g., 50)
SELECT m.name, b.batch_number, b.quantity_in_stock
FROM InventoryBatch b
JOIN Medicine m ON b.medicine_id = m.medicine_id
WHERE b.quantity_in_stock < 50;

-- 5. Expiring Batches
-- Find batches expiring within the next 30 days
SELECT m.name, b.batch_number, b.expiry_date
FROM InventoryBatch b
JOIN Medicine m ON b.medicine_id = m.medicine_id
WHERE b.expiry_date BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY);

-- 6. Expired Batches
-- Find batches that have already expired
SELECT m.name, b.batch_number, b.expiry_date, b.quantity_in_stock
FROM InventoryBatch b
JOIN Medicine m ON b.medicine_id = m.medicine_id
WHERE b.expiry_date < CURRENT_DATE AND b.quantity_in_stock > 0;

-- 7. Pharmacist Dashboard Statistics
-- Total sales today
SELECT SUM(total_amount) AS total_sales_today
FROM Sale
WHERE DATE(sale_date) = CURRENT_DATE;

-- Pending Prescriptions count
SELECT COUNT(*) AS pending_prescriptions
FROM Prescription
WHERE status = 'PENDING';

-- 8. Customer Search
-- Search customer by phone number
SELECT * FROM Customer WHERE phone_number LIKE '%555-0200%';

-- 9. Sales Totals Date Range
-- Sales report for a specific month
SELECT DATE(sale_date) AS date, SUM(total_amount) AS daily_revenue
FROM Sale
WHERE sale_date BETWEEN '2024-10-01' AND '2024-10-31'
GROUP BY DATE(sale_date);

-- 10. Invoice Retrieval
-- Get full invoice details by invoice number
SELECT i.invoice_number, i.issue_date, i.grand_total, ii.description, ii.amount
FROM Invoice i
JOIN InvoiceItem ii ON i.invoice_id = ii.invoice_id
WHERE i.invoice_number = 'INV-2024-10001';

-- 11. Notification Retrieval
-- Get unread notifications for a user
SELECT * FROM Notification
WHERE user_id = 1 AND is_read = FALSE
ORDER BY created_at DESC;

-- 12. Audit Log Retrieval
-- Recent system events
SELECT * FROM AuditLog
ORDER BY timestamp DESC
LIMIT 50;
