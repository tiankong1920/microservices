# Docker Registry Mirror Setup
# Configures Chinese mirror accelerators and restarts Docker Desktop

$ErrorActionPreference = "Stop"

$DaemonJson = Join-Path $env:USERPROFILE ".docker\daemon.json"

# Backup original config
if (Test-Path $DaemonJson) {
    $Backup = $DaemonJson + ".bak." + (Get-Date -Format "yyyyMMddHHmmss")
    Copy-Item $DaemonJson $Backup -Force
    Write-Host "[1/5] Backed up: $Backup" -ForegroundColor Green
}

# Write new daemon.json (with Chinese mirrors)
$NewConfig = @"
{
  "builder": {
    "gc": {
      "defaultKeepStorage": "20GB",
      "enabled": true
    }
  },
  "experimental": false,
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://dockerproxy.com",
    "https://mirror.ccs.tencentyun.com",
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com"
  ],
  "max-concurrent-downloads": 10,
  "log-driver": "json-file",
  "log-level": "warn"
}
"@

[System.IO.File]::WriteAllText($DaemonJson, $NewConfig, [System.Text.UTF8Encoding]::new($false))
Write-Host "[2/5] Updated daemon.json" -ForegroundColor Green

# Find Docker Desktop process
Write-Host "[3/5] Looking for Docker Desktop..." -ForegroundColor Yellow
$DockerProc = Get-Process -Name "Docker Desktop" -ErrorAction SilentlyContinue
if (-not $DockerProc) {
    $DockerProc = Get-Process -Name "com.docker.backend" -ErrorAction SilentlyContinue
}

if ($DockerProc) {
    Write-Host "       Found Docker: $($DockerProc.Name) (PID: $($DockerProc.Id))" -ForegroundColor Gray
}
else {
    Write-Host "       Docker Desktop process not found - start it manually" -ForegroundColor Yellow
}

# Stop Docker Desktop
Write-Host "[4/5] Stopping Docker Desktop..." -ForegroundColor Yellow
if ($DockerProc) {
    Stop-Process -Name "Docker Desktop" -Force -ErrorAction SilentlyContinue
    Stop-Process -Name "com.docker.backend" -Force -ErrorAction SilentlyContinue
    Stop-Process -Name "dockerd" -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 3
    Write-Host "       Stopped" -ForegroundColor Green
}
else {
    Write-Host "       Skipped (not running)" -ForegroundColor Gray
}

# Restart Docker Desktop
Write-Host "[5/5] Starting Docker Desktop..." -ForegroundColor Yellow
$DockerExe = Join-Path $env:ProgramFiles "Docker\Docker\Docker Desktop.exe"
if (-not (Test-Path $DockerExe)) {
    $DockerExe = "C:\Program Files\Docker\Docker\Docker Desktop.exe"
}

if (Test-Path $DockerExe) {
    Start-Process $DockerExe
    Write-Host "       Started: $DockerExe" -ForegroundColor Green
}
else {
    Write-Host "       [WARNING] Docker Desktop.exe not found. Please start it manually." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host " Configuration applied!" -ForegroundColor Cyan
Write-Host ""
Write-Host " After Docker Desktop is up, verify with:" -ForegroundColor White
Write-Host "   docker info | Select-String Mirrors" -ForegroundColor Gray
Write-Host ""
Write-Host " Then test pulling:" -ForegroundColor White
Write-Host "   docker pull node:20-alpine" -ForegroundColor Gray
Write-Host "============================================" -ForegroundColor Cyan
