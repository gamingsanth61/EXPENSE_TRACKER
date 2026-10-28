@echo off
title Push to GitHub
cd /d "%~dp0"
echo ========================================================
echo   Pushing Expense Tracker to GitHub (gamingsanth61)
echo ========================================================
echo.
git push -u origin main
echo.
if %ERRORLEVEL% EQU 0 (
    echo ========================================================
    echo   SUCCESS! Your project has been pushed to GitHub.
    echo   View it at: https://github.com/gamingsanth61/EXPENSE_TRACKER
    echo ========================================================
) else (
    echo ========================================================
    echo   [NOTICE] Authentication required or push failed.
    echo   If Git opens a browser window, please click "Sign In"
    echo   to authenticate with your GitHub account.
    echo ========================================================
)
echo.
pause
