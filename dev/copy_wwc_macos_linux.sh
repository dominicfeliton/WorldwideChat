#!/bin/bash

# Determine the operating system
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS commands
    cp /Users/$USER/Documents/GitHub/WorldwideChat/spigot-target/WorldwideChat-spigot.jar /Users/$USER/Documents/spigot_wwc_test_server/plugins
    cp /Users/$USER/Documents/GitHub/WorldwideChat/spigot-target/WorldwideChat-spigot.jar /Users/$USER/Documents/magma_wwc_test_server/plugins
    cp /Users/$USER/Documents/GitHub/WorldwideChat/spigot-target/WorldwideChat-spigot.jar /Users/$USER/Documents/paper1132_wwc_test_server/plugins
    cp /Users/$USER/Documents/GitHub/WorldwideChat/paper-target/WorldwideChat-paper.jar /Users/$USER/Documents/wwc_test_server/plugins
    cp /Users/$USER/Documents/GitHub/WorldwideChat/folia-target/WorldwideChat-folia.jar /Users/$USER/Documents/folia_wwc_test_server/plugins
    cd /Users/$USER/Documents/wwc_test_server/
    ./start_mcserver.sh
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux commands
    cp /home/$USER/Documents/WorldwideChat/paper-target/WorldwideChat-paper.jar /home/$USER/Documents/wwc_test_server/plugins
    cp /home/$USER/Documents/WorldwideChat/paper-target/WorldwideChat-paper.jar /home/$USER/Documents/wwc_test_server_1165/plugins
    cp /home/$USER/Documents/WorldwideChat/spigot-target/WorldwideChat-spigot.jar /home/$USER/Documents/wwc_test_server_1132/plugins
    cp /home/$USER/Documents/WorldwideChat/spigot-target/WorldwideChat-spigot.jar /home/$USER/Documents/wwc_test_server_spigot/plugins
    cd /home/$USER/Documents/wwc_test_server_spigot/
    ./start_mcserver.sh
else
    echo "Unsupported operating system."
fi
