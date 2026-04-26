@echo off
setlocal EnableExtensions

set "VERSION_PIN=26.1"
set "PROJECT_ROOT=C:\Users\domin\Desktop\Work\Minecraft - Java Development\WorldwideChat\WorldwideChat"
set "SERVER_ROOT=C:\Users\domin\Desktop\Work\Minecraft - Java Development\MC Servers"
set "PAPER_JAR=%PROJECT_ROOT%\paper-target\WorldwideChat-paper.jar"

for %%S in (
    personal_wwc_test_server
    personal_wwc_test_server_188
    personal_wwc_test_server_194
    personal_wwc_test_server_1102
    personal_wwc_test_server_1112
    personal_wwc_test_server_1122
    personal_wwc_test_server_1132
) do (
    call :copy_plugin "%PAPER_JAR%" "%SERVER_ROOT%\%%~S\plugins" || exit /b 1
)

call :start_server "%SERVER_ROOT%\personal_wwc_test_server" "%VERSION_PIN%"
exit /b %ERRORLEVEL%

:copy_plugin
set "source_jar=%~1"
set "plugins_dir=%~2"

if not exist "%source_jar%" (
    echo Missing build artifact: "%source_jar%" 1>&2
    exit /b 1
)

if not exist "%plugins_dir%\" (
    echo Missing plugins directory: "%plugins_dir%" 1>&2
    exit /b 1
)

copy /Y "%source_jar%" "%plugins_dir%\" >nul
exit /b %ERRORLEVEL%

:start_server
set "server_dir=%~1"
set "server_version=%~2"

if not exist "%server_dir%\run_mcserver.bat" (
    echo Missing server script: "%server_dir%\run_mcserver.bat" 1>&2
    exit /b 1
)

pushd "%server_dir%" || exit /b 1
call "%server_dir%\run_mcserver.bat" "%server_version%"
set "server_exit=%ERRORLEVEL%"
popd
exit /b %server_exit%
