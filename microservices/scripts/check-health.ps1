# Health Check Script for Inventory Microservices
# Usage: .\check-health.ps1 [-Service <name>] [-Port <port>] [-ConfigFile <path>]
# Environment variables can override port settings: <SERVICE_NAME>_PORT (e.g., PRODUCT_SERVICE_PORT=8081)

param(
    [string]$Service = "",
    [int]$Timeout = 5,
    [string]$ConfigFile = ""
)

$ErrorActionPreference = "Continue"

function Get-ServicePortsFromEnv {
    $portMap = @{}
    $serviceNames = @(
        "product-service", "order-service", "inventory-service", "sales-service",
        "procurement-service", "customer-service", "supplier-service", "mall-service",
        "business-partner-service", "datasource-service", "template-service",
        "invoice-service", "admin-service", "auth-service", "finance-service",
        "gateway-service", "registry-service", "config-service"
    )

    foreach ($name in $serviceNames) {
        $envVar = ($name -replace "-", "_").ToUpper() + "_PORT"
        $port = [Environment]::GetEnvironmentVariable($envVar)
        if ($port) {
            $portMap[$name] = [int]$port
        }
    }
    return $portMap
}

function Get-ServicePortsFromConfig {
    param([string]$Path)
    $portMap = @{}
    if (Test-Path $Path) {
        try {
            $config = Get-Content $Path -Raw | ConvertFrom-Json
            foreach ($prop in $config.PSObject.Properties) {
                if ($prop.Value -is [int]) {
                    $portMap[$prop.Name] = $prop.Value
                }
            }
            Write-Host "[INFO] Loaded port configuration from $Path" -ForegroundColor Cyan
        } catch {
            Write-Host "[WARN] Failed to parse config file: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
    return $portMap
}

$envPortMap = Get-ServicePortsFromEnv
$configPortMap = if ($ConfigFile) { Get-ServicePortsFromConfig -Path $ConfigFile } else { @{} }

$services = @(
    @{ Name = "product-service"; Port = if ($envPortMap["product-service"]) { $envPortMap["product-service"] } elseif ($configPortMap["product-service"]) { $configPortMap["product-service"] } else { 8081 } },
    @{ Name = "order-service"; Port = if ($envPortMap["order-service"]) { $envPortMap["order-service"] } elseif ($configPortMap["order-service"]) { $configPortMap["order-service"] } else { 8082 } },
    @{ Name = "inventory-service"; Port = if ($envPortMap["inventory-service"]) { $envPortMap["inventory-service"] } elseif ($configPortMap["inventory-service"]) { $configPortMap["inventory-service"] } else { 8083 } },
    @{ Name = "sales-service"; Port = if ($envPortMap["sales-service"]) { $envPortMap["sales-service"] } elseif ($configPortMap["sales-service"]) { $configPortMap["sales-service"] } else { 8084 } },
    @{ Name = "procurement-service"; Port = if ($envPortMap["procurement-service"]) { $envPortMap["procurement-service"] } elseif ($configPortMap["procurement-service"]) { $configPortMap["procurement-service"] } else { 8085 } },
    @{ Name = "customer-service"; Port = if ($envPortMap["customer-service"]) { $envPortMap["customer-service"] } elseif ($configPortMap["customer-service"]) { $configPortMap["customer-service"] } else { 8086 } },
    @{ Name = "supplier-service"; Port = if ($envPortMap["supplier-service"]) { $envPortMap["supplier-service"] } elseif ($configPortMap["supplier-service"]) { $configPortMap["supplier-service"] } else { 8087 } },
    @{ Name = "mall-service"; Port = if ($envPortMap["mall-service"]) { $envPortMap["mall-service"] } elseif ($configPortMap["mall-service"]) { $configPortMap["mall-service"] } else { 8088 } },
    @{ Name = "business-partner-service"; Port = if ($envPortMap["business-partner-service"]) { $envPortMap["business-partner-service"] } elseif ($configPortMap["business-partner-service"]) { $configPortMap["business-partner-service"] } else { 8089 } },
    @{ Name = "datasource-service"; Port = if ($envPortMap["datasource-service"]) { $envPortMap["datasource-service"] } elseif ($configPortMap["datasource-service"]) { $configPortMap["datasource-service"] } else { 8090 } },
    @{ Name = "template-service"; Port = if ($envPortMap["template-service"]) { $envPortMap["template-service"] } elseif ($configPortMap["template-service"]) { $configPortMap["template-service"] } else { 8091 } },
    @{ Name = "invoice-service"; Port = if ($envPortMap["invoice-service"]) { $envPortMap["invoice-service"] } elseif ($configPortMap["invoice-service"]) { $configPortMap["invoice-service"] } else { 8092 } },
    @{ Name = "admin-service"; Port = if ($envPortMap["admin-service"]) { $envPortMap["admin-service"] } elseif ($configPortMap["admin-service"]) { $configPortMap["admin-service"] } else { 8093 } },
    @{ Name = "auth-service"; Port = if ($envPortMap["auth-service"]) { $envPortMap["auth-service"] } elseif ($configPortMap["auth-service"]) { $configPortMap["auth-service"] } else { 8094 } },
    @{ Name = "finance-service"; Port = if ($envPortMap["finance-service"]) { $envPortMap["finance-service"] } elseif ($configPortMap["finance-service"]) { $configPortMap["finance-service"] } else { 8095 } },
    @{ Name = "gateway-service"; Port = if ($envPortMap["gateway-service"]) { $envPortMap["gateway-service"] } elseif ($configPortMap["gateway-service"]) { $configPortMap["gateway-service"] } else { 8096 } },
    @{ Name = "registry-service"; Port = if ($envPortMap["registry-service"]) { $envPortMap["registry-service"] } elseif ($configPortMap["registry-service"]) { $configPortMap["registry-service"] } else { 8761 } },
    @{ Name = "config-service"; Port = if ($envPortMap["config-service"]) { $envPortMap["config-service"] } elseif ($configPortMap["config-service"]) { $configPortMap["config-service"] } else { 8888 } }
)

function Test-ServiceHealth {
    param([string]$Name, [int]$Port)

    $url = "http://localhost:$Port/actuator/health"
    try {
        $response = Invoke-WebRequest -Uri $url -TimeoutSec $Timeout -UseBasicParsing -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host "[PASS] $Name (port $Port)" -ForegroundColor Green
            return $true
        }
    } catch {
        Write-Host "[FAIL] $Name (port $Port) - $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
    Write-Host "[FAIL] $Name (port $Port)" -ForegroundColor Red
    return $false
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Inventory Microservices Health Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($Service) {
    $svc = $services | Where-Object { $_.Name -eq $Service }
    if ($svc) {
        Test-ServiceHealth -Name $svc.Name -Port $svc.Port
    } else {
        Write-Host "Service '$Service' not found" -ForegroundColor Yellow
        Write-Host "Available services: $($services.Name -join ', ')" -ForegroundColor Yellow
    }
} else {
    $passCount = 0
    $failCount = 0

    foreach ($svc in $services) {
        $result = Test-ServiceHealth -Name $svc.Name -Port $svc.Port
        if ($result) { $passCount++ } else { $failCount++ }
    }

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Summary: $passCount passed, $failCount failed" -ForegroundColor $(if ($failCount -eq 0) { "Green" } else { "Yellow" })
    Write-Host "========================================" -ForegroundColor Cyan
}
