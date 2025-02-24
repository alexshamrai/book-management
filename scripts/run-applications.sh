#!/bin/bash

echo "Starting services..."

# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Go to project root
cd "$PROJECT_ROOT"

# Run both services
(cd book-service && mvn spring-boot:run) &
(cd order-service && mvn spring-boot:run) &

# Wait for both background processes
wait