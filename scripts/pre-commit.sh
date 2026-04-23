#!/bin/sh
set -e

echo "Running Spotless apply..."
mvn spotless:apply

echo "Running Checkstyle..."
mvn checkstyle:check -DskipTests
