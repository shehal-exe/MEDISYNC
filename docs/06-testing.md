# MEDISYNC - Testing Strategy

## Overview
This document outlines the testing approach for the MEDISYNC project to ensure high software quality across all layers of the application.

## Testing Types

### 1. Unit Testing
- **Focus**: Testing individual Java methods (Controllers, Services) and JavaScript functions in isolation.
- **Tools**: JUnit 5, Mockito (Backend), Jest (Frontend - if applicable in future phases, otherwise manual/vanilla JS testing frameworks).
- **Scope**: Business logic validation, boundary value analysis.

### 2. API Testing
- **Focus**: Verifying that REST API endpoints return correct status codes, JSON payloads, and handle errors gracefully.
- **Tools**: Postman, cURL, Spring MockMvc.
- **Scope**: Request validation, response formatting.

### 3. Frontend Testing
- **Focus**: Validating UI rendering, user interactions, and Chart.js diagram generation.
- **Tools**: Selenium (for E2E), manual browser testing.
- **Scope**: Cross-browser compatibility, responsive design, form validation.

### 4. Database Testing
- **Focus**: Verifying SQL queries, constraints, foreign keys, and indexes.
- **Tools**: DBeaver, automated JDBC tests with an in-memory DB (H2) or test instance.
- **Scope**: CRUD operations, transaction rollbacks.

### 5. Integration Testing
- **Focus**: Testing the flow between multiple components (e.g., Controller -> Service -> DAO -> Database).
- **Tools**: Spring Boot Test.
- **Scope**: Ensuring modules interact correctly.

### 6. Security Testing
- **Focus**: Ensuring data privacy, RBAC, and preventing common vulnerabilities (SQLi, XSS, CSRF).
- **Tools**: Spring Security test utilities, manual penetration testing.
- **Scope**: Authentication flows, token validation, unauthorized access attempts.

## Initial Test Case IDs

| Test Case ID | Module | Description | Type |
|---|---|---|---|
| TC-AUTH-01 | Authentication | Verify successful patient registration with valid data. | API / Integration |
| TC-AUTH-02 | Authentication | Verify login rejection with incorrect password. | API / Security |
| TC-INV-01 | Inventory | Verify pharmacist can add a new medicine batch. | Integration |
| TC-INV-02 | Inventory | Verify patient cannot access inventory addition endpoint. | Security |
| TC-PRES-01 | Prescription | Verify prescription upload accepts only valid file formats (PDF/JPG). | API / Unit |
| TC-SALE-01 | Sales | Verify successful transaction updates inventory stock correctly. | Integration / DB |
| TC-UI-01 | Frontend | Verify analytics dashboard renders charts without console errors. | Frontend |
