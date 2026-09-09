# MEDISYNC - Requirements Document

## 1. Project Overview
MEDISYNC is a web-based healthcare management system designed to streamline the interaction between patients and pharmacists. It facilitates medicine management, prescription tracking, and inventory control for pharmacies, while providing patients with a seamless way to purchase and track their medications.

## 2. Problem Statement
Patients often face difficulties in tracking their prescriptions, verifying medicine availability, and managing their medication schedules. On the other hand, pharmacists struggle with manual inventory management, verifying prescriptions, and maintaining patient medication logs efficiently.

## 3. Proposed Solution
MEDISYNC provides a centralized platform connecting patients and pharmacists. It allows patients to register, view available medicines, submit prescriptions, and track their medication schedules. Pharmacists can manage the medicine inventory, process sales, verify prescriptions, and manage customer billing.

## 4. Objectives
- Streamline pharmacy inventory management.
- Provide a secure platform for prescription verification and medicine sales.
- Empower patients to track their medications and purchase history.
- Digitize medication schedules and billing processes.

## 5. Users
- Patients who need to purchase medicines and track prescriptions.
- Pharmacists who manage inventory, verify prescriptions, and handle sales.

## 6. Roles
- **PATIENT**: A user who can register publicly. They can manage their profile, view medicines, upload/view prescriptions, and track their medication schedules.
- **PHARMACIST**: A user who manages the system. They manage the medicine inventory, process sales, verify prescriptions, and review audit logs.

## 7. Functional Requirements
| ID | Description | Role |
|---|---|---|
| FR-01 | The system shall allow users to register as a PATIENT. | PUBLIC |
| FR-02 | The system shall authenticate users using email and password. | PATIENT, PHARMACIST |
| FR-03 | The system shall allow patients to view a list of available medicines. | PATIENT |
| FR-04 | The system shall allow patients to upload and view prescriptions. | PATIENT |
| FR-05 | The system shall allow patients to log their medication schedules. | PATIENT |
| FR-06 | The system shall allow pharmacists to add, update, and remove medicines from inventory. | PHARMACIST |
| FR-07 | The system shall allow pharmacists to verify uploaded prescriptions. | PHARMACIST |
| FR-08 | The system shall allow pharmacists to process sales and generate invoices. | PHARMACIST |
| FR-09 | The system shall notify patients of upcoming medication times. | PATIENT |
| FR-10 | The system shall track inventory batches and alert pharmacists of low stock. | PHARMACIST |

## 8. Non-functional Requirements
- **Security**: Passwords must be hashed. Only authorized roles can access specific endpoints.
- **Performance**: API response times should be under 500ms.
- **Usability**: The frontend must be responsive and intuitive.
- **Reliability**: The database must maintain ACID properties for transactions.

## 9. Scope
- User Authentication and Role Management (Patient, Pharmacist)
- Medicine Inventory Management
- Prescription Upload and Verification
- Sales and Invoicing
- Medication Scheduling and Logging

## 10. Out-of-scope
- Integration with external payment gateways.
- Integration with third-party hospital management systems.
- Home delivery tracking.

## 11. Assumptions
- Users have access to modern web browsers.
- The pharmacy operates from a single physical location (no multi-tenant pharmacy support initially).

## 12. Constraints
- The backend must be developed using Java 17 and Spring Boot.
- The frontend must be developed using HTML5, CSS3, Vanilla JS, and Chart.js.
- The database must be MySQL accessed via JDBC.

## 13. Limitations
- Offline access is not supported.
- Notifications are limited to in-app or simple email (no SMS integration in Phase 1).

## 14. Future Enhancements
- Integration with external payment gateways.
- Mobile application development.
- AI-based prescription reading.
- Multi-branch pharmacy support.
