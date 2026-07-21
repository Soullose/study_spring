#!/bin/bash

# Spring Boot Application Start Script (Linux/Mac)
# 瘦 jar + lib/ 目录启动方式：start-1.0-SNAPSHOT.jar + start/target/lib/*

# 切换到脚本所在目录（项目根目录），保证相对路径可用
cd "$(dirname "$0")" || exit 1

JAR_FILE="start/target/start-1.0-SNAPSHOT.jar"
LIB_DIR="start/target/lib"
MAIN_CLASS="com.wsf.StartApplication"

echo "========================================"
echo "   Spring Boot Application Start Script"
echo "========================================"
echo

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "ERROR: JAR file not found: $JAR_FILE"
    echo "Please run: mvn clean package first"
    echo
    exit 1
fi

# Check if dependency directory exists
if [ ! -d "$LIB_DIR" ]; then
    echo "ERROR: Dependency directory not found: $LIB_DIR"
    echo "Please run: mvn clean package first"
    echo
    exit 1
fi

# JVM options, 可用环境变量覆盖： JAVA_OPTS="-Xmx1g" ./start.sh
JAVA_OPTS="${JAVA_OPTS:--Xmx512m -Xms256m}"

# Build classpath: thin JAR + all jars under lib/
CLASSPATH="$JAR_FILE"
for jar in "$LIB_DIR"/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done

echo "Starting Spring Boot Application..."
echo "JAR File:      $JAR_FILE"
echo "Dependencies:  $LIB_DIR/"
echo "JVM Options:   $JAVA_OPTS"
echo

# Start application - using thin JAR and external dependencies
# shellcheck disable=SC2086
java $JAVA_OPTS -cp "$CLASSPATH" "$MAIN_CLASS" "$@"

echo
echo "Application stopped."
