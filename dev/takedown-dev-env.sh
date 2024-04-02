#!/bin/bash

cd "$(dirname "$0")"

if command -v docker-compose &> /dev/null; then
    docker-compose down -v
elif command -v docker &> /dev/null; then
    docker compose down -v
else
    echo "docker-compose is not installed."
fi