# Reset Database and Run Migrations for Inventory Microservices
# Usage: .\reset-db.ps1 [-Service <name>] [-DropAll]

param(
    [string]$Service = "",
    [switch]$DropAll
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Reset Database for Inventory Microservices" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$services = @(
    "product-service",
    "order-service",
    "inventory-service",
    "sales-service",
    "procurement-service",
    "customer-service",
    "supplier-service",
    "business-partner-service",
    "mall-service",
    "datasource-service",
    "template-service",
    "invoice-service",
    "admin-service",
    "auth-service",
    "finance-service",
    "report-service"
)

function Reset-ServiceDatabase {
    param([string]$ServiceName)

    Write-Host "Resetting database for: $ServiceName" -ForegroundColor Yellow

    $serviceDir = "core-services/$ServiceName"
    if (-not (Test-Path $serviceDir)) {
        $serviceDir = "support-services/$ServiceName"
    }

    $migrateScript = "scripts/migrate.sh"
    if (Test-Path $migrateScript) {
        Write-Host "  Running migrations via: $migrateScript" -ForegroundColor Gray
    } else {
        Write-Host "  Flyway migration task" -ForegroundColor Gray
        .\gradlew.bat :$serviceDir:flywayMigrate --no-daemon -q
    }

    Write-Host "  Database reset complete: $ServiceName" -ForegroundColor Green
}

if ($DropAll) {
    Write-Host "WARNING: Dropping all databases!" -ForegroundColor Red
    Write-Host "This will delete all data. Are you sure? (Ctrl+C to cancel)" -ForegroundColor Red
    Start-Sleep -Seconds 5
}

if ($Service) {
    $svc = $services | Where-Object { $_ -eq $Service }
    if ($svc) {
        Reset-ServiceDatabase -ServiceName $Service
    } else {
        Write-Host "Service '$Service' not found" -ForegroundColor Red
        Write-Host "Available services: $($services -join ', ')" -ForegroundColor Yellow
    }
} else {
    foreach ($svc in $services) {
        Reset-ServiceDatabase -ServiceName $svc
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All databases reset complete" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
