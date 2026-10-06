# MEDISYNC - Master Requirements Document

## 1. Project Overview
MEDISYNC — Integrated Medicine Reminder and Pharmacy Management System.

This is a B.Tech Computer Science and Engineering OOP project developed by a team of four.

## 2. Technology Stack
**Frontend:**
* HTML5
* CSS3
* Vanilla JavaScript
* Chart.js

**Backend:**
* Java
* Spring Boot
* Maven
* Spring Security
* Validation

**Database & Connectivity:**
* MySQL
* JDBC

**Database Management:**
* DBeaver

**Testing:**
* Postman
* Browser testing
* JUnit where appropriate

**Version Control:**
* Git
* GitHub

## 3. Project Description
MEDISYNC is a web-based integrated medicine reminder and pharmacy management system connecting patients and pharmacists through one centralized application with separate role-based portals.
- **The Patient Portal** focuses on personal medication management.
- **The Pharmacist Portal** focuses on pharmacy operations.

## 4. Primary User Roles
1. **PATIENT**: A user utilizing the system for personal medication management. Public registration creates PATIENT accounts only. A public user must not be able to select PHARMACIST as their role during registration.
2. **PHARMACIST**: A user responsible for pharmacy-related operations. *(Note: A pharmacist is strictly an operational role and is NOT a general system administrator).*

*(A SUPER_ADMIN role may be considered as a future enhancement but is not included in Version 1).*

## 5. Authentication Requirements
Authentication handles secure session initialization, credential management, and role segregation.
- **Registration**: Public registration exclusively creates Patient accounts.
- **Login / Logout**: Secure session initiation and termination for both portals.
- **Forgot Password / Reset Password / Change Password**: Standard credential recovery and update workflows.
- **Password Visibility Toggle**: UI capability to securely view typed passwords.
- **Remember Me & Session Management**: Securely persisting login states and managing active sessions.
- **Role-based Authorization & Protected Resources**: Ensuring patients cannot access pharmacist endpoints and vice versa.
- **Frontend Access Control & Backend Authorization**: The frontend provides UX routing, but backend authorization is mandatory.
- **Form Validation & Duplicate Email Prevention**: Rejects invalid inputs and prevents duplicate accounts.
- **Invalid Credential Handling**: Graceful error handling for incorrect login attempts.

**Security Requirements**:
- Passwords are never stored as plaintext.
- Passwords are securely hashed.
- Authentication is handled by Spring Security.
- Backend authorization is mandatory.
- Frontend protection alone is not sufficient.

## 6. Functional Modules
1. Authentication and Role Management
2. Patient Portal
3. Medication Management
4. Medication Scheduling
5. Medication Reminders
6. Medication Adherence
7. Medication History
8. Prescription Management
9. Notifications
10. Patient Profile and Preferences
11. Pharmacist Portal
12. Pharmacy Medicine Management
13. Inventory
14. Batch Management
15. Stock Management
16. Expiry Monitoring
17. Customer Management
18. Prescription Review
19. Sales
20. Billing
21. Invoices
22. Reports
23. Analytics

## 7. Functional Requirements

### Patient Functional Requirements
| ID | Title | Description | Primary Actor | Priority |
|---|---|---|---|---|
| FR-01 | Patient registration | Allow users to register a new account as a PATIENT. | PATIENT | HIGH |
| FR-02 | Patient login | Authenticate patient credentials to access the Patient Portal. | PATIENT | HIGH |
| FR-03 | Patient logout | Securely terminate the active session. | PATIENT | HIGH |
| FR-04 | Forgot password | Initiate password recovery workflow. | PATIENT | MEDIUM |
| FR-05 | Reset password | Reset password using a recovery mechanism. | PATIENT | MEDIUM |
| FR-06 | Change password | Allow authenticated users to change their password. | PATIENT | LOW |
| FR-07 | Remember me | Optionally persist the login state across sessions. | PATIENT | LOW |
| FR-08 | Session management | Maintain and expire user sessions securely. | PATIENT | HIGH |
| FR-09 | Role-based access | Restrict patient users strictly to the Patient Portal. | PATIENT | HIGH |
| FR-10 | Patient dashboard | Display upcoming medications and alerts. | PATIENT | HIGH |
| FR-11 | Patient profile management | View and update personal details. | PATIENT | MEDIUM |
| FR-12 | Medicine management | Manage personal list of prescribed/active medicines. | PATIENT | HIGH |
| FR-13 | Add medicine | Add a new medicine to the personal list. | PATIENT | HIGH |
| FR-14 | Edit medicine | Modify details of an existing personal medicine. | PATIENT | MEDIUM |
| FR-15 | Delete medicine | Remove a medicine from the personal list. | PATIENT | LOW |
| FR-16 | View medicine | View details of a specific medicine in the personal list. | PATIENT | HIGH |
| FR-17 | Search/filter medicines | Search the personal medication list. | PATIENT | LOW |
| FR-18 | Medication schedule management | Define schedules for medication intake. | PATIENT | HIGH |
| FR-19 | Daily medication schedule | Set medicines to be taken daily at specific times. | PATIENT | HIGH |
| FR-20 | Weekly medication schedule | Set medicines to be taken on specific days. | PATIENT | MEDIUM |
| FR-21 | Medication reminders | Generate internal alerts for scheduled medications. | PATIENT | HIGH |
| FR-22 | View upcoming reminders | Display a list of upcoming medication tasks. | PATIENT | HIGH |
| FR-23 | Mark medication as taken | Record a medication as successfully taken. | PATIENT | HIGH |
| FR-24 | Mark medication as skipped | Record a medication as missed or skipped. | PATIENT | HIGH |
| FR-25 | Medication log | Maintain a chronological log of medications. | PATIENT | HIGH |
| FR-26 | Medication adherence calculation | Calculate percentage of medications taken vs scheduled. | PATIENT | MEDIUM |
| FR-27 | Daily/weekly/monthly adherence | View adherence statistics over various periods. | PATIENT | LOW |
| FR-28 | Medication history | Review historical medication usage data. | PATIENT | MEDIUM |
| FR-29 | Prescription upload | Upload digital copies of medical prescriptions. | PATIENT | HIGH |
| FR-30 | View prescription | View previously uploaded prescriptions. | PATIENT | HIGH |
| FR-31 | View prescription status | Check verification status (Pending/Verified/Rejected). | PATIENT | MEDIUM |
| FR-32 | View pharmacist notes | Read feedback left by the pharmacist. | PATIENT | MEDIUM |
| FR-33 | Notifications | Receive system alerts and reminders. | PATIENT | HIGH |
| FR-34 | Refill alerts | Alerts when medication supplies run low. | PATIENT | LOW |
| FR-35 | Notification read/unread state | Track and toggle the read status of notifications. | PATIENT | LOW |
| FR-36 | Preference management | Update application notification settings. | PATIENT | LOW |

### Pharmacist Functional Requirements
| ID | Title | Description | Primary Actor | Priority |
|---|---|---|---|---|
| FR-37 | Pharmacist login | Authenticate pharmacist credentials. | PHARMACIST | HIGH |
| FR-38 | Pharmacist logout | Securely terminate the pharmacist's active session. | PHARMACIST | HIGH |
| FR-39 | Pharmacist dashboard | Summary of pending prescriptions, low stock, and sales. | PHARMACIST | HIGH |
| FR-40 | Medicine master management | Maintain the central catalog of pharmacy medicines. | PHARMACIST | HIGH |
| FR-41 | Inventory management | Oversee all pharmacy inventory operations. | PHARMACIST | HIGH |
| FR-42 | Inventory batch management | Add and track specific batches of medicines. | PHARMACIST | HIGH |
| FR-43 | Stock management | Update and maintain accurate stock levels. | PHARMACIST | HIGH |
| FR-44 | Low-stock monitoring | Track medicines falling below a threshold. | PHARMACIST | HIGH |
| FR-45 | Out-of-stock monitoring | Identify completely depleted items. | PHARMACIST | HIGH |
| FR-46 | Expiry monitoring | Track the expiration dates of all batches. | PHARMACIST | HIGH |
| FR-47 | Expired medicine identification | Flag medicines past their expiration date. | PHARMACIST | HIGH |
| FR-48 | Customer management | Maintain records of pharmacy customers. | PHARMACIST | MEDIUM |
| FR-49 | Customer search | Search for customers by name/phone/ID. | PHARMACIST | MEDIUM |
| FR-50 | Customer profile | View and manage detailed customer information. | PHARMACIST | LOW |
| FR-51 | Customer purchase history | Review past pharmacy sales for a specific customer. | PHARMACIST | LOW |
| FR-52 | Prescription review | Access and review patient-uploaded prescriptions. | PHARMACIST | HIGH |
| FR-53 | Prescription approval | Mark a reviewed prescription as verified. | PHARMACIST | HIGH |
| FR-54 | Prescription rejection | Reject an invalid or unreadable prescription. | PHARMACIST | MEDIUM |
| FR-55 | Prescription notes | Attach operational notes to a reviewed prescription. | PHARMACIST | MEDIUM |
| FR-56 | Sales management | Initiate and process pharmacy sales. | PHARMACIST | HIGH |
| FR-57 | Sale item management | Add/edit/remove medicines from an active transaction. | PHARMACIST | HIGH |
| FR-58 | Stock deduction after successful sale | Decrease inventory stock quantities upon sale. | PHARMACIST | HIGH |
| FR-59 | Billing | Calculate costs, taxes, and final amounts. | PHARMACIST | HIGH |
| FR-60 | Invoice generation | Create detailed invoices for completed sales. | PHARMACIST | HIGH |
| FR-61 | Payment status | Record whether an invoice has been paid. | PHARMACIST | MEDIUM |
| FR-62 | Sales reports | Generate summaries of sales over specific periods. | PHARMACIST | MEDIUM |
| FR-63 | Inventory reports | Generate summaries of stock levels and batches. | PHARMACIST | MEDIUM |
| FR-64 | Expiry reports | Generate reports listing medicines nearing expiry. | PHARMACIST | MEDIUM |
| FR-65 | Low-stock reports | Generate reports of items needing reorder. | PHARMACIST | MEDIUM |
| FR-66 | Prescription reports | Generate reports on prescription verifications. | PHARMACIST | LOW |
| FR-67 | Revenue analytics | Visual charts showing revenue trends. | PHARMACIST | MEDIUM |
| FR-68 | Sales analytics | Visual charts detailing sales volume over time. | PHARMACIST | MEDIUM |
| FR-69 | Top-selling medicine analytics | Identify and visually display most frequently sold items. | PHARMACIST | LOW |

## 8. Non-Functional Requirements
- **Security**: Mandatory backend authorization, secure password hashing, protection against web vulnerabilities.
- **Usability**: Intuitive, responsive user interfaces with separate portals.
- **Performance**: The system must maintain reasonable response times (e.g., under 1-2 seconds) for common operations (like loading dashboards or processing sales) under expected academic/project usage.
- **Reliability**: Transactional integrity in database operations via JDBC.
- **Maintainability**: Clean, modular OOP code utilizing MVC architectural patterns.
- **Data integrity**: Strict referential integrity maintained in the MySQL database.
- **Availability**: Standard local/network availability for academic demonstrations.
- **Responsiveness**: Frontend must seamlessly adapt to varying screen sizes.
- **Browser compatibility**: Consistent behavior across modern browsers.
- **Modularity**: Strict separation between Controller, Service, and DAO layers.
- **Scalability**: The system should support moderate academic/project-scale application data and allow additional modules to be added without major architectural restructuring.

## 9. Scope
Version 1 encompasses the complete, end-to-end functionality required for a fully operational integrated medicine reminder and pharmacy management system.

**Patient Scope:**
- Dashboard
- Medicine management
- Medication scheduling
- Medication reminders
- Medication adherence
- Medication history
- Prescription management
- Notifications
- Profile/preferences
- Refill alerts

**Pharmacist Scope:**
- Dashboard
- Medicine management
- Pharmacy inventory
- Batch management
- Stock management
- Expiry monitoring
- Customer management
- Prescription management
- Sales
- Billing
- Invoices
- Reports
- Analytics

**Authentication Scope:**
- Registration
- Login
- Logout
- Forgot password
- Reset password
- Change password
- Remember me
- Session management
- Role-based access
- Protected resources
- Form validation
- Password visibility toggle
- Login state persistence

## 10. Out of Scope
The following are excluded from Version 1:
- Real payment gateway
- Real SMS gateway
- Real email notification service (unless separately implemented)
- AI diagnosis
- Doctor consultation
- Hospital integration
- Insurance processing
- Mobile application
- Blockchain
- Microservices

## 11. Assumptions
- Users have internet/browser access.
- MySQL is available on the server/development machine.
- Pharmacists have authorized accounts.
- Prescription files are uploaded in supported formats.
- The first version is intended as an academic/demo system.

## 12. Constraints
- Four-member student development team.
- Academic project scope constraints.
- Local MySQL development environment.
- Limited external services.
- Java/Spring Boot backend.
- HTML/CSS/JavaScript frontend.

## 13. Limitations
As an academic Version 1 release, the application prioritizes robust core backend logic, relational data integrity, and OOP principles over third-party integrations. Offline caching and external notifications (SMS) are not supported. This focused scope ensures the project remains complete, reliable, and functional without appearing unfinished.

## 14. Future Enhancements
- Mobile application
- Email notifications
- SMS notifications
- Doctor portal
- Hospital integration
- Online payments
- Advanced analytics
- Cloud deployment
- Wearable integration
- AI-assisted medication insights
