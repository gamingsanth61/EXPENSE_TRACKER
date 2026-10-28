@echo off
title MySQL Database Setup
cd /d "%~dp0"
echo =======================================================
echo   Expense Tracker - Automatic MySQL Setup
echo =======================================================
echo.
echo Your MySQL Server 8.0 is running, but 'expense_user'
echo has not been created in your MySQL yet.
echo.
echo Please enter your MySQL 'root' password:
set /p ROOT_PASS=Root Password: 

echo.
echo [1/2] Creating database 'expense_tracker_db' and user 'expense_user'...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p%ROOT_PASS% -e "CREATE DATABASE IF NOT EXISTS expense_tracker_db; CREATE USER IF NOT EXISTS 'expense_user'@'localhost' IDENTIFIED BY 'password123'; GRANT ALL PRIVILEGES ON expense_tracker_db.* TO 'expense_user'@'localhost'; FLUSH PRIVILEGES;"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Incorrect root password or MySQL could not connect.
    echo Please verify your root password and try again.
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Importing tables and categories from database.sql...
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u expense_user -ppassword123 expense_tracker_db < "%~dp0database.sql"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Could not import database.sql.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo =======================================================
echo   SUCCESS! Database and categories created successfully.
echo   You can now launch the app using 'run.bat'.
echo =======================================================
echo.
pause
