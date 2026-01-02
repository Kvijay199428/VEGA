@echo off
echo ===========================================
echo Compiling C++ Market Data Core (JNI - MSVC)
echo ===========================================

REM Ensure we are in the script directory
pushd "%~dp0"
set "SCRIPT_DIR=%CD%"

REM Check for cl.exe
where cl >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] cl.exe not found.
    echo Please run this from a Developer Command Prompt for VS.
    popd
    exit /b 1
)

REM Define Java Source Root relative to this script
REM Structure: backend/cpp/market_data_core -> backend/java/vega-trader/src/main/java
set "JAVA_SRC_DIR=%SCRIPT_DIR%\..\..\java\vega-trader\src\main\java"
set "TARGET_JAVA_FILE=%JAVA_SRC_DIR%\com\vegatrader\market\journal\NativeJournal.java"

echo [INFO] Script Dir: %SCRIPT_DIR%
echo [INFO] Source File: %TARGET_JAVA_FILE%

if not exist "%TARGET_JAVA_FILE%" (
    echo [ERROR] File not found: %TARGET_JAVA_FILE%
    popd
    exit /b 1
)

echo [1/3] Generating JNI Headers (javac -h)...
javac -h . "%TARGET_JAVA_FILE%"
if %errorlevel% neq 0 (
    echo [ERROR] Failed to generate JNI headers.
    popd
    exit /b 1
)

echo [2/3] Compiling C++ Library...
if not defined JAVA_HOME (
    echo [WARNING] JAVA_HOME not set. Assuming default C:\Program Files\Java\jdk-17
    set "JAVA_HOME=C:\Program Files\Java\jdk-17"
)

set "JAVA_INC=%JAVA_HOME%\include"
set "JAVA_INC_WIN=%JAVA_HOME%\include\win32"

echo Using JNI Includes: "%JAVA_INC%"

cl /LD /EHsc /std:c++17 native_lib.cpp ^
   /I"%JAVA_INC%" ^
   /I"%JAVA_INC_WIN%" ^
   /I. ^
   /Fe:market_data_core.dll

if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    popd
    exit /b 1
)

echo [3/3] Success! market_data_core.dll created.
dir market_data_core.dll
popd
