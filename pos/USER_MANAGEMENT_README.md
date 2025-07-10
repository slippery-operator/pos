# User Management Implementation

This document describes the implementation of user management functionality in the Spring POS application.

## Overview

The user management system provides:
- **User Registration (Signup)**: Automatic role assignment based on email patterns
- **User Authentication (Login)**: Session-based authentication with role-based access control
- **Role-Based Access Control**: Different permissions for SUPERVISOR and OPERATOR roles
- **Session Management**: 5-minute authentication check intervals

## Architecture

The implementation follows the layered architecture pattern:

```
Controller (AuthController) → DTO (AuthDto) → API (AuthApi) → DAO (UserDao) → Database
```

### Components

1. **Controller Layer** (`AuthController`)
   - Handles HTTP requests for `/auth/*` endpoints
   - Manages session creation and invalidation
   - Validates input using `@Valid` annotations

2. **DTO Layer** (`AuthDto`)
   - Contains business logic for authentication operations
   - Acts as an intermediary between Controller and API layers

3. **API Layer** (`AuthApi`)
   - Implements core authentication logic
   - Handles password encoding and role assignment
   - Manages data normalization (lowercase, trimming)

4. **DAO Layer** (`UserDao`)
   - Provides database access methods
   - Implements case-insensitive email search
   - Extends `AbstractDao` for common CRUD operations

5. **Entity Layer** (`UserPojo`, `Role`)
   - Defines user data structure
   - Includes role enumeration for access control

## Role Assignment Logic

### SUPERVISOR Role
Users with emails containing any of these keywords get SUPERVISOR role:
- `supervisor`
- `admin`
- `manager`
- `lead`

### OPERATOR Role
All other users get OPERATOR role by default.

## Access Control

### SUPERVISOR Permissions
- Full access to all endpoints
- Can access upload endpoints (`/products/upload`, `/inventory/upload`)
- Can access reports endpoints (`/reports/**`)
- Can perform all CRUD operations

### OPERATOR Permissions
- Restricted access to certain endpoints
- **Cannot access** upload endpoints
- **Cannot access** reports endpoints
- Can view and manage products, inventory, and clients
- Cannot create new products or inventory items via POST

## Session Management

### Authentication Flow
1. User submits login credentials
2. System validates credentials against database
3. If valid, creates session with:
   - `userId`: User's database ID
   - `userRole`: User's role (SUPERVISOR/OPERATOR)
   - `lastCheckedTime`: Current timestamp

### Session Validation
- Sessions are validated every 5 minutes (300,000 milliseconds)
- If validation fails, user is logged out
- Session invalidation occurs on logout or validation failure

## Data Handling

### Input Normalization
- **Emails**: Converted to lowercase and trimmed
- **Names**: Trimmed of leading/trailing whitespace
- **Passwords**: Encrypted using BCrypt before storage

### Case-Insensitive Operations
- Email searches are performed in lowercase
- Role comparisons are case-sensitive (enum values)

## API Endpoints

### POST `/auth/signup`
- **Purpose**: Register new user
- **Request Body**: `SignupForm` (name, email, password)
- **Response**: `UserResponse` (id, email, name, role)
- **Validation**: Email format, password length (min 6 chars)

### POST `/auth/login`
- **Purpose**: Authenticate user
- **Request Body**: `LoginForm` (email, password)
- **Response**: `LoginResponse` (message, user details)
- **Side Effect**: Creates session with user information

### POST `/auth/logout`
- **Purpose**: Logout user
- **Request Body**: None
- **Response**: String message
- **Side Effect**: Invalidates session

## Security Features

### Password Security
- Passwords are encrypted using BCrypt
- Minimum password length: 6 characters
- Password validation on login

### Session Security
- Session-based authentication
- Automatic session validation
- Session invalidation on logout

### Role-Based Security
- Custom authentication filter
- URL-based access control
- Method-level security annotations

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('SUPERVISOR', 'OPERATOR') NOT NULL,
    last_login DATETIME,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);
```

## Testing

The implementation includes comprehensive tests in `AuthServiceTest.java`:
- Role assignment based on email patterns
- Data normalization (lowercase, trimming)
- Input validation
- Error handling

## Configuration

### Spring Security Configuration
- CSRF disabled for API endpoints
- Session management enabled
- Custom authentication filter
- Role-based URL access control

### Password Encoder
- BCrypt password encoder configured
- Default strength: 10 rounds

## Error Handling

### Common Error Scenarios
1. **Invalid Credentials**: Generic message for security
2. **Email Already Exists**: During signup
3. **Invalid Session**: Session validation failure
4. **Insufficient Permissions**: Role-based access denied

### Error Responses
- HTTP 401: Unauthorized (invalid credentials/session)
- HTTP 403: Forbidden (insufficient permissions)
- HTTP 400: Bad Request (validation errors)

## Usage Examples

### Creating a Supervisor User
```bash
curl -X POST http://localhost:9000/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Supervisor",
    "email": "supervisor@company.com",
    "password": "password123"
  }'
```

### Creating an Operator User
```bash
curl -X POST http://localhost:9000/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Operator",
    "email": "operator@company.com",
    "password": "password123"
  }'
```

### User Login
```bash
curl -X POST http://localhost:9000/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "supervisor@company.com",
    "password": "password123"
  }'
```
