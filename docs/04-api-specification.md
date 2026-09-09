# MEDISYNC - API Specification

## 1. Authentication
### Register Patient
- **Method**: POST
- **Endpoint**: `/api/v1/auth/register`
- **Purpose**: Create a new PATIENT account.
- **Authentication**: None
- **Role**: PUBLIC
- **Request**: `{ "email": "...", "password": "...", "firstName": "...", "lastName": "..." }`
- **Response**: `201 Created` - `{ "message": "User registered successfully" }`
- **Expected Errors**: `400 Bad Request` (Invalid data), `409 Conflict` (Email exists)

### Login
- **Method**: POST
- **Endpoint**: `/api/v1/auth/login`
- **Purpose**: Authenticate user and return token.
- **Authentication**: None
- **Role**: PUBLIC
- **Request**: `{ "email": "...", "password": "..." }`
- **Response**: `200 OK` - `{ "token": "...", "role": "..." }`
- **Expected Errors**: `401 Unauthorized` (Invalid credentials)

## 2. Patient
### Get Patient Profile
- **Method**: GET
- **Endpoint**: `/api/v1/patients/me`
- **Purpose**: Retrieve current patient's profile details.
- **Authentication**: Bearer Token
- **Role**: PATIENT
- **Request**: None
- **Response**: `200 OK` - `{ "id": 1, "firstName": "...", "contactNumber": "..." }`
- **Expected Errors**: `401 Unauthorized`, `404 Not Found`

## 3. Pharmacist
### Get Pharmacist Profile
- **Method**: GET
- **Endpoint**: `/api/v1/pharmacists/me`
- **Purpose**: Retrieve current pharmacist's profile.
- **Authentication**: Bearer Token
- **Role**: PHARMACIST
- **Request**: None
- **Response**: `200 OK` - `{ "id": 1, "licenseNumber": "..." }`
- **Expected Errors**: `401 Unauthorized`

## 4. Inventory
### List Medicines
- **Method**: GET
- **Endpoint**: `/api/v1/inventory/medicines`
- **Purpose**: Get a list of available medicines.
- **Authentication**: Bearer Token
- **Role**: PATIENT, PHARMACIST
- **Request**: Query params (optional): `?search=xyz`
- **Response**: `200 OK` - `[ { "id": 1, "name": "...", "stock": 50 } ]`
- **Expected Errors**: `401 Unauthorized`

### Add Medicine Batch
- **Method**: POST
- **Endpoint**: `/api/v1/inventory/batches`
- **Purpose**: Add a new inventory batch for a medicine.
- **Authentication**: Bearer Token
- **Role**: PHARMACIST
- **Request**: `{ "medicineId": 1, "quantity": 100, "expiryDate": "YYYY-MM-DD", "price": 10.5 }`
- **Response**: `201 Created`
- **Expected Errors**: `400 Bad Request`, `403 Forbidden`

## 5. Prescription
### Upload Prescription
- **Method**: POST
- **Endpoint**: `/api/v1/prescriptions`
- **Purpose**: Patient uploads a prescription document.
- **Authentication**: Bearer Token
- **Role**: PATIENT
- **Request**: `multipart/form-data` (file)
- **Response**: `201 Created`
- **Expected Errors**: `400 Bad Request` (Invalid file format)

### Verify Prescription
- **Method**: PUT
- **Endpoint**: `/api/v1/prescriptions/{id}/verify`
- **Purpose**: Pharmacist verifies a prescription.
- **Authentication**: Bearer Token
- **Role**: PHARMACIST
- **Request**: `{ "status": "VERIFIED" }`
- **Response**: `200 OK`
- **Expected Errors**: `403 Forbidden`, `404 Not Found`

## 6. Sales
### Process Sale
- **Method**: POST
- **Endpoint**: `/api/v1/sales`
- **Purpose**: Record a pharmacy transaction.
- **Authentication**: Bearer Token
- **Role**: PHARMACIST
- **Request**: `{ "customerId": 1, "items": [ {"batchId": 12, "quantity": 2} ] }`
- **Response**: `201 Created` - `{ "saleId": 101, "total": 21.0 }`
- **Expected Errors**: `400 Bad Request` (Insufficient stock)

## 7. Billing
### Generate Invoice
- **Method**: GET
- **Endpoint**: `/api/v1/billing/invoices/{saleId}`
- **Purpose**: Retrieve an invoice for a specific sale.
- **Authentication**: Bearer Token
- **Role**: PHARMACIST
- **Request**: None
- **Response**: `200 OK` - `{ "invoiceNumber": "...", "grandTotal": 21.0, ... }`
- **Expected Errors**: `404 Not Found`

## 8. Reports
### Get Inventory Report
- **Method**: GET
- **Endpoint**: `/api/v1/reports/inventory`
- **Purpose**: Get current stock levels and near-expiry alerts.
- **Authentication**: Bearer Token
- **Role**: PHARMACIST
- **Request**: None
- **Response**: `200 OK` - `{ "lowStock": [...], "expiringSoon": [...] }`
- **Expected Errors**: `403 Forbidden`

## 9. Analytics
### Get Sales Analytics
- **Method**: GET
- **Endpoint**: `/api/v1/analytics/sales`
- **Purpose**: Get sales trends for charts (Chart.js).
- **Authentication**: Bearer Token
- **Role**: PHARMACIST
- **Request**: Query params: `?startDate=...&endDate=...`
- **Response**: `200 OK` - `{ "labels": [...], "data": [...] }`
- **Expected Errors**: `403 Forbidden`

## 10. Notifications
### Get My Notifications
- **Method**: GET
- **Endpoint**: `/api/v1/notifications`
- **Purpose**: Fetch alerts and medication reminders.
- **Authentication**: Bearer Token
- **Role**: PATIENT, PHARMACIST
- **Request**: None
- **Response**: `200 OK` - `[ { "id": 1, "message": "Time for medication", "isRead": false } ]`
- **Expected Errors**: `401 Unauthorized`
