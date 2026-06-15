#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env.prod"
BACKUP_DIR="./backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

source "$ENV_FILE" 2>/dev/null || {
    echo "Error: .env.prod file not found"
    exit 1
}

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

check_requirements() {
    log "Checking deployment requirements..."

    if ! command -v docker &> /dev/null; then
        log "Error: Docker is not installed"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log "Error: Docker Compose is not installed"
        exit 1
    fi

    if [ ! -f "$COMPOSE_FILE" ]; then
        log "Error: $COMPOSE_FILE not found"
        exit 1
    fi

    log "All requirements met"
}

backup_database() {
    log "Creating database backup..."

    mkdir -p "$BACKUP_DIR"

    BACKUP_FILE="$BACKUP_DIR/postgres_backup_${TIMESTAMP}.sql.gz"

    docker run --rm \
        --network inventory-net-prod \
        -e PGPASSWORD="$POSTGRES_PASSWORD" \
        postgres:18-alpine \
        pg_dump -h postgres -U postgres -d inventory_db \
        | gzip > "$BACKUP_FILE"

    log "Database backup created: $BACKUP_FILE"

    find "$BACKUP_DIR" -name "postgres_backup_*.sql.gz" -mtime +7 -delete
    log "Old backups cleaned up (keeping last 7 days)"
}

pull_images() {
    log "Pulling latest images..."

    docker-compose -f "$COMPOSE_FILE" pull

    log "Images updated"
}

deploy_services() {
    log "Deploying services..."

    docker-compose -f "$COMPOSE_FILE" up -d

    log "Services deployed"
}

health_check() {
    log "Performing health checks..."

    SERVICES=(
        "postgres:5432"
        "redis:6379"
        "nacos:8848"
        "gateway-service:8080"
        "registry-service:8761"
        "config-service:8888"
        "auth-service:8093"
        "product-service:8081"
        "order-service:8082"
        "inventory-service:8083"
    )

    for SERVICE in "${SERVICES[@]}"; do
        IFS=':' read -r HOST PORT <<< "$SERVICE"
        MAX_RETRIES=30
        RETRY_COUNT=0

        while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
            if docker exec inventory-${HOST}-prod curl -sf http://localhost:${PORT}/actuator/health &>/dev/null 2>&1; then
                log "$HOST is healthy"
                break
            fi

            RETRY_COUNT=$((RETRY_COUNT + 1))
            if [ $RETRY_COUNT -lt $MAX_RETRIES ]; then
                echo -n "."
                sleep 2
            fi
        done

        if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
            log "Warning: $HOST health check failed"
        fi
    done

    log "Health checks completed"
}

rollback() {
    log "Initiating rollback..."

    if [ -d "$BACKUP_DIR" ]; then
        LATEST_BACKUP=$(ls -t "$BACKUP_DIR"/postgres_backup_*.sql.gz 2>/dev/null | head -1)

        if [ -n "$LATEST_BACKUP" ]; then
            log "Restoring database from $LATEST_BACKUP..."

            docker run --rm \
                --network inventory-net-prod \
                -e PGPASSWORD="$POSTGRES_PASSWORD" \
                -v "$BACKUP_DIR:/backups" \
                postgres:18-alpine \
                psql -h postgres -U postgres -d inventory_db \
                -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;" \
                && gunzip -c "$LATEST_BACKUP" | psql -h postgres -U postgres -d inventory_db

            log "Database restored"
        fi
    fi

    log "Rolling back to previous images..."

    docker-compose -f "$COMPOSE_FILE" pull
    docker-compose -f "$COMPOSE_FILE" up -d

    log "Rollback completed"
}

show_status() {
    log "Showing service status..."

    docker-compose -f "$COMPOSE_FILE" ps

    echo ""
    log "Service health:"
    for SERVICE in $(docker-compose -f "$COMPOSE_FILE" ps --services); do
        STATUS=$(docker inspect --format='{{.State.Health.Status}}' inventory-${SERVICE}-prod 2>/dev/null || echo "no-healthcheck")
        echo "  $SERVICE: $STATUS"
    done
}

cleanup() {
    log "Cleaning up unused images..."

    docker image prune -f

    log "Cleanup completed"
}

usage() {
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  deploy     Deploy services to production"
    echo "  backup     Create database backup"
    echo "  rollback   Rollback to previous version"
    echo "  status     Show service status"
    echo "  cleanup    Clean up unused Docker resources"
    echo "  help       Show this help message"
}

case "${1:-deploy}" in
    deploy)
        check_requirements
        backup_database
        pull_images
        deploy_services
        health_check
        ;;
    backup)
        check_requirements
        backup_database
        ;;
    rollback)
        rollback
        ;;
    status)
        show_status
        ;;
    cleanup)
        cleanup
        ;;
    help|--help|-h)
        usage
        ;;
    *)
        echo "Unknown command: $1"
        usage
        exit 1
        ;;
esac
