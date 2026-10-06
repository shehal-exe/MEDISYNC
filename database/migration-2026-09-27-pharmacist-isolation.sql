-- MEDISYNC migration: pharmacist-specific records
-- Run this on an existing local MEDISYNC database before using the updated backend.
USE medisync;

-- 1. Pharmacist public handles used for targeted refill requests.
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'PharmacistProfile'
      AND COLUMN_NAME = 'public_handle'
);
SET @sql = IF(
    @column_exists = 0,
    'ALTER TABLE PharmacistProfile ADD COLUMN public_handle VARCHAR(80) NULL AFTER license_number',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE PharmacistProfile
SET public_handle = LOWER(REGEXP_REPLACE(CONCAT(first_name, '-', last_name, '-ph', pharmacist_id), '[^a-zA-Z0-9]+', '-'))
WHERE public_handle IS NULL OR public_handle = '';

SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'PharmacistProfile'
      AND INDEX_NAME = 'public_handle'
);
SET @sql = IF(
    @index_exists = 0,
    'ALTER TABLE PharmacistProfile ADD UNIQUE KEY public_handle (public_handle)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE PharmacistProfile MODIFY public_handle VARCHAR(80) NOT NULL;

-- 2. Patient refill requests targeted to one pharmacist.
CREATE TABLE IF NOT EXISTS RefillRequest (
    refill_request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    requested_pharmacist_id BIGINT NULL,
    approved_by BIGINT NULL,
    medicine_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    fulfillment_method ENUM('Pickup', 'Delivery') NOT NULL,
    estimated_total DECIMAL(10, 2) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'COMPLETED', 'REJECTED') DEFAULT 'PENDING',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    pharmacist_notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES PatientProfile(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (requested_pharmacist_id) REFERENCES PharmacistProfile(pharmacist_id) ON DELETE SET NULL,
    FOREIGN KEY (approved_by) REFERENCES PharmacistProfile(pharmacist_id) ON DELETE SET NULL,
    INDEX idx_refill_status (status),
    INDEX idx_refill_requested_pharmacist (requested_pharmacist_id),
    INDEX idx_refill_requested_at (requested_at)
);

-- 3. Inventory ownership: existing demo stock belongs to the demo pharmacist.
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'Medicine'
      AND COLUMN_NAME = 'pharmacist_id'
);
SET @sql = IF(
    @column_exists = 0,
    'ALTER TABLE Medicine ADD COLUMN pharmacist_id BIGINT NULL AFTER medicine_id',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @default_pharmacist_id = (
    SELECT pharmacist_id
    FROM PharmacistProfile
    ORDER BY pharmacist_id
    LIMIT 1
);
UPDATE Medicine
SET pharmacist_id = @default_pharmacist_id
WHERE pharmacist_id IS NULL;

ALTER TABLE Medicine MODIFY pharmacist_id BIGINT NOT NULL;

SET @index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'Medicine'
      AND INDEX_NAME = 'idx_medicine_pharmacist_name'
);
SET @sql = IF(
    @index_exists = 0,
    'ALTER TABLE Medicine ADD INDEX idx_medicine_pharmacist_name (pharmacist_id, name)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'Medicine'
      AND CONSTRAINT_NAME = 'fk_medicine_pharmacist'
);
SET @sql = IF(
    @fk_exists = 0,
    'ALTER TABLE Medicine ADD CONSTRAINT fk_medicine_pharmacist FOREIGN KEY (pharmacist_id) REFERENCES PharmacistProfile(pharmacist_id) ON DELETE RESTRICT',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
