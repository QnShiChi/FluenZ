## ADDED Requirements

### Requirement: Docker Compose orchestrates the full stack
The system SHALL provide a `docker-compose.yml` at the project root that defines three services: `frontend`, `backend`, and `postgres`. All services SHALL be connected via a shared Docker network. The `postgres` service SHALL use the official `postgres:16` image.

#### Scenario: Starting the full stack
- **WHEN** operator runs `docker compose up`
- **THEN** all three services (frontend, backend, postgres) start and are accessible

#### Scenario: Service networking
- **WHEN** the backend service starts
- **THEN** it can connect to the postgres service using the hostname `postgres` on port `5432`

---

### Requirement: PostgreSQL is accessible from the host
The system SHALL map PostgreSQL container port `5432` to host port `5432` so external tools (e.g., DBeaver) can connect. The database SHALL be configured with database name `fluenz_db`, user `fluenz_user`, and password `fluenz_secret`.

#### Scenario: DBeaver connection
- **WHEN** a developer configures DBeaver with host `localhost`, port `5432`, database `fluenz_db`, user `fluenz_user`, password `fluenz_secret`
- **THEN** DBeaver successfully connects and can browse the database

#### Scenario: Data persistence across restarts
- **WHEN** data is written to the database and containers are stopped with `docker compose down`
- **THEN** data is preserved when containers are restarted (via a named Docker volume)

---

### Requirement: Environment variables managed via .env file
The system SHALL use a `.env` file at the project root for all configurable values (DB credentials, ports). A `.env.example` file SHALL be committed to the repository. The `.env` file SHALL be listed in `.gitignore`.

#### Scenario: Fresh clone setup
- **WHEN** a developer clones the repository and copies `.env.example` to `.env`
- **THEN** `docker compose up` succeeds with the default configuration

---

### Requirement: Makefile provides management commands
The system SHALL provide a `Makefile` at the project root with the following targets:
- `make build` — builds all Docker images
- `make up` — starts all containers in detached mode
- `make down` — stops and removes containers
- `make logs` — tails logs for all services
- `make clean` — removes containers, networks, and database volumes
- `make backup-db` — runs `pg_dump` inside the postgres container and saves a timestamped `.sql` file to `./backups/`
- `make restore-db` — restores a database from a `.sql` file in `./backups/`

#### Scenario: Database backup
- **WHEN** operator runs `make backup-db`
- **THEN** a file named `backup_<timestamp>.sql` is created in the `./backups/` directory containing the full database dump

#### Scenario: Database restore
- **WHEN** operator runs `make restore-db` with a valid backup file
- **THEN** the database state is restored from that backup file

#### Scenario: Clean reset
- **WHEN** operator runs `make clean`
- **THEN** all containers, networks, and database volumes are removed, returning to a clean state

---

### Requirement: Backups directory is mounted to postgres container
The system SHALL map a local `./backups` directory as a volume into the postgres container so that backup and restore commands can read/write `.sql` files.

#### Scenario: Backup file visibility
- **WHEN** a backup is created inside the postgres container
- **THEN** the `.sql` file appears in the local `./backups/` directory on the host
