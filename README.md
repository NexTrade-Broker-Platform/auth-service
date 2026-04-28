# AUTH SERVICE

## Overview

This is a Spring Boot-based authentication service that provides basic user management and JWT-based authentication.

The service supports full CRUD operations, input validation, secure password handling, and database persistence using PostgreSQL.

---

## Features

* User registration
* User login (JWT-based authentication)
* Get user by ID (secured)
* Update user (secured, ownership enforced)
* Delete user (secured, ownership enforced)
* Input validation (email, password, age)
* Global error handling (standardized responses)
* Password hashing (BCrypt)
* JWT authentication using HTTP-only cookies
* PostgreSQL database (Dockerized)
* Unit tests (Mockito, JUnit 5)
* Integration tests (Testcontainers)

---

## Tech Stack

* Java 17+
* Spring Boot
* Spring Security
* JPA / Hibernate
* PostgreSQL
* Docker
* JUnit 5 + Mockito
* Testcontainers

---

## Setup

### 1. Start Database (Docker)

Run:

```
docker compose up -d
```

Database will be available at:

```
localhost:5434
```

---

### 2. Run Application

```
./gradlew bootRun
```

Application runs at:

```
http://localhost:8080
```

---

## Authentication

The application uses JWT stored in **HTTP-only cookies**.

Flow:

1. Register or login
2. Server returns JWT cookie
3. Browser automatically sends cookie with each request
4. JWT filter authenticates the user

---

## API Endpoints

### 1. Register User

**POST /users/register**

Request:

```
{
  "email": "test@test.com",
  "password": "password1",
  "username": "user1",
  "first_name": "Test",
  "last_name": "User",
  "date_of_birth": "2000-01-01"
}
```

Response:

* 201 Created
* JWT cookie set
* User data returned

---

### 2. Login

**POST /users/login**

Request:

```
{
  "email": "test@test.com",
  "password": "password1"
}
```

Response:

* 200 OK
* JWT cookie set
* User data returned

---

### 3. Get User by ID (Protected)

**GET /users/{id}**

Requires authentication.

User can only access their own data.

---

### 4. Update User (Protected)

**PATCH /users/{id}**

Example:

```
{
  "username": "newUsername"
}
```

Rules:

* Requires authentication
* User can only update their own account

---

### 5. Delete User (Protected)

**DELETE /users/{id}**

Rules:

* Requires authentication
* User can only delete their own account

---

## Error Handling

All errors return a consistent structure:

```
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Description",
    "details": {}
  }
}
```

### Common Errors

| Status | Code                | Description                  |
| ------ | ------------------- | ---------------------------- |
| 400    | VALIDATION_ERROR    | Invalid input                |
| 401    | INVALID_CREDENTIALS | Wrong login                  |
| 403    | FORBIDDEN           | Unauthorized access          |
| 404    | USER_NOT_FOUND      | User does not exist          |
| 409    | RESOURCE_EXISTS     | Email/username already taken |

---

## Testing

Run all tests:

```
./gradlew test
```

### Unit Tests

* Service layer
* Uses Mockito
* Database is mocked

### Integration Tests

* Repository layer
* Uses Testcontainers
* Real PostgreSQL instance

---

## Database

PostgreSQL runs in Docker.

Default config:

* DB: auth_db
* User: user
* Password: password
* Port: 5434

---

## Security Notes

* Passwords are hashed using BCrypt
* Password is never returned in responses
* JWT is stored in HTTP-only cookie (not accessible via JS)
* Token expires after 1 hour
* Stateless authentication (no sessions)

---

