@echo off
title VaxiCare Backend Server & Database Engine
color 0b
echo =======================================================================
echo          Starting VaxiCare Backend Server & Database Engine...
echo =======================================================================
echo.
powershell.exe -ExecutionPolicy Bypass -NoProfile -File "%~dp0start-server.ps1"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Server stopped with error.
    pause
)
