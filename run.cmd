@echo off
REM Track Draw Application Launcher
REM This script runs the pre-built JAR file

echo ========================================
echo Track Draw Application Launcher
echo ========================================
echo.

REM Check if Java is installed
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 11 or higher
    pause
    exit /b 1
)

REM Set JAR file path (standard Maven output location)
set JAR_FILE=target\track-draw-1.0-SNAPSHOT.jar

REM Check if JAR file exists
if not exist "%JAR_FILE%" (
    echo ERROR: JAR file not found: %JAR_FILE%
    echo.
    echo Please build the application first using:
    echo   mvn clean package
    echo.
    echo Or if you have a different JAR file, please update the JAR_FILE variable in this script.
    pause
    exit /b 1
)

echo Found JAR file: %JAR_FILE%
echo Starting application...
echo.

REM Run the application
java -jar "%JAR_FILE%"

REM If the application exits, pause to see any error messages
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with error code %ERRORLEVEL%
    pause
)
