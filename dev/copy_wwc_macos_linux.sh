#!/usr/bin/env bash
set -euo pipefail

VERSION_PIN="26.1.2"

copy_plugin() {
    local source_jar="$1"
    local plugins_dir="$2"

    if [[ ! -f "$source_jar" ]]; then
        printf 'Missing build artifact: %s\n' "$source_jar" >&2
        exit 1
    fi

    if [[ ! -d "$plugins_dir" ]]; then
        printf 'Missing plugins directory: %s\n' "$plugins_dir" >&2
        exit 1
    fi

    cp "$source_jar" "$plugins_dir/"
}

copy_to_all() {
    local source_jar="$1"
    shift

    local plugins_dir
    for plugins_dir in "$@"; do
        copy_plugin "$source_jar" "$plugins_dir"
    done
}

start_server() {
    local server_dir="$1"
    shift

    if [[ ! -x "$server_dir/run.sh" ]]; then
        printf 'Missing executable server script: %s\n' "$server_dir/run.sh" >&2
        exit 1
    fi

    (
        cd "$server_dir"
        ./run.sh start --no-tmux "$@"
    )
}

case "$(uname -s)" in
    Darwin)
        repo_dir="$HOME/Documents/GitHub/WorldwideChat"
        documents_dir="$HOME/Documents"

        copy_to_all "$repo_dir/spigot-target/WorldwideChat-spigot.jar" \
            "$documents_dir/spigot_wwc_test_server/plugins" \
            "$documents_dir/paper1132_wwc_test_server/plugins"

        copy_plugin "$repo_dir/paper-target/WorldwideChat-paper.jar" \
            "$documents_dir/wwc_test_server/plugins"

        copy_plugin "$repo_dir/folia-target/WorldwideChat-folia.jar" \
            "$documents_dir/folia_wwc_test_server/plugins"

        start_server "$documents_dir/wwc_test_server" --rev "$VERSION_PIN"
        ;;
    Linux)
        repo_dir="$HOME/Documents/WorldwideChat"
        documents_dir="$HOME/Documents"

        copy_to_all "$repo_dir/paper-target/WorldwideChat-paper.jar" \
            "$documents_dir/wwc_test_server/plugins" \
            "$documents_dir/wwc_test_server_1165/plugins"

        copy_to_all "$repo_dir/spigot-target/WorldwideChat-spigot.jar" \
            "$documents_dir/wwc_test_server_1132/plugins" \
            "$documents_dir/wwc_test_server_spigot/plugins"

        start_server "$documents_dir/wwc_test_server"
        ;;
    *)
        printf 'Unsupported operating system: %s\n' "$(uname -s)" >&2
        exit 1
        ;;
esac
