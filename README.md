# FluenZ — AI-Powered Personalized Communication Coach

A web-based language learning platform that dynamically tailors learning paths based on your profession and goals, using the Chunking method and LLM-powered roleplay.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 18, Vite, TypeScript, Tailwind CSS, Shadcn UI, Zustand |
| Backend | Java 17, Spring Boot 3, Gradle, Spring Data JPA |
| Database | PostgreSQL 16 |
| DevOps | Docker, Docker Compose |

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) & [Docker Compose](https://docs.docker.com/compose/install/)
- (Optional) [DBeaver](https://dbeaver.io/) for database management

## Quick Start

```bash
# 1. Clone the repo
git clone <repo-url> && cd FluenZ

# 2. Create your environment file
cp .env.example .env

# 3. Build and start all services
make build
make up

# 4. Access the applications
#    Frontend:  http://localhost:5173
#    Backend:   http://localhost:8080
#    Health:    http://localhost:8080/api/health
```

## Makefile Commands

| Command | Description |
|---------|-------------|
| `make build` | Build all Docker images |
| `make up` | Start all containers (detached) |
| `make down` | Stop and remove containers |
| `make logs` | Tail logs for all services |
| `make clean` | Remove containers, networks, and DB volumes |
| `make backup-db` | Create a timestamped PostgreSQL backup in `./backups/` |
| `make restore-db` | Restore database from a backup file |

## DBeaver Connection

To connect DBeaver (or any PostgreSQL client) to the local database:

| Setting | Value |
|---------|-------|
| Host | `localhost` |
| Port | `5432` |
| Database | `fluenz_db` |
| Username | `fluenz_user` |
| Password | `fluenz_secret` |

> **Note:** If port 5432 is already in use on your machine, change `POSTGRES_HOST_PORT` in your `.env` file.

## Project Structure

```
FluenZ/
├── backend/             # Spring Boot API (Java 17, Gradle)
│   ├── src/main/java/com/fluenz/api/
│   │   ├── controller/  # REST controllers
│   │   ├── service/     # Service interfaces
│   │   │   └── impl/    # Service implementations
│   │   ├── repository/  # Spring Data JPA repos
│   │   ├── dto/
│   │   │   ├── request/ # Request DTOs
│   │   │   └── response/# Response DTOs
│   │   ├── entity/      # JPA entities
│   │   └── config/      # Spring configuration
│   └── Dockerfile
├── frontend/            # React app (Vite + TypeScript)
│   ├── src/
│   │   ├── components/  # Reusable UI components
│   │   ├── hooks/       # Custom React hooks
│   │   ├── pages/       # Page-level components
│   │   ├── services/    # API service modules
│   │   ├── stores/      # Zustand state stores
│   │   └── lib/         # Utility functions
│   └── Dockerfile
├── backups/             # Database backup files
├── docker-compose.yml
├── Makefile
└── .env.example
```
