@echo off
echo ===========================================
echo Compiling C++ Market Data Core (JNI)
echo ===========================================

REM Check for G++
where g++ >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] g++ not found in PATH.
    echo Please install MinGW or MSVC and ensure 'g++' is accessible.
    exit /b 1
)

echo [1/3] Generating JNI Headers (javac -h)...
cd "..\java\vega-trader\src\main\java"
javac -h "..\..\..\..\..\..\cpp\market_data_core" "com\vegatrader\market\journal\NativeJournal.java"
if %errorlevel% neq 0 (
    echo [ERROR] Failed to generate JNI headers.
    exit /b 1
)
cd "..\..\..\..\..\..\cpp\market_data_core"

echo [2/3] Compiling C++ Library...
REM Replace with your JDK Path if different
set JAVA_HOME_INC="C:\Program Files\Java\jdk-17\include"
set JAVA_HOME_INC_WIN="C:\Program Files\Java\jdk-17\include\win32"

g++ -shared -o market_data_core.dll native_lib.cpp ^
    -I%JAVA_HOME_INC% ^
    -I%JAVA_HOME_INC_WIN% ^
    -I. ^
    -static-libgcc -static-libstdc++ ^
    -Wl,--add-stdcall-alias

if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    exit /b 1
)

echo [3/3] Success! market_data_core.dll created.
dir market_data_core.dll
pause
