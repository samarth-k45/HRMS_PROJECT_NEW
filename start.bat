@echo off
echo.
echo ==================================================
echo  HRMS - Build and Start (No Maven Required)
echo ==================================================
echo.

REM Run the PowerShell setup/build/start script
powershell -ExecutionPolicy Bypass -File "%~dp0setup.ps1"

pause
