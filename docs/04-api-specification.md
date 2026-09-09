# MEDISYNC - API Specification

## Conventions

### Base URL
`/api/v1`

### Authentication
MEDISYNC uses Spring Security session-based authentication with secure session cookies (and 'remember-me' where requested). JWT is strictly NOT introduced.
- **PATIENT endpoints** derive patient identity from the authenticated session, never trusting a user-supplied `patient_id`.
- **PHARMACIST endpoints** derive pharmacist identity from the session.

### Response Formats
**Success Format:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

**Error Format:**
```json
{
  "success": false,
  "message": "Error description",
  "errorCode": "ERROR_CODE"
}
```

---

## 1. Authentication (AUTH)

### Register Patient
- **Method**: POST
- **Endpoint**: `/api/v1/auth/register`
- **Purpose**: Register a new patient account (only PATIENT role).
- **Authentication**: None
- **Role**: PUBLIC
- **Request Body**: `{ "email": "...", "password": "...", "firstName": "...", "lastName": "..." }`
- **Expected Errors**: `400 Bad Request` (Validation failed), `409 Conflict` (Email exists).

### Login
- **Method**: POST
- **Endpoint**: `/api/v1/auth/login`
- **Purpose**: Authenticate user, establish session cookie.
- **Request Body**: `{ "email": "...", "password": "...", "rememberMe": true }`

### Logout
- **Method**: POST
- **Endpoint**: `/api/v1/auth/logout`
- **Purpose**: Invalidate current session.

### Get Current Session User
- **Method**: GET
- **Endpoint**: `/api/v1/auth/me`
- **Purpose**: Get current authenticated user details and role.

### Forgot Password
- **Method**: POST
- **Endpoint**: `/api/v1/auth/forgot-password`
- **Purpose**: Request password reset token.

### Reset Password
- **Method**: POST
- **Endpoint**: `/api/v1/auth/reset-password`
- **Purpose**: Reset password using token.

### Change Password
- **Method**: POST
- **Endpoint**: `/api/v1/auth/change-password`
- **Purpose**: Change password for authenticated user.

---

## 2. Patient Portal (PATIENT)
*Authentication: Mandatory. Role: PATIENT.*

### Patient Profile
- `GET /api/v1/patients/me`: Get own profile.
- `PUT /api/v1/patients/me`: Update profile.

### Patient Medicines
- `GET /api/v1/patient/medicines`: List all personal medicines.
- `POST /api/v1/patient/medicines`: Add medicine to personal list.
- `GET /api/v1/patient/medicines/{id}`: View specific personal medicine details.
- `PUT /api/v1/patient/medicines/{id}`: Edit personal medicine.
- `DELETE /api/v1/patient/medicines/{id}`: Remove personal medicine.

### Schedules
- `GET /api/v1/patient/schedules`: List personal medication schedules.
- `POST /api/v1/patient/schedules`: Create schedule.
- `PUT /api/v1/patient/schedules/{id}`: Edit schedule.
- `DELETE /api/v1/patient/schedules/{id}`: Remove schedule.

### Reminders & Adherence
- `GET /api/v1/patient/reminders`: List upcoming/due reminders.
- `POST /api/v1/patient/reminders/{id}/taken`: Mark medication as TAKEN.
- `POST /api/v1/patient/reminders/{id}/skipped`: Mark medication as SKIPPED (or MISSED).
- `GET /api/v1/patient/adherence`: Get adherence percentage (taken expected events / total expected events × 100).
- `GET /api/v1/patient/history`: View medication log history.

### Prescriptions
- `GET /api/v1/patient/prescriptions`: List own uploaded prescriptions.
- `POST /api/v1/patient/prescriptions`: Upload a new prescription (multipart/form-data). Supported formats: PDF, PNG, JPG, JPEG.
- `GET /api/v1/patient/prescriptions/{id}`: View prescription details.

### Notifications
- `GET /api/v1/notifications`: List personal notifications.
- `PUT /api/v1/notifications/{id}/read`: Mark notification as read.

---

## 3. Pharmacist Portal (PHARMACIST)
*Authentication: Mandatory. Role: PHARMACIST.*

### Dashboard & Profile
- `GET /api/v1/pharmacists/me`: Get own profile.
- `GET /api/v1/pharmacist/dashboard`: Get summary statistics (pending prescriptions, low stock, daily sales).

### Medicine Master & Inventory
- `GET /api/v1/inventory/medicines`: List master catalog of medicines.
- `POST /api/v1/inventory/medicines`: Add new medicine to catalog.
- `PUT /api/v1/inventory/medicines/{id}`: Edit medicine details.
- `DELETE /api/v1/inventory/medicines/{id}`: Remove medicine from catalog.

### Batch Management
- `GET /api/v1/inventory/batches`: List batches.
- `POST /api/v1/inventory/batches`: Add new batch (includes quantity, expiry, price).
- `PUT /api/v1/inventory/batches/{id}`: Edit batch details.
- `DELETE /api/v1/inventory/batches/{id}`: Delete batch.

### Stock & Expiry Monitoring
- `GET /api/v1/inventory/stock/low`: List low stock medicines.
- `GET /api/v1/inventory/stock/out-of-stock`: List out of stock medicines.
- `GET /api/v1/inventory/expiry`: List near-expiry batches.
- `GET /api/v1/inventory/expired`: List expired batches.

### Customers
- `GET /api/v1/customers`: List pharmacy customers.
- `POST /api/v1/customers`: Register new customer.
- `GET /api/v1/customers/{id}`: Get customer profile and purchase history.
- `PUT /api/v1/customers/{id}`: Update customer profile.

### Prescription Review
- `GET /api/v1/pharmacist/prescriptions`: List uploaded patient prescriptions.
- `PUT /api/v1/pharmacist/prescriptions/{id}/approve`: Verify/Approve a prescription.
- `PUT /api/v1/pharmacist/prescriptions/{id}/reject`: Reject a prescription.

### Sales & Billing
- `POST /api/v1/sales`: Process a new transactional sale (deducts stock, creates sale, generates invoice).
- `GET /api/v1/sales`: List sales history.
- `GET /api/v1/sales/{id}`: View specific sale details.
- `POST /api/v1/billing/invoices`: Generate invoice (if separate from initial sale logic).
- `GET /api/v1/billing/invoices/{id}`: View/Download invoice.

### Reports & Analytics
- `GET /api/v1/reports/sales`: Generate sales report data.
- `GET /api/v1/reports/inventory`: Generate inventory report data.
- `GET /api/v1/reports/expiry`: Generate expiry report data.
- `GET /api/v1/analytics/sales`: Get sales analytics data formatted for Chart.js.

---

## 4. System Health
- `GET /api/v1/health`: Returns a simple successful JSON response. (Public)
