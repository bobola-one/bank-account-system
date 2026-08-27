# Bank Account Management System

A full-stack banking application built with Spring Boot, PostgreSQL, and a Java Swing desktop client. Supports creating accounts, depositing and withdrawing funds, and viewing transaction history — with full CRUD operations exposed through a REST API and consumed by a standalone GUI.

## Overview

This project was built as a learning exercise to practice core backend and desktop application development skills: Java OOP, relational database design, REST API architecture, input validation, and centralized error handling. It started as a Spring Boot + PostgreSQL backend and grew to include a Swing GUI client communicating with that backend over HTTP.

## Tech Stack

- **Language:** Java 21
- **Backend Framework:** Spring Boot 4.1
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA (Hibernate)
- **Desktop GUI:** Java Swing
- **HTTP Client (GUI to API):** Java's built-in `java.net.http.HttpClient`
- **JSON Serialization:** Jackson
- **Testing:** JUnit 5, Mockito
- **Build Tool:** Maven
- **Version Control:** Git

## Architecture

The application follows a layered architecture with a clear separation of concerns:

Entity -> Repository -> Service -> Controller -> (REST API) -> Swing GUI (via ApiClient)

- **Entity layer** — `Account`, `Transaction`, `TransactionType` define the data model and their relationships (one-to-many between `Account` and `Transaction`).
- **Repository layer** — Spring Data JPA interfaces providing database access with no manual SQL.
- **Service layer** — All business logic: account creation, deposit/withdraw validation, balance updates, and transaction recording, wrapped in database transactions to guarantee consistency.
- **Controller layer** — REST endpoints exposing the service layer over HTTP, using DTOs to decouple the API contract from the database schema.
- **Exception handling** — Custom exceptions (`AccountNotFoundException`, `InsufficientFundsException`, `InvalidAmountException`) mapped to correct HTTP status codes via a centralized `@RestControllerAdvice` handler.
- **GUI layer** — A Swing desktop client that communicates exclusively through a REST API client (`ApiClient`), never touching the database or service layer directly.

## Features

- Create new bank accounts with an account holder name and initial balance
- Deposit and withdraw funds, with balance validation and insufficient-funds checking
- View a complete, timestamped transaction history per account
- Full CRUD accessible both via REST API (testable with Postman) and a Swing desktop GUI
- Centralized error handling returning structured JSON error responses (404, 400, 409) instead of generic server errors
- Unit tests covering core business logic and validation rules using mocked dependencies

## Skills Demonstrated

- **Java OOP** — encapsulation, immutability (transaction records are never modified after creation), functional interfaces, and clean separation of responsibilities across layers
- **Database relationships** — a one-to-many relationship between `Account` and `Transaction`, modeled with JPA and cascading correctly at the database level
- **Input validation and error handling** — validation enforced at the database (constraints), service (business rules), and GUI (user-facing dialogs) layers, with custom exceptions carrying meaningful, specific error messages end-to-end
- **REST API design** — proper HTTP status codes, DTOs to protect the API contract, and RESTful conventions for non-CRUD actions (e.g. `POST /accounts/{id}/deposit`)
- **Unit testing** — isolated service-layer tests using Mockito to mock the repository layer, verifying both successful operations and every validation failure path

## Project Structure

Backend source: `src/main/java/com/bobola/bank_account_system/`
- `entity/` — JPA entities (Account, Transaction, TransactionType)
- `repository/` — Spring Data JPA repositories
- `service/` — Business logic and validation
- `controller/` — REST controllers
- `dto/` — Request/response data transfer objects
- `exception/` — Custom exceptions and global exception handler
- `gui/` — Swing desktop client (MainFrame, ApiClient, dialogs)

Test source: `src/test/java/com/bobola/bank_account_system/`
- `service/` — Unit tests for AccountService

## Setup Instructions

### Prerequisites

- Java 21 (JDK)
- PostgreSQL installed and running locally
- Maven (or use the included `mvnw` wrapper)

### 1. Create the database

In `psql` or pgAdmin:

```sql
CREATE DATABASE bank_db;
```

### 2. Configure database credentials

In `src/main/resources/application.properties`, set your local PostgreSQL username and password:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bank_db
spring.datasource.username=postgres
spring.datasource.password=your_password_here
```

Tables are created automatically on first run (`spring.jpa.hibernate.ddl-auto=update`) — no manual schema setup needed.

### 3. Run the backend

Run `BankAccountSystemApplication.java` as a Java Application. The REST API will be available at `http://localhost:8080/api/accounts`.

### 4. Run the GUI

**The backend must already be running before starting the GUI** — the GUI has no direct database access; every action goes through the REST API over HTTP. Run `GuiLauncher.java` as a separate Java Application (a second, independent run, not through the same process as the backend).

## Running Tests

```bash
mvn test
```

Unit tests cover `AccountService`'s core logic — valid operations, invalid input, insufficient funds, and account-not-found scenarios — using Mockito to mock the repository layer so no real database connection is required to run them.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/accounts` | Create a new account |
| GET | `/api/accounts` | List all accounts |
| GET | `/api/accounts/{id}` | Get a single account by id |
| POST | `/api/accounts/{id}/deposit` | Deposit into an account |
| POST | `/api/accounts/{id}/withdraw` | Withdraw from an account |

## Known Quirks / Notes

- The backend and GUI run as two **separate processes** — starting the GUI without the backend already running will result in connection errors on every action.
- Port `8080` must be free before starting the backend; if a previous run is still active, the new instance will fail to start with a "port already in use" error.
- `spring.jpa.hibernate.ddl-auto=update` is used for convenience during development. In a production system, schema changes would instead be managed with a migration tool like Flyway rather than relying on Hibernate's auto-schema-sync.
- You may see a console warning from Mockito about "self-attaching to enable the inline-mock-maker" when running tests — this is a known compatibility notice related to newer JDK versions and does not affect test results.

## Future Improvements

- Update account details, close/delete accounts
- Pagination for accounts with large transaction histories
- Authentication/authorization for multi-user support
- Migrate schema management to Flyway