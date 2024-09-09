#!/bin/bash

cd "$(dirname "$0")"

if command -v docker-compose &> /dev/null; then
    docker-compose up -d
elif command -v docker &> /dev/null; then
    docker compose up -d
else
    echo "docker-compose is not installed."
fi