param(
    [string]$Version = "3.46.1.3"
)

$ErrorActionPreference = "Stop"
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$libDir = Join-Path $projectDir "lib"
$jarName = "sqlite-jdbc-$Version.jar"
$target = Join-Path $libDir $jarName
$url = "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/$Version/$jarName"

if (-not (Test-Path $libDir)) {
    New-Item -ItemType Directory -Path $libDir | Out-Null
}

if (Test-Path $target) {
    Write-Host "Already exists: $target"
    exit 0
}

Write-Host "Downloading: $url"
Invoke-WebRequest -Uri $url -OutFile $target

if (-not (Test-Path $target)) {
    throw "Download failed. File not found after download attempt."
}

Write-Host "Downloaded: $target"
Write-Host "Next step: run .\\build-jar.ps1"
