@echo off
title Expense Tracker
cd /d "%~dp0"

echo Starting Expense Tracker...
call mvn javafx:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Something went wrong while running the application.
    pause
)
