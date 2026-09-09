# MEDISYNC - Database Design Document

This document outlines the database design for MEDISYNC, including entities, relationships, primary keys, foreign keys, constraints, and indexes. All monetary values use `DECIMAL` types.

## Entities Overview

### 1. User
- **Purpose**: Central table for all system users (authentication and basic info).
- **PK**: `user_id`
- **Constraints**: `email` (UNIQUE, NOT NULL), `password_hash` (NOT NULL, hashed).
- **Relationships**: 
  - 1-to-many with `Role`
  - 1-to-1 with `PatientProfile` or `PharmacistProfile`

### 2. Role
- **Purpose**: Defines user roles (PATIENT, PHARMACIST).
- **PK**: `role_id`
- **FK**: `user_id` (References `User.user_id`)
- **Constraints**: `role_name` (NOT NULL, ENUM: 'PATIENT', 'PHARMACIST'). Only these two roles are permitted.

### 3. PatientProfile
- **Purpose**: Stores patient-specific details.
- **PK**: `patient_id`
- **FK**: `user_id` (References `User.user_id`, UNIQUE)
- **Constraints**: `date_of_birth`, `contact_number`

### 4. PharmacistProfile
- **Purpose**: Stores pharmacist-specific details.
- **PK**: `pharmacist_id`
- **FK**: `user_id` (References `User.user_id`, UNIQUE)
- **Constraints**: `license_number` (UNIQUE, NOT NULL)

### 5. Medicine
- **Purpose**: Master catalog of available medicines.
- **PK**: `medicine_id`
- **Constraints**: `name` (NOT NULL), `manufacturer`, `description`
- **Indexes**: Index on `name` for faster search.

### 6. InventoryBatch
- **Purpose**: Tracks specific batches of medicines (for expiry and stock management).
- **PK**: `batch_id`
- **FK**: `medicine_id` (References `Medicine.medicine_id`)
- **Constraints**: `batch_number` (NOT NULL), `expiry_date` (NOT NULL), `quantity_in_stock` (INT, NOT NULL, >= 0), `unit_price` (DECIMAL, NOT NULL)
- **Unique**: `(medicine_id, batch_number)` composite unique constraint.
- **Indexes**: Index on `expiry_date` for reporting near-expiry stock.

### 7. PatientMedicine
- **Purpose**: Maps medicines prescribed or currently taken by a patient.
- **PK**: `patient_medicine_id`
- **FKs**: `patient_id` (References `PatientProfile.patient_id`), `medicine_id` (References `Medicine.medicine_id`)
- **Constraints**: `dosage`, `instructions`, `is_active`

### 8. MedicationSchedule
- **Purpose**: Defines when a patient should take specific medicines.
- **PK**: `schedule_id`
- **FK**: `patient_medicine_id` (References `PatientMedicine.patient_medicine_id`)
- **Constraints**: `frequency` (e.g., daily, weekly), `time_of_day`, `start_date`, `end_date`

### 9. MedicationLog
- **Purpose**: Tracks patient adherence and medication events.
- **PK**: `log_id`
- **FK**: `schedule_id` (References `MedicationSchedule.schedule_id`)
- **Constraints**: `log_time`, `status` (ENUM: 'TAKEN', 'MISSED', 'SKIPPED')
- **Note**: Adherence percentage = (completed TAKEN expected events / total expected medication events) × 100.

### 10. Prescription
- **Purpose**: Represents a prescription uploaded by a patient. Metadata is stored here; the actual file is in server-side storage.
- **PK**: `prescription_id`
- **FK**: `patient_id` (References `PatientProfile.patient_id`), `verified_by` (References `PharmacistProfile.pharmacist_id`, NULLABLE)
- **Constraints**: `upload_date`, `file_path` (NOT NULL), `status` (ENUM: 'PENDING', 'VERIFIED', 'REJECTED')

### 11. PrescriptionItem
- **Purpose**: Individual medicines listed in a verified prescription.
- **PK**: `prescription_item_id`
- **FKs**: `prescription_id` (References `Prescription.prescription_id`), `medicine_id` (References `Medicine.medicine_id`)
- **Constraints**: `prescribed_quantity`, `dosage_instructions`

### 12. Notification
- **Purpose**: System alerts, refill alerts, and medication reminders.
- **PK**: `notification_id`
- **FK**: `user_id` (References `User.user_id`)
- **Constraints**: `message`, `created_at`, `is_read` (BOOLEAN)

### 13. Customer
- **Purpose**: Stores walk-in or general customer details for pharmacy sales.
- **PK**: `customer_id`
- **Constraints**: `name`, `phone_number`

### 14. Sale
- **Purpose**: Records a pharmacy transaction.
- **PK**: `sale_id`
- **FKs**: `customer_id` (References `Customer.customer_id`, NULLABLE), `patient_id` (References `PatientProfile.patient_id`, NULLABLE), `pharmacist_id` (References `PharmacistProfile.pharmacist_id`, NOT NULL)
- **Constraints**: `sale_date`, `total_amount` (DECIMAL, NOT NULL)

### 15. SaleItem
- **Purpose**: Line items for a specific sale.
- **PK**: `sale_item_id`
- **FKs**: `sale_id` (References `Sale.sale_id`), `batch_id` (References `InventoryBatch.batch_id`)
- **Constraints**: `quantity` (INT, NOT NULL), `price_at_sale` (DECIMAL, NOT NULL)

### 16. Invoice
- **Purpose**: Financial record generated from a sale.
- **PK**: `invoice_id`
- **FK**: `sale_id` (References `Sale.sale_id`)
- **Constraints**: `invoice_number` (UNIQUE, NOT NULL), `issue_date`, `tax_amount` (DECIMAL), `grand_total` (DECIMAL)

### 17. InvoiceItem
- **Purpose**: Immutable financial detail corresponding to invoice lines.
- **PK**: `invoice_item_id`
- **FK**: `invoice_id` (References `Invoice.invoice_id`), `medicine_id` (References `Medicine.medicine_id`)
- **Constraints**: `description`, `amount` (DECIMAL, NOT NULL)

### 18. PasswordResetToken
- **Purpose**: Handles secure password reset flows.
- **PK**: `token_id`
- **FK**: `user_id` (References `User.user_id`)
- **Constraints**: `token_hash` (UNIQUE, NOT NULL - raw token must NEVER be stored), `expiry_date` (NOT NULL)

### 19. AuditLog
- **Purpose**: Tracks significant system actions for security and compliance.
- **PK**: `log_id`
- **FK**: `user_id` (References `User.user_id`, NULLABLE)
- **Constraints**: `action`, `table_affected`, `timestamp`, `ip_address`
- **Indexes**: Index on `timestamp` for sorting logs.
