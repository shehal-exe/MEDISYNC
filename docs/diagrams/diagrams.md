# MEDISYNC - Diagrams (Mermaid)

## 1. Use Case Diagram
```mermaid
usecaseDiagram
    actor Patient
    actor Pharmacist

    Patient --> (Register)
    Patient --> (Login)
    Patient --> (View Medicines)
    Patient --> (Upload Prescription)
    Patient --> (Log Medication)

    Pharmacist --> (Login)
    Pharmacist --> (Manage Inventory)
    Pharmacist --> (Verify Prescription)
    Pharmacist --> (Process Sale)
    Pharmacist --> (View Analytics)
```

## 2. ER Diagram
```mermaid
erDiagram
    USER ||--o{ ROLE : has
    USER ||--o| PATIENT_PROFILE : acts_as
    USER ||--o| PHARMACIST_PROFILE : acts_as
    PATIENT_PROFILE ||--o{ PRESCRIPTION : uploads
    PHARMACIST_PROFILE ||--o{ PRESCRIPTION : verifies
    MEDICINE ||--o{ INVENTORY_BATCH : has
    PATIENT_PROFILE ||--o{ PATIENT_MEDICINE : takes
    MEDICINE ||--o{ PATIENT_MEDICINE : is
    PATIENT_MEDICINE ||--o{ MEDICATION_SCHEDULE : scheduled_by
    MEDICATION_SCHEDULE ||--o{ MEDICATION_LOG : tracked_by
    SALE ||--o{ SALE_ITEM : contains
    SALE ||--|| INVOICE : generates
    INVENTORY_BATCH ||--o{ SALE_ITEM : deducted_from
```

## 3. Class Diagram (Backend Snippet)
```mermaid
classDiagram
    class UserController {
        +register(UserDto)
        +login(Credentials)
    }
    class UserService {
        +createUser(UserDto)
        +authenticate()
    }
    class UserRepository {
        +findByEmail(String)
        +save(User)
    }
    class User {
        -Long id
        -String email
        -String passwordHash
    }

    UserController --> UserService
    UserService --> UserRepository
    UserRepository --> User
```

## 4. System Architecture Diagram
```mermaid
graph TD
    UI[Frontend: HTML/CSS/JS/Chart.js] -->|HTTP/REST| API[Spring Boot REST API]
    API --> Controller[Controller Layer]
    Controller --> Service[Service Layer]
    Service --> DAO[DAO / JDBC Layer]
    DAO --> DB[(MySQL Database)]
```

## 5. Login Sequence Diagram
```mermaid
sequenceDiagram
    actor User
    participant UI as Frontend
    participant API as AuthController
    participant Svc as AuthService
    participant DB as Database

    User->>UI: Enter email & password
    UI->>API: POST /api/v1/auth/login
    API->>Svc: authenticate(credentials)
    Svc->>DB: findByEmail(email)
    DB-->>Svc: User details
    Svc-->>API: Token (if password valid)
    API-->>UI: 200 OK + Token
    UI-->>User: Redirect to Dashboard
```

## 6. Patient Medication Sequence Diagram
```mermaid
sequenceDiagram
    actor Patient
    participant UI as Frontend
    participant API as MedController
    participant Svc as MedService
    participant DB as Database

    Patient->>UI: Click "Log Medication Taken"
    UI->>API: POST /api/v1/medication/log
    API->>Svc: saveLog(patientId, scheduleId)
    Svc->>DB: INSERT into MedicationLog
    DB-->>Svc: Success
    Svc-->>API: Status 201
    API-->>UI: Update view
    UI-->>Patient: Show Success Msg
```

## 7. Pharmacy Sale Sequence Diagram
```mermaid
sequenceDiagram
    actor Pharmacist
    participant UI as Frontend
    participant API as SaleController
    participant Svc as SaleService
    participant DB as Database

    Pharmacist->>UI: Checkout Cart
    UI->>API: POST /api/v1/sales (items)
    API->>Svc: processSale(items)
    Svc->>DB: Check InventoryStock
    DB-->>Svc: Stock OK
    Svc->>DB: Deduct Stock
    Svc->>DB: Insert Sale & Invoice
    DB-->>Svc: Sale ID
    Svc-->>API: 201 Created (Sale ID)
    API-->>UI: Show Invoice
    UI-->>Pharmacist: Sale Complete
```

## 8. Patient Medication Activity Diagram
```mermaid
stateDiagram-v2
    [*] --> CheckSchedule
    CheckSchedule --> NotificationSent: Time to take med
    NotificationSent --> PatientLogsIn
    PatientLogsIn --> MarkTaken
    MarkTaken --> RecordSaved
    RecordSaved --> [*]
```

## 9. Pharmacy Sale Activity Diagram
```mermaid
stateDiagram-v2
    [*] --> ScanItems
    ScanItems --> CheckStock
    CheckStock --> StockAvailable: Yes
    CheckStock --> OutOfStock: No
    OutOfStock --> [*]
    StockAvailable --> CalculateTotal
    CalculateTotal --> ProcessPayment
    ProcessPayment --> DeductInventory
    DeductInventory --> GenerateInvoice
    GenerateInvoice --> [*]
```
