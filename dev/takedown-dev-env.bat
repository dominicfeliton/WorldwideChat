@echo off
cd /d %~dp0

docker-compose -v >nul 2>&1
if %errorlevel% equ 0 (
    docker-compose down
) else (
    echo docker-compose is not installed.
)