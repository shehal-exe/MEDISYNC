# MEDISYNC - Testing Strategy

## Overview
This document outlines the testing approach for the MEDISYNC project, utilizing the approved technology stack to ensure robust functionality, security, and performance.

## Testing Stack
**Backend:**
- JUnit 5
- Mockito (where useful)
- Spring Boot Test
- MockMvc

**API:**
- Postman
- MockMvc

**Frontend:**
- Browser / manual testing
- Form validation checks
- Interaction testing
- Responsive testing
- Console-error checks
- Chart.js rendering checks

**Database:**
- DBeaver
- MySQL
- DAO/JDBC tests
- Constraint verification
- Transaction verification

**Security:**
- Spring Security testing
- Authentication/Authorization tests
- Patient ownership isolation tests
- Pharmacist-only endpoint tests
- Invalid session tests
- Password reset tests
- Input validation
- SQL injection resistance (via PreparedStatements)
- XSS-safe output handling
- CSRF/session behavior appropriate to the architecture

*(Note: Jest, Selenium, and H2 are deliberately EXCLUDED from the required stack unless a strong, documented reason arises later).*

## Testing Matrix

| Test Case ID | Module | Description | Type |
|---|---|---|---|
| TC-AUTH-01 | Authentication | Verify successful patient registration with valid data. | API / Integration |
| TC-AUTH-02 | Authentication | Verify login rejection with incorrect password. | API / Security |
| TC-AUTH-03 | Authentication | Verify registration validation logic. | Unit / Integration |
| TC-AUTH-04 | Authentication | Verify duplicate email prevention during registration. | API / DB |
| TC-AUTH-05 | Authentication | Verify logout invalidates the session securely. | Security |
| TC-AUTH-06 | Authentication | Verify remember-me/login persistence mechanism. | API / Security |
| TC-AUTH-07 | Authentication | Verify forgot password generates a token and expired tokens are rejected. | Security |
| TC-SEC-01 | Security | Verify patient ownership isolation (cannot access other patients' data). | Security / API |
| TC-SEC-02 | Security | Verify pharmacist role restriction on pharmacist-only endpoints. | Security |
| TC-PAT-01 | Patient | Verify medicine CRUD operations for a patient. | API / DB |
| TC-PAT-02 | Patient | Verify schedule CRUD operations for a patient. | API / DB |
| TC-PAT-03 | Patient | Verify 'taken' reminder successfully updates medication log. | Integration |
| TC-PAT-04 | Patient | Verify 'skipped' and 'missed' reminders update correctly. | Integration |
| TC-PAT-05 | Patient | Verify medication adherence calculation logic. | Unit |
| TC-PRES-01 | Prescription | Verify prescription upload validation and ownership. | API / Security |
| TC-PRES-02 | Prescription | Verify invalid prescription file format is rejected. | API |
| TC-INV-01 | Inventory | Verify pharmacist can add a new medicine batch. | Integration |
| TC-INV-02 | Inventory | Verify patient cannot access inventory addition endpoint. | Security |
| TC-INV-03 | Inventory | Verify low stock reporting triggers correctly. | Integration / DB |
| TC-INV-04 | Inventory | Verify expiry reporting identifies batches near or past expiration. | Integration / DB |
| TC-CUST-01 | Pharmacist | Verify customer management operations. | API / DB |
| TC-SALE-01 | Sales | Verify successful transaction updates inventory stock correctly. | Integration / DB |
| TC-SALE-02 | Sales | Verify insufficient stock correctly prevents a sale. | Business Logic |
| TC-SALE-03 | Sales | Verify sale transaction rolls back completely on any failure. | DB / Integration |
| TC-BILL-01 | Billing | Verify invoice generation creates accurate totals and line items. | Integration |
| TC-REP-01 | Reports | Verify reports and analytics endpoints return formatted data. | API |
| TC-NOT-01 | Notifications | Verify notifications are generated and marked read correctly. | Integration |
| TC-UI-01 | Frontend | Verify analytics dashboard renders charts without console errors. | Frontend |
