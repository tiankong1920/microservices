# Inventory Management System - Production Deployment Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Pre-Deployment Checklist](#pre-deployment-checklist)
3. [Deployment Steps](#deployment-steps)
4. [Post-Deployment Verification](#post-deployment-verification)
5. [Rollback Procedures](#rollback-procedures)
6. [Emergency Contacts](#emergency-contacts)

## Prerequisites

### Hardware Requirements
- **Production Server**: 8+ CPU cores, 32GB+ RAM
- **Database Server**: 4+ CPU cores, 16GB+ RAM, 500GB+ SSD
- **Monitoring Server**: 4+ CPU cores, 8GB+ RAM

### Software Requirements
- Docker 24.0+
- Docker Compose 2.20+
- PostgreSQL 18 client tools
- curl, bash 4+

### Network Requirements
- Port 80, 443 (HTTP/HTTPS)
- Port 5432 (PostgreSQL - internal only)
- Port 6379 (Redis - internal only)
- Port 9090 (Prometheus)
- Port 3000 (Grafana)

---

## Pre-Deployment Checklist

### 1. Environment Configuration
```bash
# Copy environment template
cp .env.prod.template .env.prod

# Edit with production values
vim .env.prod
```

Required environment variables:
- [ ] `POSTGRES_PASSWORD` - Strong password (min 16 chars)
- [ ] `REDIS_PASSWORD` - Strong password
- [ ] `GRAFANA_PASSWORD` - Admin password
- [ ] `JWT_SECRET` - Secret key (min 32 chars)
- [ ] `NACOS_PASSWORD` - Nacos admin password
- [ ] `ALERT_EMAIL_*` - SMTP configuration

### 2. Database Backup
```bash
# Create full backup before deployment
./scripts/migrate.sh backup
```

### 3. DNS Configuration
- [ ] Configure DNS for `api.inventory-system.com`
- [ ] Configure SSL certificates
- [ ] Configure load balancer health checks

### 4. Security Review
- [ ] Firewall rules configured
- [ ] SSL/TLS certificates valid
- [ ] Database credentials rotated
- [ ] API keys secured in vault

---

## Deployment Steps

### Step 1: Infrastructure Setup
```bash
# Create Docker network
docker network create inventory-net-prod

# Start base services first
docker-compose -f docker-compose.prod.yml up -d postgres redis
```

### Step 2: Run Database Migrations
```bash
# Set database environment variables
export DB_HOST=localhost
export DB_USER=postgres
export DB_PASSWORD=your_password
export DB_NAME=inventory_db

# Run migrations
./scripts/migrate.sh up
```

### Step 3: Deploy Core Infrastructure Services
```bash
# Start registry, config, and gateway
docker-compose -f docker-compose.prod.yml up -d registry-service config-service gateway-service

# Verify services are healthy
docker-compose -f docker-compose.prod.yml ps
```

### Step 4: Deploy Business Services
```bash
# Deploy in order (dependencies)
docker-compose -f docker-compose.prod.yml up -d \
  auth-service \
  product-service \
  inventory-service \
  customer-service \
  supplier-service \
  order-service \
  sales-service \
  procurement-service \
  business-partner-service
```

### Step 5: Deploy Monitoring Stack
```bash
# Start monitoring
docker-compose -f docker-compose.prod.yml up -d prometheus grafana alertmanager
```

### Step 6: Deploy Frontend
```bash
# Build and deploy frontend
cd mall-frontend
npm run build
# Deploy dist to web server
```

---

## Post-Deployment Verification

### Health Check Script
```bash
#!/bin/bash
SERVICES=(
  "gateway-service:8080"
  "auth-service:8093"
  "product-service:8081"
  "order-service:8082"
  "inventory-service:8083"
)

for SERVICE in "${SERVICES[@]}"; do
  HOST=$(echo $SERVICE | cut -d: -f1)
  PORT=$(echo $SERVICE | cut -d: -f2)

  if curl -sf http://localhost:${PORT}/actuator/health | grep -q '"status":"UP"'; then
    echo "✓ $HOST is healthy"
  else
    echo "✗ $HOST is UNHEALTHY"
  fi
done
```

### Smoke Tests
```bash
# Test API Gateway
curl -sf http://localhost:8080/actuator/health

# Test Authentication
curl -sf -X POST http://localhost:8093/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'

# Test Product Service
curl -sf http://localhost:8081/api/products
```

### Monitoring Verification
- [ ] Prometheus targets all healthy
- [ ] Grafana dashboards loading
- [ ] Alert rules firing correctly
- [ ] Log aggregation working

---

## Rollback Procedures

### Automatic Rollback
```bash
# If using the deploy script, rollback is built-in
./scripts/deploy-prod.sh rollback
```

### Manual Rollback

#### Step 1: Stop Services
```bash
docker-compose -f docker-compose.prod.yml down
```

#### Step 2: Restore Database
```bash
# List available backups
ls -la backups/postgres_backup_*.sql.gz

# Restore from backup
gunzip -c backups/postgres_backup_YYYYMMDD_HHMMSS.sql.gz | \
  PGPASSWORD="$DB_PASSWORD" psql -h localhost -U postgres -d inventory_db
```

#### Step 3: Restore Previous Images
```bash
# Pull previous version
docker pull inventory/product-service:v2.x.x

# Start services with previous version
docker-compose -f docker-compose.prod.yml up -d
```

### Rollback Decision Matrix

| Issue Severity | Action | Time Limit |
|----------------|--------|------------|
| Critical (Data loss, Security breach) | Immediate rollback | 5 minutes |
| High (Service down, Major feature broken) | Rollback | 15 minutes |
| Medium (Feature partially working) | Deploy fix | 1 hour |
| Low (UI issues, Minor bugs) | Next sprint | Planned |

---

## Emergency Contacts

| Role | Contact | Escalation |
|------|---------|------------|
| On-Call Engineer | +1-xxx-xxx-xxxx | N/A |
| DevOps Lead | +1-xxx-xxx-xxxx | +1-xxx-xxx-xxxx |
| Engineering Manager | +1-xxx-xxx-xxxx | CTO |
| Database Admin | +1-xxx-xxx-xxxx | +1-xxx-xxx-xxxx |

---

## Post-Deployment Tasks

### 1. Documentation
- [ ] Update runbook with deployment time
- [ ] Document any deviations from procedure
- [ ] Update API documentation if needed

### 2. Monitoring
- [ ] Verify all alerts are firing correctly
- [ ] Check error rates in dashboards
- [ ] Review performance metrics

### 3. Communication
- [ ] Notify stakeholders of successful deployment
- [ ] Update status page
- [ ] Send deployment summary

---

## Version Information

| Component | Version | Deployed At |
|-----------|---------|-------------|
| Application | 3.0.0 | YYYY-MM-DD |
| Database | 18-alpine | YYYY-MM-DD |
| Redis | 7-alpine | YYYY-MM-DD |
| Java | 21 | YYYY-MM-DD |
