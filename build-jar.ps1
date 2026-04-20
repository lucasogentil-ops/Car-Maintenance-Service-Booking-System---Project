param(
    [string]$AppName = "CarMaintenanceApp",
    [string]$MainClass = "Main"
)

$ErrorActionPreference = "Stop"
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Push-Location $projectDir
try {
    $sqliteJar = Get-ChildItem -Path "lib" -File -Filter "sqlite-jdbc*.jar" -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if (-not $sqliteJar) {
        throw "Missing SQLite driver. Put sqlite-jdbc jar in ./lib (example: lib/sqlite-jdbc-3.46.0.0.jar)."
    }

    if (Test-Path "out") {
        Remove-Item -Recurse -Force "out"
    }
    New-Item -ItemType Directory -Path "out" | Out-Null

    javac -d out *.java dao/*.java db/*.java ui/*.java Model/*.java
    if ($LASTEXITCODE -ne 0) {
        throw "Compilation failed. Fix compile errors first."
    }

    $manifest = @"
Main-Class: $MainClass
Class-Path: lib/$($sqliteJar.Name)

"@
    Set-Content -Path "manifest.mf" -Value $manifest

    $jarFile = "$AppName.jar"
    if (Test-Path $jarFile) {
        try {
            Remove-Item -Force $jarFile
        }
        catch {
            $suffix = Get-Date -Format "yyyyMMdd-HHmmss"
            $jarFile = "$AppName-$suffix.jar"
            Write-Host "Target jar is locked, building as: $jarFile"
        }
    }

    jar --create --file $jarFile --manifest "manifest.mf" -C out .
    if ($LASTEXITCODE -ne 0) {
        throw "Jar creation failed."
    }

    Write-Host "Built $jarFile"
    Write-Host "Run it with: java -jar $jarFile"
}
finally {
    Pop-Location
}
