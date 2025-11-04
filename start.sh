#!/bin/bash

# Spring Boot Application Start Script (Linux/Mac)

echo "========================================"
echo "   Spring Boot Application Start Script"
echo "========================================"
echo

# Check if JAR file exists
if [ ! -f "start/target/start-1.0-SNAPSHOT.jar" ]; then
    echo "ERROR: JAR file not found!"
    echo "Please run: mvn clean package first"
    echo
    exit 1
fi

# Check if dependency directory exists
if [ ! -d "start/target/lib" ]; then
    echo "ERROR: Dependency directory not found!"
    echo "Please run: mvn clean package first"
    echo
    exit 1
fi

# Set JVM options
JAVA_OPTS="-Xmx512m -Xms256m"

# Build classpath
CLASSPATH="start/target/start-1.0-SNAPSHOT.jar"
for jar in start/target/lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done

echo "Starting Spring Boot Application..."
echo "JAR File: start/target/start-1.0-SNAPSHOT.jar"
echo "Dependencies: start/target/lib/"
echo "JVM Options: $JAVA_OPTS"
echo

# Start application - using thin JAR and external dependencies
java $JAVA_OPTS -cp "$CLASSPATH" com.wsf.StartApplication "$@"

echo
echo "Application stopped."