# Account Service – Secure User Management REST API

## Overview

**Account Service** is a secure backend REST API built with **Java** and **Spring Boot** that implements a complete user authentication and authorization system.  
The goal of the application is to demonstrate how to design and implement a robust authentication and security layer for a modern backend service.

---

## Business Problem

Modern web services that handle user data must provide strict security guarantees and comply with auditing and compliance requirements. This project addresses several key challenges:

- Ensuring only authorized users can access specific resources.
- Tracking and auditing all security-related actions (registration, login attempts, role changes, etc.).
- Preventing brute-force attacks and automatically locking compromised accounts.
- Providing a mechanism for administrators to manage user roles and access.
- Delivering all sensitive data over a secure HTTPS channel.

This project solves these problems by implementing a layered security model based on **Spring Security**, event logging, brute-force detection, and SSL/TLS encryption.

---

## Features

### 1. User Management
- User registration with validation and initial role assignment.
- Password change endpoint with checks against:
  - Breached password lists
  - Password duplication
- User deletion by administrators.

### 2. Authentication & Authorization
- Stateless REST API with HTTP Basic Authentication.
- Role-based access control (**RBAC**) using Spring Security.
- Custom access policies for different roles.
- Handling of unauthorized access and failed authentication attempts.

### 3. Security Event Logging
All important security-related events are logged and stored in the database. Examples include:
- `CREATE_USER` – successful registration
- `CHANGE_PASSWORD` – password change
- `LOGIN_FAILED` – failed authentication
- `ACCESS_DENIED` – unauthorized access
- `GRANT_ROLE` / `REMOVE_ROLE` – role changes
- `DELETE_USER` – user deleted
- `BRUTE_FORCE` – multiple failed login attempts detected
- `LOCK_USER` / `UNLOCK_USER` – account locked or unlocked

Each event includes:
- Timestamp  
- Event type  
- Subject (user performing the action)  
- Object (target of the action)  
- API path  

### 4. Brute-Force Protection
- Tracks failed login attempts per user.
- Locks account after 5 failed attempts.
- Admin accounts are exempt from automatic locking.
- Manual lock/unlock endpoints for administrators.

### 5. Audit and Compliance
- All security events are persisted.
- Exposed via a secure endpoint `/api/security/events` (admin-only).
- Provides chronological logs for auditing and investigation.

### 6. HTTPS / SSL
- All communication is secured with HTTPS using a self-signed certificate.
- Certificate generated with `keytool` and configured in `application.properties`.

---

## Technologies Used

- **Java 17**
- **Spring Boot 3.x**
- **Spring Web** – REST API layer
- **Spring Security** – authentication and authorization
- **Spring Data JPA / Hibernate** – persistence layer
- **PostgreSQL** – database
- **Lombok** – reduce boilerplate code
- **ApplicationListener** – custom security event handling
- **SSL/TLS (PKCS12 keystore)** – encrypted communication
- **Flyway** – database migrations (optional)
- **Docker Compose** – for local database setup (optional)

---

## Project Architecture
src/main/java/account
├─ config/ # Security configuration
├─ controller/ # REST controllers
├─ domain/ # JPA entities
├─ dto/ # DTOs
├─ repository/ # Data repositories
├─ security/ # Security layer (handlers, listeners)
│ ├─ event/ # SecurityEvent logging
| ├─ listener/ # Brute-force detection listener
│ └─ web/ # Security controllers
├─ service/ # Business logic
└─ utils/ # Helper classes

## API Endpoints

### Authentication
- `POST /api/auth/signup` – register new user  
- `POST /api/auth/changepass` – change password  

### Administration
- `GET /api/admin/user/` – list users  
- `PUT /api/admin/user/role` – grant or revoke a role  
- `DELETE /api/admin/user/{email}` – delete user  
- `PUT /api/admin/user/access` – lock or unlock user  

### Security Audit
- `GET /api/security/events/` – list all security events (admin only)

---

## Running the Project

### 1. Generate Keystore
```bash
keytool -genkeypair \
  -alias accountant_service \
  -keyalg RSA -keysize 2048 \
  -storetype PKCS12 \
  -keystore src/main/resources/keystore/service.p12 \
  -storepass service -keypass service \
  -validity 3650 \
  -dname "CN=accountant_service, OU=Dev, O=ACME, L=City, S=State, C=US"
```

### 2. Enable HTTPS
src/main/resources/application.properties:
```bash
server.ssl.enabled=true
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:keystore/service.p12
server.ssl.key-store-password=service
server.ssl.key-alias=accountant_service
server.port=8443
```

### 3. Run the Application
The service will be available at:
```bash
[./mvnw spring-boot:run](https://localhost:8443)
```

### Key Takeaways

This project demonstrates how to build a production-grade authentication and security layer with Spring Boot, including:
- Role-based access control
- Comprehensive security event logging
- Brute-force detection and account locking
- Secure password management
- HTTPS communication
The final result is a complete, auditable, and secure backend suitable as the foundation for real-world enterprise services.
