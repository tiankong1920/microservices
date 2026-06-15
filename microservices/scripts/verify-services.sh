#!/bin/bash
set -euo pipefail

# Verify all services are healthy via actuator health endpoint
declare -a SERVICES=(
    "product-service:8081"
    "order-service:8082"
    "inventory-service:8083"
    "customer-service:8086"
    "gateway-service:8080"
)

for entry in "${SERVICES[@]}"; do
  IFS=':' read -r name port <<< "$entry"
  echo "Checking $name on port $port..."
  code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health || echo "000")
  if [ "$code" == "200" ]; then
    echo "[OK] $name is healthy (HTTP 200)"
  else
    echo "[WARN] $name health check failed (HTTP $code)"
  fi
done
