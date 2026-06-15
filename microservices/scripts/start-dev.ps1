# Start Development Infrastructure for Inventory Microservices
# Usage: .\start-dev.ps1

param(
    [switch]$SkipBuild
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Starting Inventory Microservices Dev" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$composeFile = "docker-compose.yml"
if (-not (Test-Path $composeFile)) {
    Write-Host "Error: docker-compose.yml not found in current directory" -ForegroundColor Red
    exit 1
}

Write-Host "Starting infrastructure services..." -ForegroundColor Yellow
docker compose -f $composeFile up -d postgres redis nacos mongo minio kafka zookeeper elasticsearch

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start infrastructure services" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Waiting for services to be healthy..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

Write-Host ""
Write-Host "Infrastructure services status:" -ForegroundColor Cyan
docker compose -f $composeFile ps

if (-not $SkipBuild) {
    Write-Host ""
    Write-Host "Building common module..." -ForegroundColor Yellow
    .\gradlew.bat :common:assemble --no-daemon -q

    Write-Host ""
    Write-Host "Build complete. To start services, run:" -ForegroundColor Green
    Write-Host "  .\gradlew :core-services:product-service:bootRun --no-daemon" -ForegroundColor White
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Infrastructure started successfully" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
