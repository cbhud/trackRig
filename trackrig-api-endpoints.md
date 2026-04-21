# TrackRig API Endpoints

## Base URL
```http
http://localhost:8080
```

---

## Authentication

### Register
Creates a new user account.

```http
POST /auth/register
```

**Auth required:** No

### Request body
```json
{
  "username": "john123",
  "email": "john@example.com",
  "password": "Password123!",
  "fullName": "John Doe"
}
```

### Response
**Status:** `201 Created`

```json
{
  "username": "john123",
  "email": "john@example.com",
  "fullName": "John Doe"
}
```

---

### Login
Authenticates a user and returns a JWT token.

```http
POST /auth/login
```

**Auth required:** No

### Request body
```json
{
  "username": "john123",
  "password": "Password123!"
}
```

### Response
**Status:** `200 OK`

```json
{
  "token": "jwt-token-here",
  "username": "john123",
  "role": "EMPLOYEE"
}
```

---

## Current User

### Get current user
Returns the currently authenticated user.

```http
GET /users/me
```

**Auth required:** Yes  
**Bearer token required:** Yes

### Response
**Status:** `200 OK`

```json
{
  "username": "john123",
  "email": "john@example.com",
  "fullName": "John Doe",
  "role": "EMPLOYEE"
}
```

---

### Update current user profile
Updates the authenticated user's profile fields.

```http
PATCH /users/me
```

**Auth required:** Yes  
**Bearer token required:** Yes

### Request body
```json
{
  "username": "johnny123",
  "email": "johnny@example.com",
  "fullName": "Johnny Doe"
}
```

### Response
**Status:** `200 OK`

```json
{
  "user": {
    "username": "johnny123",
    "email": "johnny@example.com",
    "fullName": "Johnny Doe",
    "role": "EMPLOYEE"
  },
  "token": "new-jwt-token-if-username-was-changed"
}
```

> If the username changes, a new JWT token should be used from this response.

---

### Change current user password
Changes the authenticated user's password.

```http
PATCH /users/me/password
```

**Auth required:** Yes  
**Bearer token required:** Yes

### Request body
```json
{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword123!"
}
```

### Response
**Status:** `204 No Content`

---

### Delete current user account
Deletes the authenticated user's own account.

```http
DELETE /users/me
```

**Auth required:** Yes  
**Bearer token required:** Yes

### Response
**Status:** `204 No Content`

---

## Admin / Management Endpoints

> These endpoints should only be accessible to privileged roles such as `MANAGER` or `OWNER`, depending on your security configuration.

---

### Get all users
Returns a list of all users for admin/management purposes.

```http
GET /users
```

**Auth required:** Yes  
**Bearer token required:** Yes  
**Role required:** `MANAGER` or `OWNER`

### Response
**Status:** `200 OK`

```json
[
  {
    "id": 1,
    "username": "john123",
    "email": "john@example.com",
    "fullName": "John Doe",
    "role": "EMPLOYEE",
    "createdAt": "2026-04-21T12:00:00Z"
  },
  {
    "id": 2,
    "username": "manager1",
    "email": "manager@example.com",
    "fullName": "Manager User",
    "role": "MANAGER",
    "createdAt": "2026-04-20T10:30:00Z"
  }
]
```

---

### Get user by ID
Returns one specific user by ID.

```http
GET /users/{id}
```

**Auth required:** Yes  
**Bearer token required:** Yes  
**Role required:** `MANAGER` or `OWNER`

### Response
**Status:** `200 OK`

```json
{
  "id": 1,
  "username": "john123",
  "email": "john@example.com",
  "fullName": "John Doe",
  "role": "EMPLOYEE",
  "createdAt": "2026-04-21T12:00:00Z"
}
```

---

### Update user by ID
Allows admin/manager to update another user's account details.

```http
PATCH /users/{id}
```

**Auth required:** Yes  
**Bearer token required:** Yes  
**Role required:** `MANAGER` or `OWNER`

### Request body
```json
{
  "username": "john_updated",
  "email": "john_updated@example.com",
  "fullName": "John Updated",
  "role": "MANAGER"
}
```

### Response
**Status:** `200 OK`

```json
{
  "id": 1,
  "username": "john_updated",
  "email": "john_updated@example.com",
  "fullName": "John Updated",
  "role": "MANAGER",
  "createdAt": "2026-04-21T12:00:00Z"
}
```

---

### Delete user by ID
Allows admin/manager to delete another user.

```http
DELETE /users/{id}
```

**Auth required:** Yes  
**Bearer token required:** Yes  
**Role required:** `MANAGER` or `OWNER`

### Response
**Status:** `204 No Content`

---

## Authorization Header

For protected endpoints, include:

```http
Authorization: Bearer <your-jwt-token>
```

---

## Common Error Responses

### Validation error
**Status:** `400 Bad Request`

```json
{
  "status": "BAD_REQUEST",
  "message": "Validation failed",
  "timestamp": "2026-04-21T14:00:00",
  "fieldErrors": {
    "email": "must be a well-formed email address"
  }
}
```

---

### Invalid credentials
**Status:** `401 Unauthorized`

```json
{
  "status": "UNAUTHORIZED",
  "message": "Invalid username or password",
  "timestamp": "2026-04-21T14:00:00"
}
```

---

### Forbidden
**Status:** `403 Forbidden`

```json
{
  "status": "FORBIDDEN",
  "message": "You do not have permission to access this resource",
  "timestamp": "2026-04-21T14:00:00"
}
```

---

### User already exists
**Status:** `409 Conflict`

```json
{
  "status": "CONFLICT",
  "message": "Account with that username already exists!",
  "timestamp": "2026-04-21T14:00:00"
}
```

---

### Method not allowed
**Status:** `405 Method Not Allowed`

```json
{
  "status": "METHOD_NOT_ALLOWED",
  "message": "Request method is not supported for this endpoint",
  "timestamp": "2026-04-21T14:00:00"
}
```
