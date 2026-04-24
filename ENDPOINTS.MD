# TrackRig API Documentation

This document provides a comprehensive list of all available API endpoints in the TrackRig application, including the latest component-related features.

## Base URL
`http://localhost:8080`

## Authentication
All endpoints except `/auth/*` require a Bearer Token in the `Authorization` header.

| Method | Path | Description |
| :--- | :--- | :--- |
| POST | `/auth/register` | Register a new user account |
| POST | `/auth/login` | Login and receive a JWT token |

---

## User Management (`/users`)

| Method | Path | Description | Access |
| :--- | :--- | :--- | :--- |
| GET | `/users/me` | Get current user profile | Authenticated |
| PATCH | `/users/me` | Update current user profile | Authenticated |
| PATCH | `/users/me/password` | Change current user password | Authenticated |
| DELETE | `/users/me` | Delete current user account | Authenticated |
| GET | `/users` | List all users | OWNER only |
| GET | `/users/{id}` | Get user by ID | OWNER only |
| PATCH | `/users/{id}` | Update user by ID | OWNER only |
| DELETE | `/users/{id}` | Delete user by ID | OWNER only |

---

## Workstation Management (`/workstations`)

| Method | Path | Description |
| :--- | :--- | :--- |
| GET | `/workstations` | List all workstations |
| GET | `/workstations/{id}` | Get workstation by ID |
| POST | `/workstations` | Create a new workstation |
| PATCH | `/workstations/{id}` | Update workstation details (grid, floor) |
| DELETE | `/workstations/{id}` | Delete a workstation |
| PATCH | `/workstations/{id}/status` | Update workstation status |

### Workstation Statuses
| Method | Path | Description |
| :--- | :--- | :--- |
| GET | `/workstations/status` | List all workstation statuses |
| GET | `/workstations/status/{id}` | Get workstation status by ID |
| POST | `/workstations/status` | Create a new workstation status |
| PATCH | `/workstations/status/{id}` | Update status details |
| DELETE | `/workstations/status/{id}` | Delete a status |

---

## Component Management (`/components`)

| Method | Path | Description |
| :--- | :--- | :--- |
| GET | `/components` | List all components |
| GET | `/components/{id}` | Get component by ID |
| POST | `/components` | Create a new component |
| PATCH | `/components/{id}` | Update component details |
| DELETE | `/components/{id}` | Delete a component |
| PATCH | `/components/{id}/workstation` | Assign/Unassign workstation |
| PATCH | `/components/{id}/category` | Assign a category to component |
| PATCH | `/components/{id}/status` | Assign a status to component |

### Component Categories
| Method | Path | Description |
| :--- | :--- | :--- |
| GET | `/components/categories` | List all component categories |
| GET | `/components/categories/{id}` | Get category by ID |
| POST | `/components/categories` | Create a new category |
| PATCH | `/components/categories/{id}` | Update category details |
| DELETE | `/components/categories/{id}` | Delete a category |

### Component Statuses
| Method | Path | Description |
| :--- | :--- | :--- |
| GET | `/components/status` | List all component statuses |
| GET | `/components/status/{id}` | Get status by ID |
| POST | `/components/status` | Create a new status |
| PATCH | `/components/status/{id}` | Update status details |
| DELETE | `/components/status/{id}` | Delete a status |

---

## Component Assignment History (`/component-assignments`)

| Method | Path | Description |
| :--- | :--- | :--- |
| GET | `/component-assignments` | All assignment logs |
| GET | `/component-assignments/component/{id}` | Logs by component |
| GET | `/component-assignments/workstation/{id}` | Logs by workstation |
| GET | `/component-assignments/component/{id}/active` | Current active assignment |
| POST | `/component-assignments` | Create assignment (auto-closes previous) |
| PATCH | `/component-assignments/component/{id}/close` | Close active assignment |

---

## Maintenance Management (`/maintenance`)

### Maintenance Types
| Method | Path | Description |
| :--- | :--- | :--- |
| GET | `/maintenance/types` | List all types |
| GET | `/maintenance/types/active` | List active types |
| POST | `/maintenance/types` | Create a new type |
| PATCH | `/maintenance/types/{id}` | Update type |
| DELETE | `/maintenance/types/{id}` | Delete type |

### Maintenance Logs
| Method | Path | Description |
| :--- | :--- | :--- |
| GET | `/maintenance/logs` | List all logs |
| POST | `/maintenance/logs` | Create a log |
| GET | `/maintenance/logs/{id}` | Get log by ID |
| DELETE | `/maintenance/logs/{id}` | Delete log |
| GET | `/maintenance/logs/workstation/{id}` | Logs by workstation |
| GET | `/maintenance/logs/type/{id}` | Logs by type |

### Maintenance Status
| Method | Path | Description |
| :--- | :--- | :--- |
| GET | `/maintenance/status` | All workstation×type statuses |
| GET | `/maintenance/status/workstation/{id}` | Status by workstation |
| GET | `/maintenance/status/overdue` | Overdue maintenance |

---

## Sample Request Payloads

### Create Component
```json
{
  "serialNumber": "SN-SPRING-001",
  "name": "Intel Core i7",
  "notes": "Test component",
  "componentCategory": { "id": 1 },
  "componentStatus": { "id": 1 },
  "workstation": { "name": "Workstation A" }
}
```

### Create Component Assignment
```json
{
  "componentId": 1,
  "workstationId": 5,
  "notes": "Moving to development rig"
}
```

### Create Maintenance Type
```json
{
  "name": "Monthly Cleaning",
  "description": "Clean filters and fans",
  "intervalDays": 30,
  "isActive": true
}
```

### Update Workstation Details
```json
{
  "gridX": 5,
  "gridY": 10,
  "floor": 2
}
```
