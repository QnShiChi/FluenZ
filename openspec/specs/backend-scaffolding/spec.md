## ADDED Requirements

### Requirement: Spring Boot project with Gradle and Java 17
The system SHALL provide a Spring Boot project at `backend/` using Gradle (Kotlin DSL) with Java 17. The project SHALL use the base package `com.fluenz.api`. The Gradle wrapper (`gradlew`) SHALL be committed so builds are reproducible without a local Gradle installation.

#### Scenario: Successful Gradle build
- **WHEN** developer runs `./gradlew build` inside the `backend/` directory
- **THEN** the project compiles without errors and produces a runnable JAR

---

### Requirement: Clean Architecture layer structure
The backend project SHALL enforce a Clean Architecture layout with the following package structure under `com.fluenz.api`:
- `controller/` — HTTP routing (only `@RestController` classes)
- `service/` — Service interfaces
- `service/impl/` — Service implementations with business logic
- `repository/` — Spring Data JPA repository interfaces
- `dto/request/` — Request DTOs
- `dto/response/` — Response DTOs
- `entity/` — JPA entities (never exposed to the client)
- `config/` — Spring configuration classes

#### Scenario: Package structure exists
- **WHEN** the project is scaffolded
- **THEN** all listed packages exist with appropriate placeholder classes or package-info files

---

### Requirement: Health check endpoint
The backend SHALL expose a `GET /api/health` endpoint that returns HTTP 200 with a JSON body `{"status": "UP"}`. This serves as a smoke test for Docker health checks and developer verification.

#### Scenario: Health check returns UP
- **WHEN** a GET request is made to `/api/health`
- **THEN** the response status is 200 and the body is `{"status": "UP"}`

---

### Requirement: PostgreSQL datasource configuration
The backend SHALL be configured to connect to PostgreSQL using Spring's `application.yml` with the following properties sourced from environment variables:
- `SPRING_DATASOURCE_URL` (default: `jdbc:postgresql://postgres:5432/fluenz_db`)
- `SPRING_DATASOURCE_USERNAME` (default: `fluenz_user`)
- `SPRING_DATASOURCE_PASSWORD` (default: `fluenz_secret`)

JPA/Hibernate SHALL be configured with `ddl-auto: update` for initial development.

#### Scenario: Backend connects to PostgreSQL
- **WHEN** the backend starts with valid database credentials
- **THEN** it connects to PostgreSQL and the health endpoint returns 200

---

### Requirement: Multi-stage Dockerfile
The backend SHALL have a `Dockerfile` at `backend/Dockerfile` using a multi-stage build:
- Stage 1: `gradle:8-jdk17` — copies source, runs `gradle build`
- Stage 2: `eclipse-temurin:17-jre` — copies the built JAR, runs it

#### Scenario: Docker image builds successfully
- **WHEN** `docker build` is run from the backend directory
- **THEN** the image is created and the app starts on port 8080

---

### Requirement: CORS configuration
The backend SHALL configure CORS to allow requests from `http://localhost:5173` (Vite dev server) during development.

#### Scenario: Frontend can call backend API
- **WHEN** the frontend makes an API request to the backend from `localhost:5173`
- **THEN** the request is not blocked by CORS
