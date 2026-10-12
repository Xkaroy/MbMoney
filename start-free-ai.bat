@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\start-free-ai.ps1" %*
if errorlevel 1 (
    echo AI startup failed. See the message above.
)
pause
