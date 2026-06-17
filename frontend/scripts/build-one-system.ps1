$ErrorActionPreference = "Stop"

$dashboardRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$backendRoot = Resolve-Path (Join-Path $dashboardRoot "..\backend")
$staticRoot = Join-Path $backendRoot "src\main\resources\static"

Write-Host "Building Vue dashboard..."
Push-Location $dashboardRoot
npm run build
Pop-Location

Write-Host "Copying dashboard build into Spring Boot static resources..."
New-Item -ItemType Directory -Force -Path $staticRoot | Out-Null

$resolvedStatic = (Resolve-Path $staticRoot).Path
$resolvedBackend = (Resolve-Path $backendRoot).Path
if (-not $resolvedStatic.StartsWith($resolvedBackend)) {
  throw "Static folder check failed: $resolvedStatic is outside $resolvedBackend"
}

Get-ChildItem -LiteralPath $staticRoot -Force | Remove-Item -Recurse -Force
Copy-Item -Path (Join-Path $dashboardRoot "dist\*") -Destination $staticRoot -Recurse -Force

Write-Host "Building Spring Boot package..."
Push-Location $backendRoot
.\mvnw.cmd clean package -DskipTests
Pop-Location

Write-Host "Done. Run this single system with:"
Write-Host "cd $backendRoot"
Write-Host ".\mvnw.cmd spring-boot:run"
