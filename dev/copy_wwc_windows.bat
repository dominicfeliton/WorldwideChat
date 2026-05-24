@echo off
setlocal EnableExtensions

where wsl.exe >nul 2>nul
if errorlevel 1 (
    echo Error: WSL is required for dev\copy_wwc_windows.bat. Install WSL or run ./dev/copy_wwc_macos_linux.sh directly inside Linux. 1>&2
    exit /b 1
)

wsl.exe bash -lc "test -r /proc/sys/kernel/osrelease && grep -qiE '(microsoft|wsl)' /proc/sys/kernel/osrelease" >nul 2>nul
if errorlevel 1 (
    echo Error: wsl.exe is available, but the default Linux environment does not look like WSL. Configure a WSL distro or run ./dev/copy_wwc_macos_linux.sh directly inside WSL. 1>&2
    exit /b 1
)

for %%I in ("%~dp0..") do set "REPO_ROOT_WIN=%%~fI"

set "REPO_ROOT_WSL="
for /f "usebackq delims=" %%I in (`wsl.exe wslpath -a "%REPO_ROOT_WIN%" 2^>nul`) do (
    if not defined REPO_ROOT_WSL set "REPO_ROOT_WSL=%%I"
)

if not defined REPO_ROOT_WSL (
    echo Error: Could not convert "%REPO_ROOT_WIN%" to a WSL path. Move the checkout to a WSL-accessible path or run ./dev/copy_wwc_macos_linux.sh directly inside WSL. 1>&2
    exit /b 1
)

wsl.exe --cd "%REPO_ROOT_WSL%" bash -lc "test -f dev/copy_wwc_macos_linux.sh" >nul 2>nul
if errorlevel 1 (
    echo Error: WSL path "%REPO_ROOT_WSL%" does not contain dev/copy_wwc_macos_linux.sh. Move the checkout to a WSL-accessible path or run ./dev/copy_wwc_macos_linux.sh directly inside WSL. 1>&2
    exit /b 1
)

wsl.exe --cd "%REPO_ROOT_WSL%" bash ./dev/copy_wwc_macos_linux.sh %*
set "SCRIPT_EXIT=%ERRORLEVEL%"
exit /b %SCRIPT_EXIT%
