#!/usr/bin/env pwsh
# Generate TypeScript types from OpenAPI specification
# Usage: .\generate-types.ps1 [-ServiceUrl <url>] [-OutputDir <path>]
param(
    [string]$ServiceUrl = "http://localhost:8080",
    [string]$OutputDir = "../frontend/src/api"
)

$ErrorActionPreference = "Stop"

Write-Host "Generating TypeScript types from OpenAPI specification..." -ForegroundColor Cyan

$OpenApiUrl = "$ServiceUrl/v3/api-docs"
$OutputFile = "$OutputDir/api-types.ts"

if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

Write-Host "Fetching OpenAPI spec from: $OpenApiUrl" -ForegroundColor Yellow

try {
    Invoke-WebRequest -Uri $OpenApiUrl -OutFile "$OutputDir/openapi.json" -TimeoutSec 30
    Write-Host "OpenAPI spec downloaded successfully" -ForegroundColor Green
} catch {
    Write-Host "Error: Could not fetch OpenAPI spec. Is the service running?" -ForegroundColor Red
    Write-Host "Start services with: .\start-dev.ps1" -ForegroundColor Yellow
    exit 1
}

if (Get-Command npx -ErrorAction SilentlyContinue) {
    Write-Host "Running openapi-typescript..." -ForegroundColor Yellow
    npx openapi-typescript "$OutputDir/openapi.json" -o $OutputFile

    if ($LASTEXITCODE -eq 0) {
        Write-Host "TypeScript types generated: $OutputFile" -ForegroundColor Green
        Remove-Item "$OutputDir/openapi.json" -Force
    } else {
        Write-Host "Warning: openapi-typescript failed. Manual steps:" -ForegroundColor Yellow
        Write-Host "  1. Install: npm install -g openapi-typescript" -ForegroundColor White
        Write-Host "  2. Run: npx openapi-typescript $OutputDir/openapi.json -o $OutputFile" -ForegroundColor White
    }
} else {
    Write-Host "Node.js not found. Manual steps:" -ForegroundColor Yellow
    Write-Host "  1. Install Node.js from https://nodejs.org" -ForegroundColor White
    Write-Host "  2. npm install -g openapi-typescript" -ForegroundColor White
    Write-Host "  3. npx openapi-typescript $OutputDir/openapi.json -o $OutputFile" -ForegroundColor White
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Cyan
