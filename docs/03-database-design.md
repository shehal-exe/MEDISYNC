# MEDISYNC - Database Design Document

This document outlines the database design for MEDISYNC, including entities, relationships, primary keys, foreign keys, constraints, and indexes.

## Entities Overview

### 1. User
- **Purpose**: Central table for all system users (authentication and basic info).
- **PK**: `user_id`
- **Constraints**: `email` (UNIQUE, NOT NULL), `password_hash` (NOT NULL)
- **Relationships**: 
  - 1-to-many with `Role`
  - 1-to-1 with `PatientProfile` or `PharmacistProfile`

### 2. Role
- **Purpose**: Defines user roles (PATIENT, PHARMACIST).
- **PK**: `role_id`
- **FK**: `user_id` (References `User.user_id`)
- **Constraints**: `role_name` (NOT NULL, ENUM)

### 3. PatientProfile
- **Purpose**: Stores patient-specific details.
- **PK**: `patient_id`
- **FK**: `user_id` (References `User.user_id`, UNIQUE)
- **Constraints**: `date_of_birth`, `contact_number`

### 4. PharmacistProfile
- **Purpose**: Stores pharmacist-specific details (e.g., license number).
- **PK**: `pharmacist_id`
- **FK**: `user_id` (References `User.user_id`, UNIQUE)
- **Constraints**: `license_number` (UNIQUE)

### 5. Medicine
- **Purpose**: Catalog of available medicines.
- **PK**: `medicine_id`
- **Constraints**: `name` (NOT NULL), `manufacturer`, `description`
- **Indexes**: Index on `name` for faster search.

### 6. InventoryBatch
- **Purpose**: Tracks specific batches of medicines (for expiry and stock management).
- **PK**: `batch_id`
- **FK**: `medicine_id` (References `Medicine.medicine_id`)
- **Constraints**: `batch_number` (UNIQUE), `expiry_date`, `quantity_in_stock`, `unit_price`
- **Indexes**: Index on `expiry_date` for reporting near-expiry stock.

### 7. PatientMedicine
- **Purpose**: Maps medicines prescribed or currently taken by a patient.
- **PK**: `patient_medicine_id`
- **FKs**: `patient_id` (References `PatientProfile.patient_id`), `medicine_id` (References `Medicine.medicine_id`)

### 8. MedicationSchedule
- **Purpose**: Defines when a patient should take specific medicines.
- **PK**: `schedule_id`
- **FK**: `patient_medicine_id` (References `PatientMedicine.patient_medicine_id`)
- **Constraints**: `frequency`, `time_of_day`, `start_date`, `end_date`

### 9. MedicationLog
- **Purpose**: Tracks whether a patient has taken their medication.
- **PK**: `log_id`
- **FK**: `schedule_id` (References `MedicationSchedule.schedule_id`)
- **Constraints**: `log_time`, `status` (TAKEN, MISSED)

### 10. Prescription
- **Purpose**: Represents a prescription uploaded by a patient.
- **PK**: `prescription_id`
- **FK**: `patient_id` (References `PatientProfile.patient_id`), `verified_by` (References `PharmacistProfile.pharmacist_id`, NULLABLE)
- **Constraints**: `upload_date`, `file_url`, `status` (PENDING, VERIFIED, REJECTED)

### 11. PrescriptionItem
- **Purpose**: Individual medicines listed in a verified prescription.
- **PK**: `prescription_item_id`
- **FKs**: `prescription_id` (References `Prescription.prescription_id`), `medicine_id` (References `Medicine.medicine_id`)
- **Constraints**: `prescribed_quantity`, `dosage_instructions`

### 12. Notification
- **Purpose**: System alerts and medication reminders.
- **PK**: `notification_id`
- **FK**: `user_id` (References `User.user_id`)
- **Constraints**: `message`, `created_at`, `is_read`

### 13. Customer
- **Purpose**: Stores walk-in or general customer details for pharmacy sales.
- **PK**: `customer_id`
- **Constraints**: `name`, `phone_number`

### 14. Sale
- **Purpose**: Records a checkout/transaction at the pharmacy.
- **PK**: `sale_id`
- **FKs**: `customer_id` (References `Customer.customer_id`, NULLABLE), `patient_id` (References `PatientProfile.patient_id`, NULLABLE), `pharmacist_id` (References `PharmacistProfile.pharmacist_id`)
- **Constraints**: `sale_date`, `total_amount`

### 15. SaleItem
- **Purpose**: Line items for a specific sale.
- **PK**: `sale_item_id`
- **FKs**: `sale_id` (References `Sale.sale_id`), `batch_id` (References `InventoryBatch.batch_id`)
- **Constraints**: `quantity`, `price_at_sale`

### 16. Invoice
- **Purpose**: Financial record generated from a sale.
- **PK**: `invoice_id`
- **FK**: `sale_id` (References `Sale.sale_id`)
- **Constraints**: `invoice_number` (UNIQUE), `issue_date`, `tax_amount`, `grand_total`

### 17. InvoiceItem
- **Purpose**: Detail lines for an invoice (usually maps 1-to-1 with SaleItem, but separated for financial immutability).
- **PK**: `invoice_item_id`
- **FK**: `invoice_id` (References `Invoice.invoice_id`), `medicine_id` (References `Medicine.medicine_id`)
- **Constraints**: `description`, `amount`

### 18. PasswordResetToken
- **Purpose**: Handles secure password reset flows.
- **PK**: `token_id`
- **FK**: `user_id` (References `User.user_id`)
- **Constraints**: `token_hash` (UNIQUE), `expiry_date`

### 19. AuditLog
- **Purpose**: Tracks significant system actions for security and compliance.
- **PK**: `log_id`
- **FK**: `user_id` (References `User.user_id`, NULLABLE)
- **Constraints**: `action`, `table_affected`, `timestamp`, `ip_address`
- **Indexes**: Index on `timestamp` for sorting logs.
