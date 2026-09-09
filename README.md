# MEDISYNC

**Integrated Medicine Reminder and Pharmacy Management System**

MEDISYNC is a web-based healthcare management system bridging the gap between patients and pharmacists through one centralized application with separate role-based portals. It is designed as a B.Tech Computer Science and Engineering OOP project.

## Features
- **Patient Portal**: Dashboard, personal medication scheduling, reminders, adherence tracking, medication history, and prescription uploads.
- **Pharmacist Portal**: Dashboard, pharmacy inventory/batch management, prescription verification, point-of-sale (POS) processing, billing, invoicing, reports, and analytics.

## Technology Stack
- **Frontend**: HTML5, CSS3, Vanilla JavaScript, Chart.js
- **Backend**: Java 17, Spring Boot, Maven, Spring Security, Validation
- **Database**: MySQL, connected via JDBC (No ORM/Hibernate used)
- **Database Management**: DBeaver
- **Testing**: Postman, Browser testing, JUnit 5

## Architecture
MEDISYNC utilizes a classic 3-tier MVC architecture:
1. **Frontend**: Plain HTML/CSS/JS communicating asynchronously via REST.
2. **Backend**: Spring Boot handling HTTP routing (Controllers), business logic (Services), and Database access (DAOs).
3. **Database**: MySQL relational database holding structured application data.

## Folder Structure
```
MEDISYNC/
├── backend/          # Java Spring Boot application (source code, Maven POM)
├── database/         # MySQL schemas, sample data, and queries
├── docs/             # Requirements, API specs, architectural and testing documents
├── frontend/         # HTML/CSS/JS static web assets
└── screenshots/      # UI screenshots and visual assets
```

## Prerequisites
- Java Development Kit (JDK) 17
- Maven 3.8+
- MySQL 8+
- DBeaver (or any MySQL client)

## Setup & Run Instructions

### 1. Database Setup
1. Open DBeaver or your MySQL client and connect as `root` (or a user with DDL privileges).
2. Execute the script `database/schema.sql` to create the `medisync_db` database and all required tables.
3. (Optional) Execute `database/sample-data.sql` to populate the database with realistic demo data.

### 2. Configure Environment Variables
You must configure the backend to connect to your local MySQL instance. Do NOT hardcode your passwords into `application.properties`. 
Set the following environment variables on your system:
- `DB_URL` (default is `jdbc:mysql://localhost:3306/medisync_db`)
- `DB_USERNAME` (e.g., `root`)
- `DB_PASSWORD` (your local MySQL password)

### 3. How to Run the Backend
Navigate to the `backend/` directory in your terminal:
```bash
cd backend
mvn spring-boot:run
```
The server will start on `http://localhost:8080`. You can check the health endpoint at `http://localhost:8080/api/v1/health`.

### 4. How to Serve the Frontend
Since the frontend uses vanilla HTML/JS, you can serve it using any simple static file server.
For example, using Python:
```bash
cd frontend
python -m http.server 8000
```
Then navigate to `http://localhost:8000/index.html` in your browser.

## Testing
- **Java Tests**: Run unit and integration tests using Maven: `mvn test`
- **API Testing**: Import the provided endpoints into Postman to test backend REST responses.

## Git Workflow
We use a feature-branch workflow.
- **DO NOT** commit directly to `main`.
- Create feature branches off `develop` (e.g., `git checkout -b feature/patient-login`).
- After completing work, commit with clear prefixes (e.g., `feat: ...`, `fix: ...`) and open a Pull Request against `develop`.
