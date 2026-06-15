# ============================================================================
# Alert Rules Verification Script (MON-002)
# ============================================================================
# Purpose: Verify Prometheus alert rules are correctly configured
# Executor: Operations Team
# Prerequisites: Prometheus/Alertmanager must be running
# ============================================================================

param(
    [string]$PrometheusUrl = "http://localhost:9090",
    [string]$AlertmanagerUrl = "http://localhost:9093",
    [switch]$TestAll,
    [string]$AlertName = ""
)

$ErrorActionPreference = "Continue"

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "Alert Rules Verification Script (MON-002)" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

function Write-Step {
    param([string]$Message)
    Write-Host "[$((Get-Date).ToString('HH:mm:ss'))] $Message" -ForegroundColor Yellow
}

function Write-Success {
    param([string]$Message)
    Write-Host "[$((Get-Date).ToString('HH:mm:ss'))] SUCCESS: $Message" -ForegroundColor Green
}

function Write-Failure {
    param([string]$Message)
    Write-Host "[$((Get-Date).ToString('HH:mm:ss'))] FAIL: $Message" -ForegroundColor Red
}

function Get-PrometheusAlertRules {
    param([string]$Url)

    try {
        $response = Invoke-RestMethod -Uri "$Url/api/v1/rules" -TimeoutSec 10
        if ($response.status -eq "success") {
            return $response.data.groups
        }
    }
    catch {
        Write-Failure "Cannot connect to Prometheus: $_"
    }
    return $null
}

function Get-AlertmanagerAlerts {
    param([string]$Url)

    try {
        $response = Invoke-RestMethod -Uri "$Url/api/v1/alerts" -TimeoutSec 10
        return $response.data
    }
    catch {
        Write-Failure "Cannot connect to Alertmanager: $_"
    }
    return @()
}

function Test-AlertRule {
    param(
        [string]$AlertName,
        [string]$PrometheusUrl
    )

    Write-Step "Testing alert: $AlertName"

    $alertFound = $false
    $alertState = "inactive"
    $lastEvaluation = $null

    $rules = Get-PrometheusAlertRules -Url $PrometheusUrl
    if ($rules) {
        foreach ($group in $rules) {
            foreach ($rule in $group.rules) {
                if ($rule.name -eq $AlertName) {
                    $alertFound = $true
                    $alertState = $rule.state
                    $lastEvaluation = $rule.lastEvaluation
                    break
                }
            }
        }
    }

    if (-not $alertFound) {
        Write-Failure "  Alert rule '$AlertName' not found"
        return @{
            AlertName = $AlertName
            Status = "NOT_FOUND"
            Message = "Alert rule not found in Prometheus"
        }
    }

    Write-Step "  State: $alertState"
    Write-Step "  Last evaluation: $lastEvaluation"

    if ($alertState -eq "pending") {
        Write-Step "  Alert is pending, waiting for duration threshold..."
        return @{
            AlertName = $AlertName
            Status = "PENDING"
            Message = "Alert is pending, waiting for duration threshold"
        }
    }
    elseif ($alertState -eq "firing") {
        Write-Success "  Alert is firing!"
        return @{
            AlertName = $AlertName
            Status = "FIRING"
            Message = "Alert is firing"
        }
    }
    else {
        Write-Success "  Alert status is normal (inactive)"
        return @{
            AlertName = $AlertName
            Status = "OK"
            Message = "Alert is inactive (healthy)"
        }
    }
}

function Test-AlertNotification {
    param(
        [string]$AlertName,
        [string]$AlertmanagerUrl
    )

    Write-Step "Checking alert notification: $AlertName"

    $alerts = Get-AlertmanagerAlerts -Url $AlertmanagerUrl

    $matchingAlert = $alerts | Where-Object { $_.labels.alertname -eq $AlertName }

    if ($matchingAlert) {
        Write-Success "  Alert notification sent"
        return @{
            AlertName = $AlertName
            Status = "NOTIFIED"
            Message = "Alert notification sent to receivers"
        }
    }
    else {
        Write-Step "  No active alert notification found"
        return @{
            AlertName = $AlertName
            Status = "NO_NOTIFICATION"
            Message = "No active alert notification found"
        }
    }
}

$AlertRules = @(
    @{ Name = "HighCPUUsage"; Category = "Infrastructure" },
    @{ Name = "HighMemoryUsage"; Category = "Infrastructure" },
    @{ Name = "ServiceDown"; Category = "Infrastructure" },
    @{ Name = "DatabaseConnectionFailure"; Category = "Infrastructure" },
    @{ Name = "KafkaConsumerLag"; Category = "Infrastructure" },
    @{ Name = "LowInventoryLevel"; Category = "Business" },
    @{ Name = "OrderProcessingDelay"; Category = "Business" },
    @{ Name = "PaymentFailureRate"; Category = "Business" },
    @{ Name = "HighOrderCancellationRate"; Category = "Business" },
    @{ Name = "ApiLatencyHigh"; Category = "Performance" }
)

Write-Host "Prometheus URL: $PrometheusUrl" -ForegroundColor Yellow
Write-Host "Alertmanager URL: $AlertmanagerUrl" -ForegroundColor Yellow
Write-Host ""

$Results = @()

if ($AlertName) {
    $Results += Test-AlertRule -AlertName $AlertName -PrometheusUrl $PrometheusUrl
    $Results += Test-AlertNotification -AlertName $AlertName -AlertmanagerUrl $AlertmanagerUrl
}
elseif ($TestAll) {
    foreach ($rule in $AlertRules) {
        Write-Step "----------------------------------------"
        Write-Step "Testing: $($rule.Name) [$($rule.Category)]"
        $Results += Test-AlertRule -AlertName $rule.Name -PrometheusUrl $PrometheusUrl
        $Results += Test-AlertNotification -AlertName $rule.Name -AlertmanagerUrl $AlertmanagerUrl
        Write-Host ""
    }
}
else {
    Write-Host "Use -TestAll to verify all alert rules" -ForegroundColor Yellow
    Write-Host "Use -AlertName <name> to test specific alert" -ForegroundColor Yellow
}

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "Alert Rules Verification Summary" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan

$StatusCounts = @{}
foreach ($result in $Results) {
    if (-not $StatusCounts.ContainsKey($result.Status)) {
        $StatusCounts[$result.Status] = 0
    }
    $StatusCounts[$result.Status]++
}

Write-Host ""
Write-Host "Status Statistics:" -ForegroundColor Yellow
foreach ($status in $StatusCounts.Keys) {
    $count = $StatusCounts[$status]
    $color = switch ($status) {
        "OK" { "Green" }
        "FIRING" { "Red" }
        "PENDING" { "Yellow" }
        "NOTIFIED" { "Green" }
        default { "White" }
    }
    Write-Host "  $status : $count" -ForegroundColor $color
}

Write-Host ""

$totalRules = ($Results | Where-Object { $_.Status -ne "NOTIFIED" }).Count
$okRules = ($Results | Where-Object { $_.Status -eq "OK" }).Count
$passRate = if ($totalRules -gt 0) { [math]::Round(($okRules / $totalRules) * 100, 1) } else { 0 }

Write-Host "Pass Rate: $passRate% ($okRules / $totalRules)" -ForegroundColor $(if ($passRate -ge 80) { "Green" } else { "Yellow" })

Write-Host ""
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "Verification Completed - $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan

exit $(if ($passRate -ge 80) { 0 } else { 1 })
