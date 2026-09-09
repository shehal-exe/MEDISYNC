# MEDISYNC

## Project Name
MEDISYNC

## Project Purpose
MEDISYNC is a comprehensive healthcare management system that bridges the gap between patients and pharmacists. It provides robust inventory management, secure prescription verification, and sales tracking for pharmacies, while empowering patients to log medications, view prescriptions, and receive reminders.

## Technology Stack
- **Frontend**: HTML5, CSS3, Vanilla JavaScript, Chart.js
- **Backend**: Java 17, Spring Boot, Maven, Spring Web, Spring Security, Validation
- **Database**: MySQL, accessed via pure JDBC
- **Database Management**: DBeaver
- **Version Control**: Git + GitHub

## Architecture
MEDISYNC follows a standard three-tier architecture:
- **Presentation Layer**: Vanilla HTML/JS communicating asynchronously via REST.
- **Application Layer**: Spring Boot REST API orchestrating business logic (Controller -> Service -> DAO).
- **Data Layer**: MySQL database handling structured storage.

## Major Features
- **User Roles**: Separate portals for Patients and Pharmacists.
- **Inventory Management**: Track medicines, batches, stock levels, and expiries.
- **Prescription Workflow**: Patients upload prescriptions; Pharmacists verify them.
- **Point of Sale (POS)**: Streamlined checkout and automated invoicing for pharmacies.
- **Medication Tracking**: Scheduling and logging system for patients.
- **Analytics**: Dashboard reporting powered by Chart.js.

## Project Structure
```text
MEDISYNC/
├── backend/        # Spring Boot Java application
├── database/       # SQL schemas, seed data, and queries
├── docs/           # Architecture, API, DB, and Requirement documentation
├── frontend/       # HTML, CSS, JS static assets
└── screenshots/    # Application previews and mockups
```

## Development Status
**Phase 1: PROJECT INITIALIZATION AND DOCUMENTATION**
Currently in the planning and design phase. The requirements, architecture, API specifications, database schemas, and diagrams have been documented. Application logic and feature development have not yet commenced.
