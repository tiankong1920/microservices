#!/bin/bash
set -e

echo "Starting infrastructure services..."
docker-compose -f docker-compose.yml up -d postgres redis nacos

echo "Waiting for infrastructure to be ready..."
sleep 30

echo "Starting support services..."
docker-compose -f docker-compose.yml up -d registry-service config-service

echo "Waiting for support services..."
sleep 20

echo "Starting core services..."
# Start core services in parallel
for service in product-service order-service inventory-service; do
  docker-compose -f docker-compose.yml up -d "$service" &
done
wait

echo "Starting additional core services..."
for service in auth-service admin-service finance-service; do
  docker-compose -f docker-compose.yml up -d "$service" &
done
wait

echo "Starting gateway..."
docker-compose -f docker-compose.yml up -d gateway-service

echo "All services started. Verifying health..."
docker-compose -f docker-compose.yml ps
