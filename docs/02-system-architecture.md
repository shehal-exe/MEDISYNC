# MEDISYNC - System Architecture

## 1. Overall Architecture
MEDISYNC is a three-tier web architecture with a layered Spring Boot backend.

```text
PATIENT PORTAL
        \
         \
          HTML/CSS/JavaScript
         /
PHARMACIST PORTAL
        |
        v
     REST API
        |
        v
    Spring Boot
        |
        +----------------+
        |                |
        v                v
   Controller         Security
        |
        v
     Service
        |
        v
       DAO
        |
        v
      JDBC
        |
        v
      MySQL
```

*(Note: DBeaver is a database management/development tool and is not part of runtime application communication).*

## 2. Approved Stack
**Frontend:**
- HTML5
- CSS3
- Vanilla JavaScript
- Chart.js

**Backend:**
- Java 17
- Spring Boot
- Maven
- Spring Security
- Validation

**Database:**
- MySQL

**Database Access:**
- JDBC

**Database Management:**
- DBeaver

**Testing:**
- Postman
- Browser
- JUnit

**Version Control:**
- Git
- GitHub

*(Technologies strictly not introduced: React, Angular, Vue, JPA, Hibernate, PostgreSQL, MongoDB, Docker, Kubernetes, Redis, Kafka, Microservices).*

## 3. Frontend
**Technologies**: HTML5, CSS3, Vanilla JavaScript, Chart.js

**Responsibilities:**
- User interface
- Navigation
- Forms
- Client-side validation
- API communication
- Displaying API responses
- Charts and analytics
- Loading/error/empty states

*Explicit Constraint: The frontend never connects directly to MySQL.*

## 4. REST API
**Responsibilities:**
- REST communication
- GET, POST, PUT, DELETE methods
- JSON data exchange
- HTTP status codes
- Request validation
- Error responses
- Authentication/session context

## 5. Controller Layer
**Responsibilities:**
- Expose REST endpoints
- Receive requests
- Perform basic request validation
- Call services
- Return responses

*Controllers MUST NOT contain database access or complex business logic.*

## 6. Service Layer
**Responsibilities:**
- Contain business logic
- Enforce business rules
- Perform calculations (e.g., medication adherence calculation, invoice calculation)
- Coordinate DAOs
- Enforce ownership/business checks
- Manage logical workflows (e.g., prescription status workflow, sales workflow, stock validation)
- Coordinate transaction-sensitive operations

*Note: JDBC transaction management will be used where one business operation updates multiple related records. For example, during a checkout:*
```text
Sale
→ Sale Items
→ Inventory/Stock Deduction
→ Invoice
```
*These related database changes must be handled transactionally so the operation either completes correctly or rolls back consistently.*

## 7. DAO Layer
**Responsibilities:**
- Encapsulates database access
- Contains SQL
- Uses JDBC
- Uses PreparedStatement
- Processes ResultSet
- Handles CRUD/query operations

*Security constraint: SQL must never be constructed through unsafe user-input string concatenation.*

## 8. JDBC
**Key Components:**
- Connection
- PreparedStatement
- ResultSet
- SQLException
- Database connection management

*Explicit Constraint: No JPA or Hibernate ORM is used.*

## 9. MySQL
MySQL is the persistent relational data store.

**Features utilized:**
- Primary keys
- Foreign keys
- Constraints
- Indexes
- Referential integrity
- Transactional consistency

## 10. Security Architecture
**Components:**
- Spring Security
- Authentication
- Authorization
- RBAC (Role-Based Access Control)
- Password hashing
- Session management
- Logout
- Remember me/login persistence
- Forgot password
- Reset password
- Change password
- Backend endpoint protection
- Ownership checks
- Input validation
- Secure file upload validation

**Roles:**
- PATIENT
- PHARMACIST

**Security Rules:**
- Public registration creates PATIENT accounts only.
- Public users cannot select PHARMACIST during registration.
- PATIENT APIs are restricted to PATIENT users.
- PHARMACIST APIs are restricted to PHARMACIST users.
- Backend authorization is mandatory.
- Frontend hiding buttons is NOT sufficient security.
- Patients may only access their own patient-specific data.

## 11. Portal Architecture

### PATIENT PORTAL
**Modules:**
- Dashboard
- Medicine management
- Medication scheduling
- Reminders
- Adherence
- Medication history
- Prescription management
- Notifications
- Profile/preferences
- Refill alerts

### PHARMACIST PORTAL
**Modules:**
- Dashboard
- Medicine management
- Inventory
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

## 12. Module Responsibilities
1. **Authentication and Role Management**: Handles registration, secure login, sessions, and access control.
2. **User/Profile Management**: Manages updates to user-specific profile details.
3. **Patient Medication Management**: Allows patients to manage their personal list of medicines.
4. **Medication Scheduling**: Defines the frequency and timing for medication intake.
5. **Medication Reminder Management**: Generates alerts for scheduled medications.
6. **Medication Adherence**: Calculates metrics on how strictly a patient follows their schedule.
7. **Medication History**: Provides historical data and logs of past medication usage.
8. **Prescription Management**: Handles upload, review, and verification of digital prescriptions.
   - Prescription metadata/status is stored in MySQL.
   - Uploaded prescription files are stored in application/server-side file storage for Version 1.
   - Supported formats are PDF, PNG, JPG and JPEG.
   - Access is authenticated and ownership-controlled.
   - Arbitrary/unvalidated file uploads are not permitted.
9. **Notification Management**: Handles the generation, delivery, and read-state of system alerts.
10. **Pharmacy Medicine Management**: Manages the central catalog of medicines the pharmacy sells.
11. **Inventory Management**: Oversees all operations related to pharmacy stock.
12. **Batch Management**: Tracks specific batches of medicines.
13. **Stock Management**: Maintains accurate quantities of items in the pharmacy.
14. **Expiry Monitoring**: Tracks and flags items nearing or past their expiration dates.
15. **Customer Management**: Records details of pharmacy customers.
16. **Sales Management**: Processes over-the-counter pharmacy transactions and deductions.
17. **Billing and Invoice Management**: Handles cost calculation, taxes, and formal invoice generation.
18. **Reporting**: Generates data summaries for inventory, stock, and sales.
19. **Analytics**: Visualizes trends like revenue and top-selling items using Chart.js.

## 13. Data Flow
**General Request Flow:**
```text
Patient
→ Browser
→ JavaScript
→ REST API
→ Controller
→ Service
→ DAO
→ JDBC
→ MySQL
```

**Response Flow:**
```text
MySQL
→ JDBC
→ DAO
→ Service
→ Controller
→ JSON
→ JavaScript
→ UI
```

**Cross-Portal Interaction Flow (Patient ↔ Pharmacist):**
This is a core cross-portal interaction in MEDISYNC.
```text
Patient
→ uploads prescription
→ REST API
→ Spring Boot
→ prescription storage
→ Pharmacist Portal
→ pharmacist reviews prescription
→ pharmacist approves/rejects
→ database status update
→ Patient Portal displays the updated status
```

## 14. Security and Data Ownership Flow
**Access Flow:**
```text
Authenticated user
→ Spring Security
→ role check
→ ownership/business validation
→ controller/service
→ DAO
→ database
```
*Note: Patient records are strictly isolated by authenticated patient identity to ensure data privacy.*

## 15. OOP / Design Principles
The architecture supports robust software design through:
- **Encapsulation**: Grouping related logic and restricting direct field access.
- **Abstraction**: Hiding complex implementations behind simpler interfaces.
- **Interfaces**: Services and DAOs may be defined through interfaces to establish clear contracts and support abstraction and polymorphism.
- **Polymorphism**: Interacting with implementations via their interfaces.
- **Appropriate inheritance**: Used only when a strict 'is-a' relationship exists (do not force inheritance where it has no meaningful relationship).
- **Composition**: Using HAS-A relationships across layers.
- **Separation of concerns**: Strict boundaries between UI, API, business logic, and data access.
- **Exception handling**: Graceful catching, logging, and mapping of errors to appropriate HTTP responses.

## 16. Database Management Tool
**DBeaver** is documented as a development and administration tool only. It is NOT part of the runtime request path.
