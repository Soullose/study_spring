@echo off
setlocal enabledelayedexpansion
title Spring Boot Application

REM 切换到脚本所在目录（项目根目录），保证相对路径可用
cd /d "%~dp0"

set "JAR_FILE=start\target\start-1.0-SNAPSHOT.jar"
set "LIB_DIR=start\target\lib"
set "MAIN_CLASS=com.wsf.StartApplication"

echo ========================================
echo    Spring Boot Application Start Script
echo ========================================
echo.

if not exist "%JAR_FILE%" (
    echo ERROR: JAR file not found: %JAR_FILE%
    echo Please run: mvn clean package first
    echo.
    pause
    exit /b 1
)

if not exist "%LIB_DIR%" (
    echo ERROR: Dependency directory not found: %LIB_DIR%
    echo Please run: mvn clean package first
    echo.
    pause
    exit /b 1
)

REM JVM options，可在运行前用 set JAVA_OPTS=... 覆盖
if not defined JAVA_OPTS set "JAVA_OPTS=-Xmx512m -Xms256m"

REM Build classpath using wildcard: thin JAR + lib\*
set "CLASSPATH=%JAR_FILE%;%LIB_DIR%\*"

echo Starting Spring Boot Application...
echo JAR File:      %JAR_FILE%
echo Dependencies:  %LIB_DIR%\
echo JVM Options:   %JAVA_OPTS%
echo.

REM Start application using the classpath
java %JAVA_OPTS% -cp "%CLASSPATH%" %MAIN_CLASS% %*

echo.
echo Application stopped.
pause
