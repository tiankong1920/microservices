# ============================================================================
# SSL Certificate Verification Script (LS-004)
# ============================================================================
# Purpose: Verify production environment SSL certificate validity
# Executor: Operations Team
# ============================================================================

param(
    [string]$Domain = "inventory.example.com",
    [int]$Port = 443,
    [int]$WarningThresholdDays = 30,
    [int]$CriticalThresholdDays = 7
)

$ErrorActionPreference = "Stop"

function Get-CertificateInfo {
    param(
        [string]$Hostname,
        [int]$Port
    )

    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $tcpClient.Connect($Hostname, $Port)
        $tcpClient.Close()

        $cert = New-Object System.Security.Cryptography.X509Certificates.X509Certificate2
        $cert.Import(((Get-NetIPAddress -InterfaceIndex (Get-NetAdapter | Where-Object { $_.Status -eq "Up" } | Select-Object -First 1).InterfaceIndex -AddressFamily IPv4 -ErrorAction SilentlyContinue).IPAddress), $Port)

        $sslStream = New-Object System.Net.Security.SslStream($tcpClient.GetStream())
        $sslStream.AuthenticateAsClient($Hostname)
        $remoteCert = $sslStream.RemoteCertificate
        $sslStream.Close()

        return @{
            Subject = $cert.Subject
            Issuer = $cert.Issuer
            Thumbprint = $cert.Thumbprint
            NotBefore = $cert.NotBefore
            NotAfter = $cert.NotAfter
            DaysRemaining = ($cert.NotAfter - (Get-Date)).Days
            SerialNumber = $cert.SerialNumber
        }
    }
    catch {
        Write-Error "Failed to connect to ${Hostname}:${Port} - $_"
        return $null
    }
}

function Test-SslCertificate {
    param(
        [string]$Hostname,
        [int]$Port,
        [int]$WarningThreshold,
        [int]$CriticalThreshold
    )

    $certInfo = Get-CertificateInfo -Hostname $Hostname -Port $Port

    if ($null -eq $certInfo) {
        return @{
            Status = "ERROR"
            Message = "Failed to retrieve certificate"
            DaysRemaining = 0
        }
    }

    $status = "OK"
    $message = ""

    if ($certInfo.DaysRemaining -lt 0) {
        $status = "EXPIRED"
        $message = "Certificate has EXPIRED on $($certInfo.NotAfter.ToString('yyyy-MM-dd'))"
    }
    elseif ($certInfo.DaysRemaining -lt $CriticalThreshold) {
        $status = "CRITICAL"
        $message = "Certificate expires in $($certInfo.DaysRemaining) days (Critical threshold: $CriticalThreshold days)"
    }
    elseif ($certInfo.DaysRemaining -lt $WarningThreshold) {
        $status = "WARNING"
        $message = "Certificate expires in $($certInfo.DaysRemaining) days (Warning threshold: $WarningThreshold days)"
    }
    else {
        $status = "OK"
        $message = "Certificate is valid for $($certInfo.DaysRemaining) more days"
    }

    return @{
        Status = $status
        Message = $message
        DaysRemaining = $certInfo.DaysRemaining
        ExpiryDate = $certInfo.NotAfter
        Subject = $certInfo.Subject
        Issuer = $certInfo.Issuer
        Thumbprint = $certInfo.Thumbprint
    }
}

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "SSL Certificate Verification Script (LS-004)" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

$result = Test-SslCertificate -Hostname $Domain -Port $Port -WarningThreshold $WarningThresholdDays -CriticalThreshold $CriticalThresholdDays

Write-Host "Domain: $Domain`:$Port" -ForegroundColor Yellow
Write-Host "Status: $($result.Status)" -ForegroundColor $(switch ($result.Status) { "OK" { "Green" } "WARNING" { "Yellow" } "CRITICAL" { "Red" } default { "Red" } })
Write-Host "Message: $($result.Message)"
Write-Host ""

if ($result.Status -ne "ERROR" -and $result.Status -ne "EXPIRED") {
    Write-Host "Certificate Details:" -ForegroundColor Yellow
    Write-Host "  Subject: $($result.Subject)"
    Write-Host "  Issuer: $($result.Issuer)"
    Write-Host "  Thumbprint: $($result.Thumbprint)"
    Write-Host "  Expiry Date: $($result.ExpiryDate)"
    Write-Host "  Days Remaining: $($result.DaysRemaining)"
}

Write-Host ""
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "Recommended Actions:" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan

switch ($result.Status) {
    "EXPIRED" {
        Write-Host "URGENT: Certificate has EXPIRED!" -ForegroundColor Red
        Write-Host "   1. Renew certificate immediately"
        Write-Host "   2. Update certificate in load balancer"
        Write-Host "   3. Verify all services are accessible"
    }
    "CRITICAL" {
        Write-Host "CRITICAL: Certificate expires soon!" -ForegroundColor Red
        Write-Host "   1. Initiate certificate renewal process"
        Write-Host "   2. Plan maintenance window for update"
        Write-Host "   3. Schedule renewal within 7 days"
    }
    "WARNING" {
        Write-Host "WARNING: Certificate expiring soon" -ForegroundColor Yellow
        Write-Host "   1. Start certificate renewal planning"
        Write-Host "   2. Schedule renewal within 30 days"
    }
    "OK" {
        Write-Host "SUCCESS: Certificate is valid" -ForegroundColor Green
        Write-Host "   1. Continue regular monitoring"
        Write-Host "   2. Set reminder for renewal at 90 days"
    }
}

Write-Host ""

exit $(switch ($result.Status) { "OK" { 0 } "WARNING" { 1 } "CRITICAL" { 2 } "EXPIRED" { 3 } default { 4 } })
