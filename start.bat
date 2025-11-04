@echo off
setlocal enabledelayedexpansion
title Spring Boot Application

echo ========================================
echo    Spring Boot Application Start Script
echo ========================================
echo.

if not exist "start\target\start-1.0-SNAPSHOT.jar" (
    echo ERROR: JAR file not found!
    echo Please run: mvn clean package first
    echo.
    pause
    exit /b 1
)

if not exist "start\target\lib" (
    echo ERROR: Dependency directory not found!
    echo Please run: mvn clean package first
    echo.
    pause
    exit /b 1
)

set JAVA_OPTS=-Xmx512m -Xms256m 

REM Build classpath using wildcard - this is the key fix
set CLASSPATH=start\target\start-1.0-SNAPSHOT.jar;start\target\lib\*

echo Starting Spring Boot Application...
echo JAR File: start\target\start-1.0-SNAPSHOT.jar
echo Dependencies: start\target\lib\
echo JVM Options: %JAVA_OPTS%
echo.

REM Start application using the classpath
java %JAVA_OPTS% -cp "%CLASSPATH%" com.wsf.StartApplication %*

echo.
echo Application stopped.
pause