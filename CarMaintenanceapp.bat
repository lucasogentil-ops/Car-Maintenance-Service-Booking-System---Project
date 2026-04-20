@echo off
setlocal
cd /d "%~dp0"

set "LATEST_JAR="
for /f "delims=" %%F in ('dir /b /o-d "CarMaintenanceApp*.jar" 2^>nul') do (
  set "LATEST_JAR=%%F"
  goto :run
)

if not defined LATEST_JAR (
  echo No CarMaintenanceApp*.jar found.
  echo Run build-jar.ps1 first.
  exit /b 1
)

:run
echo Running %LATEST_JAR%
java -jar "%LATEST_JAR%"
endlocal
