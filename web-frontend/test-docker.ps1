# Web Frontend Docker Test Runner (PowerShell / Cross-Platform)
# Usage:
#   pwsh ./test-docker.ps1                # Run all tests
#   pwsh ./test-docker.ps1 -Verbose       # Verbose reporter
#   pwsh ./test-docker.ps1 -Coverage      # With coverage
#   pwsh ./test-docker.ps1 -Rebuild       # Force rebuild image
#   pwsh ./test-docker.ps1 -File X        # Run specific test file

param(
    [switch]$Verbose,
    [switch]$Coverage,
    [switch]$Rebuild,
    [switch]$NoCache,
    [string]$File
)

$ErrorActionPreference = "Stop"
$ImageName = "web-frontend-test"

# Change to script directory
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

Write-Host "============================================" -ForegroundColor Cyan
Write-Host " Web Frontend Docker Test Runner" -ForegroundColor Cyan
Write-Host " Image: $ImageName" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

# Step 1: Check if image exists
$ImageExists = docker images --format "{{.Repository}}:{{.Tag}}" | Select-String -Pattern "^${ImageName}:latest$"
if (-not $ImageExists -or $Rebuild) {
    Write-Host "`n[1/3] Building Docker image..." -ForegroundColor Yellow
    $BuildArgs = @()
    if ($NoCache) { $BuildArgs += "--no-cache" }
    $BuildArgs += @("-f", "Dockerfile.test", "-t", "${ImageName}:latest", ".")
    & docker build @BuildArgs
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Docker build failed" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "`n[1/3] Using cached image: ${ImageName}:latest" -ForegroundColor Green
}

# Step 2: Build vitest command
$VitestCmd = @("npx", "vitest", "run")
if ($Verbose) { $VitestCmd += "--reporter=verbose" }
if ($Coverage) { $VitestCmd += "--coverage" }
if ($File) { $VitestCmd += $File }

# Step 3: Run tests in container
Write-Host "`n[2/3] Running tests in container..." -ForegroundColor Yellow
Write-Host "       Command: $($VitestCmd -join ' ')" -ForegroundColor Gray
Write-Host ""

$ContainerPath = if ($IsWindows -or $env:OS -eq "Windows_NT") {
    "${PWD}:/app"
} else {
    "${PWD}:/app"
}

& docker run --rm `
    -v $ContainerPath `
    -v /app/node_modules `
    -e CI=$true `
    -e NODE_ENV=test `
    $ImageName:latest `
    @VitestCmd

$TestExit = $LASTEXITCODE

# Step 4: Report
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
if ($TestExit -eq 0) {
    Write-Host " [3/3] PASSED - All tests passed in Docker" -ForegroundColor Green
} else {
    Write-Host " [3/3] FAILED - Tests failed with exit code $TestExit" -ForegroundColor Red
}
Write-Host "============================================" -ForegroundColor Cyan

exit $TestExit
