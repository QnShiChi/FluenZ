# ===========================================
# FluenZ - Docker Management
# ===========================================

.PHONY: build up down logs clean backup-db restore-db

# Load environment variables
include .env
export

# ---------- Core Commands ----------

## Build all Docker images
build:
	docker compose build

## Start all containers in detached mode
up:
	docker compose up -d

## Stop and remove containers
down:
	docker compose down

## Tail logs for all services
logs:
	docker compose logs -f

## Remove containers, networks, and database volumes
clean:
	docker compose down -v --remove-orphans

# ---------- Database Commands ----------

## Create a timestamped PostgreSQL backup
backup-db:
	@mkdir -p ./backups
	@echo "Creating database backup..."
	docker compose exec postgres pg_dump -U $(POSTGRES_USER) -d $(POSTGRES_DB) > ./backups/backup_$$(date +%Y%m%d_%H%M%S).sql
	@echo "✓ Backup saved to ./backups/"

## Restore database from a backup file
## Usage: make restore-db FILE=./backups/backup_20240101_120000.sql
restore-db:
ifndef FILE
	@echo "Usage: make restore-db FILE=./backups/<filename>.sql"
	@echo ""
	@echo "Available backups:"
	@ls -1 ./backups/*.sql 2>/dev/null || echo "  (no backups found)"
	@exit 1
endif
	@echo "Restoring database from $(FILE)..."
	docker compose exec -T postgres psql -U $(POSTGRES_USER) -d $(POSTGRES_DB) < $(FILE)
	@echo "✓ Database restored from $(FILE)"
