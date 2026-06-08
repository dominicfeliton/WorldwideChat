#!/bin/bash
set -e

cd "$(dirname "$0")"

compose() {
if command -v docker-compose &> /dev/null; then
    docker-compose "$@"
elif command -v docker &> /dev/null; then
    docker compose "$@"
else
    echo "docker-compose is not installed."
    exit 1
fi
}

wait_for_service() {
    service="$1"
    echo "Waiting for ${service}..."
    for _ in $(seq 1 60); do
        container_id="$(compose ps -q "$service")"
        if [ -n "$container_id" ]; then
            status="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$container_id" 2>/dev/null || true)"
            if [ "$status" = "healthy" ]; then
                echo "${service} is healthy."
                return 0
            fi
        fi
        sleep 2
    done

    echo "${service} did not become healthy in time."
    compose ps
    exit 1
}

compose up -d
wait_for_service mysql
wait_for_service mongo
wait_for_service postgres
