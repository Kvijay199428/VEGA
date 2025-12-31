@echo off
REM ================================================
REM VEGA Trader - Java Backend Starter Script
REM ================================================
REM This script starts the Spring Boot Java backend
REM Server Port: 28020
REM ================================================

echo.
echo ====================================================
echo   VEGA Trader - Starting Java Backend Server
echo   Port: 28020
echo ====================================================
echo.

REM Navigate to the vega-trader Java project directory
cd /d "%~dp0java\vega-trader"

REM Check if pre-compiled JAR exists (faster startup)
if exist "target\vega-trader-1.0.0.jar" (
    echo Starting from pre-compiled JAR...
    java -jar target\vega-trader-1.0.0.jar
) else (
    REM Fall back to Maven if JAR doesn't exist
    echo JAR not found. Building and running with Maven...
    if exist "mvnw.cmd" (
        echo Using Maven Wrapper...
        call mvnw.cmd spring-boot:run
    ) else (
        echo Using System Maven...
        call mvn spring-boot:run
    )
)

REM If command fails, pause to show error
if %ERRORLEVEL% neq 0 (
    echo.
    echo ====================================================
    echo   ERROR: Failed to start Java backend!
    echo   Ensure Java 17+ is installed and configured.
    echo ====================================================
    pause
)
