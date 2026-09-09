# MEDISYNC - System Architecture

## Overall Architecture
MEDISYNC follows a classic three-tier architecture comprising a Presentation Layer (Frontend), an Application Layer (Backend), and a Data Access Layer (Database). The system is built using vanilla web technologies on the frontend communicating via a REST API to a Spring Boot backend, which in turn connects to a MySQL database using JDBC.

```text
HTML/CSS/JavaScript
        ↓
    REST API
        ↓
   Spring Boot
        ↓
    Controller
        ↓
     Service
        ↓
       DAO
        ↓
      JDBC
        ↓
      MySQL
```

## Frontend
- **Technologies**: HTML5, CSS3, Vanilla JavaScript, Chart.js.
- **Responsibility**: Provides the user interface for both Patients and Pharmacists. It handles user interactions, client-side validation, data visualization (using Chart.js for reports/analytics), and makes asynchronous HTTP requests to the REST API.

## REST API
- **Responsibility**: Serves as the communication bridge between the frontend and the backend. It uses standard HTTP methods (GET, POST, PUT, DELETE) and exchanges data in JSON format.

## Spring Boot
- **Technologies**: Java 17, Spring Boot, Maven, Spring Web, Spring Security, Validation.
- **Responsibility**: Acts as the core application framework, bootstrapping the application, providing dependency injection, and configuring web server and security settings.

### Controller Layer
- **Responsibility**: Exposes the REST API endpoints. It intercepts incoming HTTP requests, performs basic request validation using Spring Validation, delegates business logic processing to the Service Layer, and returns the appropriate HTTP response codes and JSON payloads.

### Service Layer
- **Responsibility**: Contains the core business logic of the application. It enforces business rules, processes data received from controllers, orchestrates multiple DAOs if necessary, and handles transaction boundaries.

### DAO Layer (Data Access Object)
- **Responsibility**: Abstracts the interaction with the database. It contains the specific SQL queries and statements required to perform CRUD (Create, Read, Update, Delete) operations on the database entities.

## JDBC
- **Responsibility**: The underlying API used by the DAO layer to connect to and execute SQL commands against the MySQL database. No ORM (like Hibernate) is used, ensuring explicit control over SQL queries.

## MySQL
- **Responsibility**: The relational database management system used to persist all application data, maintaining data integrity through constraints and relationships.

## DBeaver
- **Responsibility**: The primary database management tool used by developers and database administrators to interact with, design, and manage the MySQL database during development.

## Security
- **Technologies**: Spring Security.
- **Responsibility**: Secures the REST API by authenticating users and enforcing Role-Based Access Control (RBAC). It ensures that PATIENT endpoints are only accessible by patients, and PHARMACIST endpoints are restricted to pharmacists.

## Module Responsibilities
- **Authentication Module**: Manages login, registration, password hashing, and session/token management.
- **User Management Module**: Handles patient and pharmacist profiles.
- **Inventory Module**: Manages medicines, batches, and stock levels.
- **Prescription Module**: Handles upload, storage, and verification of prescriptions.
- **Sales & Billing Module**: Manages customer sales, cart processing, and invoice generation.
- **Medication Schedule Module**: Tracks patient medication logs and schedules.
