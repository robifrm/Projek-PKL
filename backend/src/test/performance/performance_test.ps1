# ============================================================
# VNet Backend - Performance Test Script
# ============================================================
# Cara pakai:
#   1. Pastikan backend berjalan di http://localhost:8080
#   2. Buka PowerShell dan jalankan:
#      .\performance_test.ps1
#   3. Opsional: ubah $BaseUrl atau $LoginEmail/$LoginPassword
#      di bagian CONFIGURATION di bawah
# ============================================================

param(
    [string]$BaseUrl      = "http://localhost:8080",
    [string]$LoginEmail   = "admin@victorynetwork.id",   # sesuaikan
    [string]$LoginPassword = "admin123",                 # sesuaikan
    [int]$Iterations      = 20
)

$ErrorActionPreference = "SilentlyContinue"

# --- ANSI colors -----------------------------------------
function Green($msg)  { Write-Host $msg -ForegroundColor Green  }
function Yellow($msg) { Write-Host $msg -ForegroundColor Yellow }
function Red($msg)    { Write-Host $msg -ForegroundColor Red    }
function Cyan($msg)   { Write-Host $msg -ForegroundColor Cyan   }

# --- Helper: ukur rata-rata response time ----------------
function Measure-Endpoint {
    param(
        [string]$Label,
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [string]$Body = $null,
        [int]$N = $Iterations,
        [int]$ThresholdMs = 500
    )

    $times = @()
    $errors = 0

    for ($i = 1; $i -le $N; $i++) {
        try {
            $sw = [System.Diagnostics.Stopwatch]::StartNew()

            $params = @{
                Uri             = $Url
                Method          = $Method
                Headers         = $Headers
                UseBasicParsing = $true
            }
            if ($Body) {
                $params["Body"]        = $Body
                $params["ContentType"] = "application/json"
            }

            Invoke-WebRequest @params | Out-Null
            $sw.Stop()
            $times += $sw.ElapsedMilliseconds
        } catch {
            $sw.Stop()
            $times += $sw.ElapsedMilliseconds
            $errors++
        }
    }

    $avg  = [math]::Round(($times | Measure-Object -Average).Average, 1)
    $min  = ($times | Measure-Object -Minimum).Minimum
    $max  = ($times | Measure-Object -Maximum).Maximum
    $p95  = ($times | Sort-Object)[[int]([math]::Ceiling($N * 0.95)) - 1]

    $status = if ($avg -le $ThresholdMs) { "PASS" } else { "FAIL" }
    $color  = if ($avg -le $ThresholdMs) { "Green" } else { "Red" }

    Write-Host ""
    Write-Host "  [$status] $Label" -ForegroundColor $color
    Write-Host "    Avg: ${avg}ms | Min: ${min}ms | Max: ${max}ms | P95: ${p95}ms | Errors: $errors/$N"
    Write-Host "    Threshold: ${ThresholdMs}ms" -ForegroundColor Gray

    return [PSCustomObject]@{
        Label       = $Label
        Avg         = $avg
        Min         = $min
        Max         = $max
        P95         = $p95
        Errors      = $errors
        Threshold   = $ThresholdMs
        Passed      = ($avg -le $ThresholdMs)
    }
}

# ----------------------------------------------------------
Cyan "+--------------------------------------------------+"
Cyan "|     VNet Backend - Performance Test Suite        |"
Cyan "|     Target: $BaseUrl"
Cyan "|     Iterations per endpoint: $Iterations"
Cyan "+--------------------------------------------------+"

# --- STEP 1: Health check ---------------------------------
Write-Host ""
Yellow "[1/5] Checking backend availability..."
try {
    $check = Invoke-WebRequest -Uri "$BaseUrl/api/auth/captcha" -UseBasicParsing -TimeoutSec 5
    Green "  [OK] Backend is UP (HTTP $($check.StatusCode))"
} catch {
    Red "  [ERROR] Backend is DOWN or unreachable at $BaseUrl"
    Red "     Pastikan backend sudah berjalan dengan: .\mvnw spring-boot:run"
    exit 1
}

# --- STEP 2: Login untuk mendapatkan JWT token -----------
Write-Host ""
Yellow "[2/5] Authenticating to get JWT token..."
$loginBody = @{
    email    = $LoginEmail
    password = $LoginPassword
} | ConvertTo-Json

try {
    $loginResp = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResp.token
    Green "  [OK] Login successful - token obtained"
} catch {
    Yellow "  [WARN] Login failed (email/password mungkin salah atau user belum ada)"
    Yellow "     Menjalankan test publik saja (tanpa JWT)..."
    $token = $null
}

$authHeaders = @{}
if ($token) { $authHeaders["Authorization"] = "Bearer $token" }

# --- STEP 3: Performance Tests ---------------------------
Write-Host ""
Yellow "[3/5] Running performance benchmarks ($Iterations iterations each)..."
$results = @()

# Public endpoints
$results += Measure-Endpoint -Label "GET /api/auth/captcha (public)" -Method GET -Url "$BaseUrl/api/auth/captcha" -ThresholdMs 200
$results += Measure-Endpoint -Label "POST /api/auth/login (auth)" -Method POST -Url "$BaseUrl/api/auth/login" -Body $loginBody -ThresholdMs 500

# Authenticated endpoints (hanya jika token ada)
if ($token) {
    $results += Measure-Endpoint -Label "GET /api/customers (list all)" -Method GET -Url "$BaseUrl/api/customers" -Headers $authHeaders -ThresholdMs 1000
    $results += Measure-Endpoint -Label "GET /api/customers/page?page=0&size=10" -Method GET -Url "$BaseUrl/api/customers/page?page=0&size=10" -Headers $authHeaders -ThresholdMs 500
    $results += Measure-Endpoint -Label "GET /api/dashboard/summary (analytics summary)" -Method GET -Url "$BaseUrl/api/dashboard/summary" -Headers $authHeaders -ThresholdMs 1000
    $results += Measure-Endpoint -Label "GET /api/packages (package list)" -Method GET -Url "$BaseUrl/api/packages" -Headers $authHeaders -ThresholdMs 300
    $results += Measure-Endpoint -Label "GET /api/addresses/insights (analytics heavy)" -Method GET -Url "$BaseUrl/api/addresses/insights" -Headers $authHeaders -ThresholdMs 2000
} else {
    Yellow "  [SKIP] Skipping authenticated endpoints (no token)"
}

# --- STEP 4: Summary -------------------------------------
Write-Host ""
Yellow "[4/5] Performance Test Summary"
Write-Host "-----------------------------------------------------" -ForegroundColor DarkGray

$passed = ($results | Where-Object { $_.Passed }).Count
$failed = ($results | Where-Object { -not $_.Passed }).Count
$total  = $results.Count

Write-Host ""
Write-Host "  Results: $passed/$total endpoints passed threshold"

foreach ($r in $results) {
    $icon = if ($r.Passed) { "[OK]" } else { "[FAIL]" }
    $col  = if ($r.Passed) { "Green" } else { "Red" }
    Write-Host "  $icon $($r.Label) - avg $($r.Avg)ms (threshold: $($r.Threshold)ms)" -ForegroundColor $col
}

# --- STEP 5: Security Notes ------------------------------
Write-Host ""
Yellow "[5/5] Quick Security Checks"

# Check: endpoint tanpa token return 401
try {
    Invoke-WebRequest -Uri "$BaseUrl/api/customers" -UseBasicParsing | Out-Null
    Red "  [FAIL] SEC: /api/customers accessible WITHOUT token - SECURITY ISSUE!"
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Green "  [OK] SEC: /api/customers returns 401 without token - OK"
    } else {
        Yellow "  [WARN] SEC: /api/customers returned unexpected status without token"
    }
}

# Check: endpoint dengan token palsu return 401
try {
    Invoke-WebRequest -Uri "$BaseUrl/api/customers" -Headers @{ Authorization = "Bearer fake.jwt.token" } -UseBasicParsing | Out-Null
    Red "  [FAIL] SEC: /api/customers accepts FAKE token - CRITICAL SECURITY ISSUE!"
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 401) {
        Green "  [OK] SEC: Fake JWT rejected with 401 - OK"
    }
}

# --- Final result -----------------------------------------
Write-Host ""
if ($failed -eq 0) {
    Green "+--------------------------------------------+"
    Green "|  SUCCESS: ALL PERFORMANCE TESTS PASSED!    |"
    Green "+--------------------------------------------+"
} else {
    Red "+--------------------------------------------+"
    Red "|  WARN: $failed ENDPOINT(S) OVER THRESHOLD  |"
    Red "|  Review slow endpoints above               |"
    Red "+--------------------------------------------+"
}

Write-Host ""
Write-Host "  Catatan: Hasil dapat bervariasi tergantung data di DB dan hardware."
Write-Host "  Jalankan ulang setelah warm-up untuk hasil lebih akurat."
Write-Host ""
