AUTH SERVICE DOCUMENTATION

Overview
--------
This is a simple authentication service built with Spring Boot.
It provides basic user management (CRUD), validation, and database persistence.

Features
--------
- User registration
- Get user by ID
- Get all users
- Update user (PATCH)
- Delete user
- Input validation (email, password, age)
- Global error handling
- PostgreSQL database
- Docker support
- Unit tests (Mockito)
- Integration tests (Testcontainers)

----------------------------------------

SETUP

1. Start database (Docker)

Run:
docker compose up -d

PostgreSQL will run on:
localhost:5434

----------------------------------------

2. Run application

From terminal:
./gradlew bootRun

Application runs on:
http://localhost:8080

----------------------------------------

API ENDPOINTS

1. REGISTER USER

POST /users/register

Example request:
{
  "email": "test@test.com",
  "password": "password1",
  "username": "user1",
  "first_name": "Test",
  "last_name": "User",
  "date_of_birth": "2000-01-01"
}

Expected:
- 201 Created
- JWT cookie is returned
- User data (without password)

----------------------------------------

2. GET ALL USERS

GET /users

Example:
GET http://localhost:8080/users

----------------------------------------

3. GET USER BY ID

GET /users/{id}

Example:
GET http://localhost:8080/users/PUT_ID_HERE

----------------------------------------

4. UPDATE USER

PATCH /users/{id}

Example request:
{
  "username": "newUsername"
}

Note:
If authorization is enabled, user can only update their own account.

----------------------------------------

5. DELETE USER

DELETE /users/{id}

Example:
DELETE http://localhost:8080/users/PUT_ID_HERE

----------------------------------------

ERROR HANDLING

The application returns structured error responses.

Example (validation error):
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "The request payload failed validation.",
    "details": {
      "email": "Invalid email"
    }
  }
}

----------------------------------------

TESTING

Run all tests:
./gradlew test

Unit Tests:
- Service layer
- Uses Mockito

Integration Tests:
- Repository layer
- Uses Testcontainers with real PostgreSQL

----------------------------------------

NOTES

- Passwords are encrypted using BCrypt
- Password is never returned in API responses
- Duplicate email or username returns 409 Conflict
- Minimum age is configurable (default: 18)

----------------------------------------

FUTURE IMPROVEMENTS

- Login endpoint
- Enforced JWT authentication
