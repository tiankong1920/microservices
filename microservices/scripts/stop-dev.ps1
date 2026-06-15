# Stop All Docker Containers for Inventory Microservices
# Usage: .\stop-dev.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Stopping Inventory Microservices" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$containers = @(
    "inventory-postgres",
    "inventory-redis",
    "inventory-nacos",
    "inventory-mongo",
    "inventory-minio",
    "inventory-kafka",
    "inventory-zookeeper",
    "inventory-elasticsearch"
)

$runningContainers = @()
foreach ($name in $containers) {
    $container = docker ps -a --filter "name=$name" --format "{{.Names}}" 2>$null
    if ($container) {
        $runningContainers += $container
    }
}

if ($runningContainers.Count -eq 0) {
    Write-Host "No inventory containers are running." -ForegroundColor Yellow
} else {
    Write-Host "Stopping containers: $($runningContainers -join ', ')" -ForegroundColor Yellow
    foreach ($container in $runningContainers) {
        docker stop $container 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  Stopped: $container" -ForegroundColor Green
        } else {
            Write-Host "  Failed to stop: $container" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "Removing stopped containers..." -ForegroundColor Cyan
docker container prune -f 2>$null

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All containers stopped" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
