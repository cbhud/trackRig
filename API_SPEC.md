# TrackRig API Specification

Detailed technical documentation for all API endpoints in the TrackRig system.

## 🔒 Authentication
Base path: `/auth`

### 1. Register User
- **Method:** `POST`
- **Path:** `/auth/register`
- **Description:** Creates a new user account. Defaults to `EMPLOYEE` role.
- **Request Body:**
  | Field | Type | Required | Constraints | Description |
  | :--- | :--- | :--- | :--- | :--- |
  | `username` | String | Yes | 3-16 chars | Unique username |
  | `email` | String | Yes | Email format | Valid email address |
  | `password` | String | Yes | 8-32 chars, Pattern | Must contain Upper, Lower, Number, Special char |
  | `fullName` | String | Yes | - | User's real name |
- **Response:** `RegisterResponse` (username, email, fullName)

### 2. Login
- **Method:** `POST`
- **Path:** `/auth/login`
- **Description:** Authenticates user and returns a JWT.
- **Request Body:**
  | Field | Type | Required | Constraints | Description |
  | :--- | :--- | :--- | :--- | :--- |
  | `username` | String | Yes | 3-16 chars | |
  | `password` | String | Yes | 8-32 chars | |
- **Response:** `AuthResponse` (token, username, role)

---

## 👤 User Management
Base path: `/users`
*Requires Authentication*

### 1. Get Current Profile
- **Method:** `GET`
- **Path:** `/users/me`
- **Response:** `UserResponse` (username, email, fullName, role)

### 2. Update My Profile
- **Method:** `/PATCH`
- **Path:** `/users/me`
- **Request Body:** (All fields optional)
  - `username` (3-16 chars)
  - `email` (Email format)
  - `fullName` (1-100 chars)

### 3. Change Password
- **Method:** `PATCH`
- **Path:** `/users/me/password`
- **Request Body:**
  - `currentPassword` (Required)
  - `newPassword` (Required, 8-32 chars, Pattern)

### 4. Admin: List All Users
- **Method:** `GET`
- **Path:** `/users`
- **Access:** `OWNER` only.

---

## 🖥️ Workstation Management
Base path: `/workstations`

### 1. Create Workstation
- **Method:** `POST`
- **Path:** `/workstations`
- **Request Body:**
  - `name` (Required, 3-32 chars)

### 2. Update Workstation Details
- **Method:** `PATCH`
- **Path:** `/workstations/{id}`
- **Request Body:** (All optional)
  - `name` (3-32 chars)
  - `gridX` (Integer)
  - `gridY` (Integer)
  - `floor` (Integer)

### 3. Update Status
- **Method:** `PATCH`
- **Path:** `/workstations/{id}/status`
- **Request Body:**
  - `statusId` (Required, Integer)

### 4. Workstation Status CRUD
- `GET /workstations/status`
- `POST /workstations/status`: `{ "name": "...", "color": "..." }`
- `PATCH /workstations/status/{id}`: `{ "name": "...", "color": "..." }`

---

## 🛠️ Component Management
Base path: `/components`

### 1. Create Component
- **Method:** `POST`
- **Path:** `/components`
- **Request Body:**
  | Field | Type | Required | Description |
  | :--- | :--- | :--- | :--- |
  | `serialNumber` | String | No | Unique serial, max 100 chars |
  | `name` | String | Yes | Component name |
  | `notes` | String | No | Max 255 chars |
  | `componentCategory` | Object | Yes | `{ "id": Integer }` |
  | `componentStatus` | Object | Yes | `{ "id": Integer }` |
  | `workstation` | Object | No | `{ "name": String }` (Lookup by name) |

### 2. Update Component
- **Method:** `PATCH`
- **Path:** `/components/{id}`
- **Request Body:** `serialNumber`, `name`, `notes` (All optional)

### 3. Specific Assignments
- `PATCH /components/{id}/workstation`: `{ "workstationId": Integer }` (null to unassign)
- `PATCH /components/{id}/category`: `{ "categoryId": Integer }`
- `PATCH /components/{id}/status`: `{ "statusId": Integer }`

---

## 📜 Assignment History
Base path: `/component-assignments`

### 1. Create Assignment
- **Method:** `POST`
- **Path:** `/component-assignments`
- **Description:** Moves a component to a workstation. Auto-closes the previous assignment.
- **Request Body:**
  - `componentId` (Required)
  - `workstationId` (Optional, null for storage)
  - `notes` (Optional)

### 2. Close Assignment
- **Method:** `PATCH`
- **Path:** `/component-assignments/component/{id}/close`
- **Query Param:** `notes` (Optional)

---

## 🧹 Maintenance
Base path: `/maintenance`

### 1. Maintenance Types
- `GET /maintenance/types`: List all
- `POST /maintenance/types`: `{ "name": "...", "intervalDays": Integer, "description": "...", "isActive": Boolean }`

### 2. Maintenance Logs
- **Method:** `POST`
- **Path:** `/maintenance/logs`
- **Request Body:**
  - `workstationId` (Required)
  - `maintenanceTypeId` (Required)
  - `notes` (Optional)

### 3. Maintenance Status
- `GET /maintenance/status`: Full matrix of workstation vs task status.
- `GET /maintenance/status/overdue`: Only tasks that have passed their next due date.
