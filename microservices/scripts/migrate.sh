#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MIGRATION_LOG="$SCRIPT_DIR/migration_$(date +%Y%m%d_%H%M%S).log"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$MIGRATION_LOG"
}

log "Starting database migration..."

check_connection() {
    log "Checking database connection..."
    if PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1;" &>/dev/null; then
        log "Database connection successful"
    else
        log "Error: Cannot connect to database"
        exit 1
    fi
}

create_migration_table() {
    log "Creating migration tracking table..."

    PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" <<-EOSQL
        CREATE TABLE IF NOT EXISTS schema_migrations (
            version VARCHAR(255) PRIMARY KEY,
            applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            description TEXT
        );
	EOSQL

    log "Migration tracking table ready"
}

run_migration() {
    local VERSION=$1
    local SQL_FILE=$2

    log "Checking migration $VERSION..."

    local APPLIED=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM schema_migrations WHERE version='$VERSION';" 2>/dev/null | tr -d ' ')

    if [ "$APPLIED" -eq 0 ]; then
        log "Applying migration $VERSION..."

        PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -f "$SQL_FILE" 2>&1 | tee -a "$MIGRATION_LOG"

        PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -c "INSERT INTO schema_migrations (version, description) VALUES ('$VERSION', 'Applied via migration script');" &>/dev/null

        log "Migration $VERSION completed"
    else
        log "Migration $VERSION already applied, skipping"
    fi
}

rollback_migration() {
    local VERSION=$1
    local SQL_FILE=$2

    log "Rolling back migration $VERSION..."

    if [ -f "$SQL_FILE" ]; then
        log "Warning: Manual rollback required - see $SQL_FILE.rollback"
    fi

    PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -c "DELETE FROM schema_migrations WHERE version='$VERSION';"

    log "Rollback of $VERSION recorded"
}

usage() {
    echo "Usage: $0 [up|down|status] [--version VERSION]"
    echo ""
    echo "Commands:"
    echo "  up       Run pending migrations"
    echo "  down     Rollback last migration"
    echo "  status   Show migration status"
    echo ""
    echo "Options:"
    echo "  --version VERSION    Specify migration version (for rollback)"
}

case "${1:-up}" in
    up)
        check_connection
        create_migration_table

        run_migration "V1__initial_schema" "$SCRIPT_DIR/V1__initial_schema.sql"
        run_migration "V2__add_procurement_orders" "$SCRIPT_DIR/V2__add_procurement_orders.sql"
        run_migration "V3__add_business_partner" "$SCRIPT_DIR/V3__add_business_partner.sql"
        run_migration "V4__add_inventory_alerts" "$SCRIPT_DIR/V4__add_inventory_alerts.sql"

        log "All migrations completed"
        ;;
    down)
        check_connection
        ROLLBACK_VERSION="${2:-V4__add_inventory_alerts}"
        ROLLBACK_SQL="$SCRIPT_DIR/${ROLLBACK_VERSION}.rollback.sql"
        rollback_migration "$ROLLBACK_VERSION" "$ROLLBACK_SQL"
        ;;
    status)
        check_connection
        log "Migration status:"
        PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -c "SELECT version, applied_at, description FROM schema_migrations ORDER BY applied_at;"
        ;;
    *)
        usage
        exit 1
        ;;
esac
