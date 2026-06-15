#!/usr/bin/env pwsh
# Download OpenTelemetry Java Agent for tracing
# Usage: .\download-otel-agent.ps1 [-Version <version>] [-OutputDir <path>]
param(
    [string]$Version = "2.1.0",
    [string]$OutputDir = "../agents"
)

$OtelAgentUrl = "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${Version}/opentelemetry-javaagent.jar"
$OutputFile = "$OutputDir/opentelemetry-javaagent-${Version}.jar"

if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

if (Test-Path $OutputFile) {
    Write-Host "OpenTelemetry agent already exists at: $OutputFile" -ForegroundColor Green
    exit 0
}

Write-Host "Downloading OpenTelemetry Java Agent v${Version}..." -ForegroundColor Cyan
try {
    Invoke-WebRequest -Uri $OtelAgentUrl -OutFile $OutputFile -TimeoutSec 120
    Write-Host "Downloaded to: $OutputFile" -ForegroundColor Green
} catch {
    Write-Host "Error: Failed to download agent. Check version and network." -ForegroundColor Red
    Write-Host "Latest releases: https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases" -ForegroundColor Yellow
    exit 1
}
