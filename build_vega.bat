@echo off
REM =====================================================
REM VEGA Trader - One-click JNI Build
REM =====================================================

REM --- Path to Visual Studio Dev Command Prompt ---
REM Using the x86 path as commonly found on 64-bit Windows for VS2022 BuildTools/Community
SET VS_DEV_CMD="C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\Common7\Tools\VsDevCmd.bat"
REM Fallback for Community Edition if BuildTools path fails (optional, but good for robustness)
IF NOT EXIST %VS_DEV_CMD% (
    SET VS_DEV_CMD="C:\Program Files\Microsoft Visual Studio\2022\Community\Common7\Tools\VsDevCmd.bat"
)
REM Fallback for Enterprise
IF NOT EXIST %VS_DEV_CMD% (
    SET VS_DEV_CMD="C:\Program Files\Microsoft Visual Studio\2022\Enterprise\Common7\Tools\VsDevCmd.bat"
)

IF NOT EXIST %VS_DEV_CMD% (
    echo [ERROR] VsDevCmd.bat not found. Checked standard locations.
    echo Please edit build_vega.bat to point to your VS 2022 installation.
    exit /b 1
)

echo [INFO] Loading MSVC Environment from %VS_DEV_CMD%...
call %VS_DEV_CMD% -arch=x64 >nul

REM --- Confirm cl.exe is available ---
where cl >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo [ERROR] cl.exe not found in PATH after running VsDevCmd.bat
    exit /b 1
)

REM --- Run the main VEGA JNI compilation batch ---
echo [INFO] Starting Compilation...
cd backend\cpp\market_data_core
call compile_jni_msvc.bat
cd ..\..\..
