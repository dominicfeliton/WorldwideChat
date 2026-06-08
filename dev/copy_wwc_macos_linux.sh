#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

DEFAULT_SERVER_ROOT="/tmp/wwc-test-servers"
ROOT_STATE_PARENT="${TMPDIR:-/tmp}"
ROOT_STATE_PARENT="${ROOT_STATE_PARENT%/}"
SERVER_ROOT_STATE_FILE="${WWC_TEST_SERVER_ROOT_STATE_FILE:-${ROOT_STATE_PARENT}/wwc-test-servers.current}"
SERVER_ROOT_EXPLICIT=false
SERVER_ROOT_FROM_STATE=false
if [[ -n "${WWC_TEST_SERVER_ROOT:-}" ]]; then
    SERVER_ROOT_EXPLICIT=true
    SERVER_ROOT="${WWC_TEST_SERVER_ROOT}"
elif [[ -f "${SERVER_ROOT_STATE_FILE}" ]]; then
    SAVED_SERVER_ROOT="$(sed -n '1p' "${SERVER_ROOT_STATE_FILE}" 2>/dev/null || true)"
    if [[ "${SAVED_SERVER_ROOT}" == /* && -d "${SAVED_SERVER_ROOT}" ]]; then
        SERVER_ROOT_FROM_STATE=true
        SERVER_ROOT="${SAVED_SERVER_ROOT}"
    else
        SERVER_ROOT="${DEFAULT_SERVER_ROOT}"
    fi
else
    SERVER_ROOT="${DEFAULT_SERVER_ROOT}"
fi
MINECRAFT_SERVER_SCRIPT_REPO_URL="${MINECRAFT_SERVER_SCRIPT_REPO_URL:-https://github.com/dominicfeliton/minecraft-server-script.git}"
SERVER_SCRIPT_CLONE_DIR="${SERVER_ROOT}/minecraft-server-script"
TEST_CONFIG_DIR="${SCRIPT_DIR}/test-config"
PORT_BASE="${WWC_TEST_PORT_BASE:-25565}"
JAVA_CMD="${WWC_JAVA_CMD:-java}"
PAPERMC_API_BASE="${PAPERMC_API_BASE:-https://fill.papermc.io/v3}"
MOJANG_VERSION_MANIFEST="${MOJANG_VERSION_MANIFEST:-https://piston-meta.mojang.com/mc/game/version_manifest_v2.json}"
MOJANG_PROFILE_API="${MOJANG_PROFILE_API:-https://api.mojang.com/users/profiles/minecraft}"
GITHUB_API_BASE="${GITHUB_API_BASE:-https://api.github.com}"
LUCKPERMS_METADATA_URL="${LUCKPERMS_METADATA_URL:-https://metadata.luckperms.net/data/all}"
ESSENTIALSX_JENKINS_API="${ESSENTIALSX_JENKINS_API:-https://ci.ender.zone/job/EssentialsX/lastSuccessfulBuild/api/json?tree=number,url,artifacts[fileName,relativePath]}"
USER_AGENT="${WWC_TEST_USER_AGENT:-WorldwideChat-dev-test-script/1.0}"

SERVER_IDS=(
    spigot-1.20
    spigot-latest
    paper-1.20
    paper-latest
    folia-1.20
    folia-latest
)

usage() {
    cat <<EOF
Usage:
  $(basename "$0") [init|init-only|copy|start [server-id] [run.sh args...]]

Commands:
  init       Create/update /tmp test servers, copy local config, copy WWC jars, download Via suite. Default.
  init-only  Create/update /tmp test servers, copy local config, and download Via suite; skip WWC jar copy.
  copy       Copy WWC jars into existing generated server plugin directories.
  start      Start one generated server through its copied minecraft-server-script run.sh.
             If no server id is provided in an interactive shell, prompt for server and optional plugins.

Generated server ids:
  ${SERVER_IDS[*]}

Prompted start choices:
  Pick Paper, Folia, or Spigot, then latest or an exact custom version.
  Optional plugin packs: blank slate, LuckPerms, EssentialsX, or both.

Environment overrides:
  WWC_TEST_SERVER_ROOT=${SERVER_ROOT}
  MINECRAFT_SERVER_SCRIPT_REPO_URL=${MINECRAFT_SERVER_SCRIPT_REPO_URL}
  WWC_TEST_PORT_BASE=${PORT_BASE}
  WWC_JAVA_CMD=${JAVA_CMD}

Local config seed:
  ${TEST_CONFIG_DIR}
EOF
}

die() {
    printf 'Error: %s\n' "$*" >&2
    exit 1
}

set_server_root() {
    SERVER_ROOT="$1"
    SERVER_SCRIPT_CLONE_DIR="${SERVER_ROOT}/minecraft-server-script"
}

record_server_root() {
    mkdir -p "$(dirname "${SERVER_ROOT_STATE_FILE}")"
    printf '%s\n' "${SERVER_ROOT}" > "${SERVER_ROOT_STATE_FILE}"
}

select_setup_root() {
    local skip_remembered="${1:-false}"
    local choice
    local fresh_root
    local temp_parent

    if [[ "${SERVER_ROOT_EXPLICIT}" == "true" ]]; then
        printf 'Using configured test server root: %s\n' "${SERVER_ROOT}"
        return
    fi

    if [[ "${skip_remembered}" == "true" && "${SERVER_ROOT_FROM_STATE}" == "true" ]]; then
        printf 'Using remembered test server root: %s\n' "${SERVER_ROOT}"
        return
    fi

    if [[ ! -t 0 ]]; then
        if [[ "${SERVER_ROOT_FROM_STATE}" == "true" ]]; then
            printf 'Using remembered test server root: %s\n' "${SERVER_ROOT}"
        else
            printf 'Using test server root: %s\n' "${SERVER_ROOT}"
        fi
        return
    fi

    printf 'Select test server root:\n' >&2
    printf '  1) Reuse current root (%s)\n' "${SERVER_ROOT}" >&2
    printf '  2) Fresh random root\n' >&2
    printf 'Choice [1]: ' >&2
    read -r choice
    choice="${choice:-1}"

    case "${choice}" in
        1)
            ;;
        2)
            temp_parent="${TMPDIR:-/tmp}"
            temp_parent="${temp_parent%/}"
            fresh_root="$(mktemp -d "${temp_parent}/wwc-test-servers.XXXXXX")" || die "Could not create a fresh test server root."
            set_server_root "${fresh_root}"
            SERVER_ROOT_FROM_STATE=false
            ;;
        *)
            die "Invalid test server root selection '${choice}'."
            ;;
    esac

    record_server_root
    printf 'Using test server root: %s\n' "${SERVER_ROOT}"
}

require_command() {
    command -v "$1" >/dev/null 2>&1 || die "'$1' is required."
}

require_common_commands() {
    require_command curl
    require_command jq
}

require_test_config() {
    require_command rsync

    [[ -f "${TEST_CONFIG_DIR}/ops.json" ]] || die "Missing ${TEST_CONFIG_DIR}/ops.json. Create dev/test-config from dev/test-config.sample or copy an existing test server config."
    [[ -d "${TEST_CONFIG_DIR}/plugins/WorldwideChat" ]] || die "Missing ${TEST_CONFIG_DIR}/plugins/WorldwideChat. Create dev/test-config from dev/test-config.sample or copy an existing test server config."
}

refresh_server_script() {
    require_command git

    local parent_dir

    parent_dir="$(dirname "${SERVER_SCRIPT_CLONE_DIR}")"

    [[ "${SERVER_SCRIPT_CLONE_DIR}" == "${SERVER_ROOT}/minecraft-server-script" ]] || die "Refusing to refresh unexpected server script path: ${SERVER_SCRIPT_CLONE_DIR}"
    mkdir -p "${parent_dir}"
    if [[ -d "${SERVER_SCRIPT_CLONE_DIR}/.git" ]]; then
        git -C "${SERVER_SCRIPT_CLONE_DIR}" remote set-url origin "${MINECRAFT_SERVER_SCRIPT_REPO_URL}"
        git -C "${SERVER_SCRIPT_CLONE_DIR}" fetch --depth 1 origin
        git -C "${SERVER_SCRIPT_CLONE_DIR}" checkout --detach FETCH_HEAD
        printf 'Updated minecraft-server-script -> %s\n' "${SERVER_SCRIPT_CLONE_DIR}"
    elif [[ -e "${SERVER_SCRIPT_CLONE_DIR}" ]]; then
        die "${SERVER_SCRIPT_CLONE_DIR} exists but is not a git checkout. Move it aside or set WWC_TEST_SERVER_ROOT to a clean temp root."
    else
        git clone --depth 1 "${MINECRAFT_SERVER_SCRIPT_REPO_URL}" "${SERVER_SCRIPT_CLONE_DIR}"
        printf 'Cloned minecraft-server-script -> %s\n' "${SERVER_SCRIPT_CLONE_DIR}"
    fi

    [[ -f "${SERVER_SCRIPT_CLONE_DIR}/run.sh" ]] || die "minecraft-server-script checkout did not include run.sh."
}

validate_port_base() {
    [[ "${PORT_BASE}" =~ ^[0-9]+$ ]] || die "WWC_TEST_PORT_BASE must be a number."
}

curl_json() {
    local url="$1"
    curl -fsSL -H "User-Agent: ${USER_AGENT}" -H "Accept: application/json" "$url"
}

server_dir() {
    printf '%s/%s\n' "${SERVER_ROOT}" "$1"
}

server_project() {
    case "$1" in
        spigot-*) printf 'spigot\n' ;;
        paper-*) printf 'paper\n' ;;
        folia-*) printf 'folia\n' ;;
        *) die "Unknown server id '$1'." ;;
    esac
}

server_artifact() {
    case "$1" in
        spigot-*) printf '%s/spigot-target/WorldwideChat-spigot.jar\n' "${REPO_DIR}" ;;
        paper-*) printf '%s/paper-target/WorldwideChat-paper.jar\n' "${REPO_DIR}" ;;
        folia-*) printf '%s/folia-target/WorldwideChat-folia.jar\n' "${REPO_DIR}" ;;
        *) die "Unknown server id '$1'." ;;
    esac
}

versioned_server_id() {
    [[ "$1" =~ ^(spigot|paper|folia)-[0-9]+(\.[0-9]+){1,2}$ ]]
}

known_server_id() {
    local requested="$1"
    local id
    for id in "${SERVER_IDS[@]}"; do
        [[ "${id}" == "${requested}" ]] && return 0
    done
    return 1
}

valid_server_id() {
    known_server_id "$1" || versioned_server_id "$1"
}

server_port() {
    local id="$1"
    local index=0
    local known_id

    for known_id in "${SERVER_IDS[@]}"; do
        if [[ "${known_id}" == "${id}" ]]; then
            printf '%s\n' "$((PORT_BASE + index))"
            return
        fi
        index=$((index + 1))
    done

    versioned_server_id "${id}" || die "Unknown server id '$id'."
    printf '%s\n' "$((PORT_BASE + 100))"
}

all_server_ids_or_args() {
    if [[ $# -gt 0 ]]; then
        printf '%s\n' "$@"
    else
        printf '%s\n' "${SERVER_IDS[@]}"
    fi
}

resolve_mojang_latest_release() {
    local version
    version="$(curl_json "${MOJANG_VERSION_MANIFEST}" | jq -r '.latest.release // empty')"
    [[ -n "${version}" && "${version}" != "null" ]] || die "Could not resolve latest Minecraft release from Mojang manifest."
    printf '%s\n' "${version}"
}

resolve_papermc_earliest_minor() {
    local project="$1"
    local minor="$2"
    local version
    version="$(
        curl_json "${PAPERMC_API_BASE}/projects/${project}" \
            | jq -r --arg minor "${minor}" '.versions[$minor] // empty | if type == "array" and length > 0 then .[-1] else empty end'
    )"
    [[ -n "${version}" && "${version}" != "null" ]] || die "Could not resolve earliest ${project} ${minor}.x from PaperMC."
    printf '%s\n' "${version}"
}

resolve_papermc_latest_version() {
    local project="$1"
    local version
    version="$(
        curl_json "${PAPERMC_API_BASE}/projects/${project}" \
            | jq -r '[.versions | to_entries[] | .value[]][0] // empty'
    )"
    [[ -n "${version}" && "${version}" != "null" ]] || die "Could not resolve latest ${project} version from PaperMC."
    printf '%s\n' "${version}"
}

assert_mojang_release_exists() {
    local version="$1"
    local exists

    exists="$(
        curl_json "${MOJANG_VERSION_MANIFEST}" \
            | jq -r --arg version "${version}" 'any(.versions[]; .id == $version and .type == "release")'
    )"
    [[ "${exists}" == "true" ]] || die "Minecraft release '${version}' was not found in Mojang's version manifest."
}

assert_papermc_version_exists() {
    local project="$1"
    local version="$2"
    local exists

    exists="$(
        curl_json "${PAPERMC_API_BASE}/projects/${project}" \
            | jq -r --arg version "${version}" 'any(.versions | to_entries[] | .value[]; . == $version)'
    )"
    [[ "${exists}" == "true" ]] || die "${project} version '${version}' was not found in PaperMC."
}

assert_custom_server_version_exists() {
    local project="$1"
    local version="$2"

    require_common_commands
    case "${project}" in
        spigot) assert_mojang_release_exists "${version}" ;;
        paper|folia) assert_papermc_version_exists "${project}" "${version}" ;;
        *) die "Unknown server project '${project}'." ;;
    esac
}

resolve_papermc_build_state() {
    local project="$1"
    local version="$2"
    local build_json
    local build
    local channel

    build_json="$(
        curl_json "${PAPERMC_API_BASE}/projects/${project}/versions/${version}/builds" \
            | jq -c '([.[] | select(.channel == "STABLE")] | max_by((.id // .number) | tonumber? // 0)) // ([.[]] | max_by((.id // .number) | tonumber? // 0)) // empty'
    )"
    [[ -n "${build_json}" && "${build_json}" != "null" ]] || die "Could not resolve ${project} ${version} build from PaperMC."

    build="$(jq -r '(.id // .number // empty)' <<< "${build_json}")"
    channel="$(jq -r '(.channel // empty)' <<< "${build_json}")"
    [[ -n "${build}" && "${build}" != "null" ]] || die "Could not resolve ${project} ${version} build id."
    [[ -n "${channel}" && "${channel}" != "null" ]] || die "Could not resolve ${project} ${version} build channel."

    printf '%s|%s\n' "${build}" "${channel}"
}

resolve_server_version() {
    case "$1" in
        spigot-1.20) printf '1.20\n' ;;
        spigot-latest) resolve_mojang_latest_release ;;
        paper-1.20) resolve_papermc_earliest_minor paper 1.20 ;;
        paper-latest) resolve_papermc_latest_version paper ;;
        folia-1.20) resolve_papermc_earliest_minor folia 1.20 ;;
        folia-latest) resolve_papermc_latest_version folia ;;
        spigot-[0-9]*|paper-[0-9]*|folia-[0-9]*) printf '%s\n' "${1#*-}" ;;
        *) die "Unknown server id '$1'." ;;
    esac
}

write_server_conf() {
    local id="$1"
    local project="$2"
    local dir="$3"

    cat > "${dir}/server.conf" <<EOF
# Generated by WorldwideChat dev/copy_wwc_macos_linux.sh.
SERVER_DIR=${dir}
PROJECT_NAME=${project}
DEFAULT_WORLD_NAME=world
DEFAULT_XMS=1G
DEFAULT_XMX=2G
JAVA_CMD=${JAVA_CMD}
TMUX_SESSION_NAME=wwc-${id}
AUTO_AGREE_EULA=true
CHECK_TAILSCALE_BIND=false
EOF
}

write_current_version() {
    local project="$1"
    local version="$2"
    local dir="$3"
    local state
    local build
    local channel

    if [[ "${project}" == "spigot" ]]; then
        printf '%s\n' "${version}" > "${dir}/current_version.txt"
        return
    fi

    state="$(resolve_papermc_build_state "${project}" "${version}")"
    build="${state%%|*}"
    channel="${state#*|}"

    cat > "${dir}/current_version.txt" <<EOF
VERSION=${version}
BUILD=${build}
CHANNEL=${channel}
DECLINED_STABLE_TARGET=
DECLINED_BETA_TARGET=
DECLINED_ALPHA_TARGET=
EOF
}

write_server_properties() {
    local id="$1"
    local dir="$2"
    local port="$3"

    cat > "${dir}/server.properties" <<EOF
# Generated by WorldwideChat dev/copy_wwc_macos_linux.sh.
server-ip=
server-port=${port}
motd=WorldwideChat test ${id}
EOF
}

prepare_server_dir() {
    local id="$1"
    local project
    local version
    local dir
    local port

    valid_server_id "${id}" || die "Unknown server id '${id}'. Valid ids: ${SERVER_IDS[*]} or <platform>-<version>."

    project="$(server_project "${id}")"
    version="$(resolve_server_version "${id}")"
    if versioned_server_id "${id}" && ! known_server_id "${id}"; then
        assert_custom_server_version_exists "${project}" "${version}"
    fi
    dir="$(server_dir "${id}")"
    port="$(server_port "${id}")"

    mkdir -p "${dir}/plugins"
    cp "${SERVER_SCRIPT_CLONE_DIR}/run.sh" "${dir}/run.sh"
    chmod +x "${dir}/run.sh"
    write_server_conf "${id}" "${project}" "${dir}"
    write_current_version "${project}" "${version}" "${dir}"
    write_server_properties "${id}" "${dir}" "${port}"

    printf 'Prepared %-14s project=%-6s version=%-8s port=%s dir=%s\n' "${id}" "${project}" "${version}" "${port}" "${dir}"
}

init_servers() {
    require_common_commands
    validate_port_base

    local id

    mkdir -p "${SERVER_ROOT}"
    refresh_server_script
    for id in "${SERVER_IDS[@]}"; do
        prepare_server_dir "${id}"
    done
}

extract_local_op_name() {
    local name

    name="$(jq -r '.[0].name // empty' "${TEST_CONFIG_DIR}/ops.json")"
    [[ -n "${name}" && "${name}" != "null" ]] || die "Could not read a player name from ${TEST_CONFIG_DIR}/ops.json."
    [[ "${name}" =~ ^[A-Za-z0-9_]{3,16}$ ]] || die "Invalid Minecraft player name '${name}' in ${TEST_CONFIG_DIR}/ops.json."

    printf '%s\n' "${name}"
}

hyphenate_uuid() {
    local raw="${1//-/}"
    raw="$(printf '%s' "${raw}" | tr '[:upper:]' '[:lower:]')"
    [[ "${raw}" =~ ^[0-9a-f]{32}$ ]] || die "Invalid Mojang UUID '${1}'."

    printf '%s-%s-%s-%s-%s\n' "${raw:0:8}" "${raw:8:4}" "${raw:12:4}" "${raw:16:4}" "${raw:20:12}"
}

resolve_mojang_profile() {
    local requested_name="$1"
    local profile
    local raw_uuid
    local uuid
    local canonical_name

    profile="$(curl_json "${MOJANG_PROFILE_API}/${requested_name}")" || die "Could not resolve Mojang profile for '${requested_name}'. Check the name and network access."
    raw_uuid="$(jq -r '.id // empty' <<< "${profile}")"
    canonical_name="$(jq -r '.name // empty' <<< "${profile}")"
    [[ -n "${raw_uuid}" && "${raw_uuid}" != "null" ]] || die "Mojang profile for '${requested_name}' did not include a UUID."
    [[ -n "${canonical_name}" && "${canonical_name}" != "null" ]] || die "Mojang profile for '${requested_name}' did not include a player name."

    uuid="$(hyphenate_uuid "${raw_uuid}")"
    printf '%s|%s\n' "${uuid}" "${canonical_name}"
}

write_op_files() {
    local dir="$1"
    local uuid="$2"
    local name="$3"

    cat > "${dir}/ops.json" <<EOF
[
  {
    "uuid": "${uuid}",
    "name": "${name}",
    "level": 4,
    "bypassesPlayerLimit": false
  }
]
EOF

    cat > "${dir}/usercache.json" <<EOF
[
  {
    "uuid": "${uuid}",
    "name": "${name}",
    "expiresOn": "2099-01-01 00:00:00 +0000"
  }
]
EOF
}

copy_test_config_for_id() {
    local id="$1"
    local uuid="$2"
    local name="$3"
    local dir
    local wwc_config_dir

    dir="$(server_dir "${id}")"
    wwc_config_dir="${dir}/plugins/WorldwideChat"

    [[ -d "${dir}" ]] || die "Missing server directory: ${dir}. Run $(basename "$0") init first."
    mkdir -p "${dir}/plugins"
    write_op_files "${dir}" "${uuid}" "${name}"
    mkdir -p "${wwc_config_dir}"
    rsync -a --delete --delete-excluded --exclude '.DS_Store' --exclude 'messages*' "${TEST_CONFIG_DIR}/plugins/WorldwideChat/" "${wwc_config_dir}/"
    printf 'Copied test config -> %s\n' "${dir}"
}

copy_test_config() {
    require_common_commands
    require_test_config

    local op_name
    local profile
    local uuid
    local canonical_name
    local id

    op_name="$(extract_local_op_name)"
    profile="$(resolve_mojang_profile "${op_name}")"
    uuid="${profile%%|*}"
    canonical_name="${profile#*|}"
    write_op_files "${TEST_CONFIG_DIR}" "${uuid}" "${canonical_name}"

    while IFS= read -r id; do
        copy_test_config_for_id "${id}" "${uuid}" "${canonical_name}"
    done < <(all_server_ids_or_args "$@")
}

copy_wwc_artifact_for_id() {
    local id="$1"
    local artifact
    local dir
    local plugins_dir

    artifact="$(server_artifact "${id}")"
    dir="$(server_dir "${id}")"
    plugins_dir="${dir}/plugins"

    [[ -f "${artifact}" ]] || die "Missing build artifact: ${artifact}. Run mvn -B package -Dyamltranslator.skip=true -Dmaven.test.skip=true first."
    [[ -d "${plugins_dir}" ]] || die "Missing plugins directory: ${plugins_dir}. Run $(basename "$0") init-only first."

    find "${plugins_dir}" -maxdepth 1 -type f -name 'WorldwideChat-*.jar' -delete
    cp "${artifact}" "${plugins_dir}/"
    printf 'Copied %s -> %s\n' "$(basename "${artifact}")" "${plugins_dir}"
}

copy_wwc_artifacts() {
    local id

    while IFS= read -r id; do
        copy_wwc_artifact_for_id "${id}"
    done < <(all_server_ids_or_args "$@")
}

resolve_github_release_asset() {
    local repo="$1"
    local prefix="$2"
    local url

    url="$(
        curl_json "${GITHUB_API_BASE}/repos/${repo}/releases/latest" \
            | jq -r --arg prefix "${prefix}" 'first(.assets[] | select(.name | test("^" + $prefix + "-.*\\.jar$")) | .browser_download_url) // empty'
    )"
    [[ -n "${url}" && "${url}" != "null" ]] || die "Could not resolve latest ${prefix} jar from ${repo} GitHub releases."
    printf '%s\n' "${url}"
}

download_via_jar() {
    local repo="$1"
    local prefix="$2"
    local cache_dir="$3"
    local url
    local file_name
    local target
    local tmp_file

    url="$(resolve_github_release_asset "${repo}" "${prefix}")"
    file_name="${url##*/}"
    target="${cache_dir}/${file_name}"

    if [[ ! -s "${target}" ]]; then
        tmp_file="${target}.tmp"
        printf 'Downloading %s -> %s\n' "${url}" "${target}" >&2
        curl -fL --retry 3 -H "User-Agent: ${USER_AGENT}" -o "${tmp_file}" "${url}"
        [[ -s "${tmp_file}" ]] || die "Downloaded ${prefix} jar is empty."
        mv -f "${tmp_file}" "${target}"
    else
        printf 'Using cached %s\n' "${target}" >&2
    fi

    printf '%s\n' "${target}"
}

download_managed_jar() {
    local url="$1"
    local cache_dir="$2"
    local label="$3"
    local file_name
    local target
    local tmp_file

    file_name="${url##*/}"
    target="${cache_dir}/${file_name}"

    if [[ ! -s "${target}" ]]; then
        tmp_file="${target}.tmp"
        printf 'Downloading %s -> %s\n' "${url}" "${target}" >&2
        curl -fL --retry 3 -H "User-Agent: ${USER_AGENT}" -o "${tmp_file}" "${url}"
        [[ -s "${tmp_file}" ]] || die "Downloaded ${label} jar is empty."
        mv -f "${tmp_file}" "${target}"
    else
        printf 'Using cached %s\n' "${target}" >&2
    fi

    printf '%s\n' "${target}"
}

install_via_suite() {
    require_common_commands

    local cache_dir="${SERVER_ROOT}/.cache/via"
    local via_version
    local via_backwards
    local via_rewind
    local id
    local plugins_dir

    mkdir -p "${cache_dir}"

    via_version="$(download_via_jar ViaVersion/ViaVersion ViaVersion "${cache_dir}")"
    via_backwards="$(download_via_jar ViaVersion/ViaBackwards ViaBackwards "${cache_dir}")"
    via_rewind="$(download_via_jar ViaVersion/ViaRewind ViaRewind "${cache_dir}")"

    while IFS= read -r id; do
        plugins_dir="$(server_dir "${id}")/plugins"
        [[ -d "${plugins_dir}" ]] || die "Missing plugins directory: ${plugins_dir}. Run $(basename "$0") init-only first."

        find "${plugins_dir}" -maxdepth 1 -type f \( \
            -name 'ViaVersion-*.jar' -o \
            -name 'ViaBackwards-*.jar' -o \
            -name 'ViaRewind-*.jar' \
        \) -delete
        cp "${via_version}" "${plugins_dir}/"
        cp "${via_backwards}" "${plugins_dir}/"
        cp "${via_rewind}" "${plugins_dir}/"
        printf 'Installed Via suite -> %s\n' "${plugins_dir}"
    done < <(all_server_ids_or_args "$@")
}

resolve_luckperms_bukkit_url() {
    local url

    url="$(curl_json "${LUCKPERMS_METADATA_URL}" | jq -r '.downloads.bukkit // empty')"
    [[ -n "${url}" && "${url}" != "null" ]] || die "Could not resolve latest LuckPerms Bukkit jar from ${LUCKPERMS_METADATA_URL}."
    printf '%s\n' "${url}"
}

resolve_essentialsx_url() {
    local build_json
    local build_url
    local artifact_path

    build_json="$(curl -g -fsSL -H "User-Agent: ${USER_AGENT}" -H "Accept: application/json" "${ESSENTIALSX_JENKINS_API}")"
    build_url="$(jq -r '.url // empty' <<< "${build_json}")"
    artifact_path="$(
        jq -r 'first(.artifacts[] | select(.fileName | test("^EssentialsX-[0-9].*\\.jar$")) | .relativePath) // empty' <<< "${build_json}"
    )"
    [[ -n "${build_url}" && "${build_url}" != "null" ]] || die "Could not resolve EssentialsX Jenkins build URL from ${ESSENTIALSX_JENKINS_API}."
    [[ -n "${artifact_path}" && "${artifact_path}" != "null" ]] || die "Could not resolve latest EssentialsX dev jar from ${ESSENTIALSX_JENKINS_API}."

    printf '%sartifact/%s\n' "${build_url%/}/" "${artifact_path}"
}

validate_optional_plugin_pack_for_server() {
    local id="$1"
    local pack="$2"

    if [[ "${id}" == folia-* && ( "${pack}" == "essentialsx" || "${pack}" == "both" ) ]]; then
        die "EssentialsX does not support Folia. Choose blank slate or LuckPerms for ${id}."
    fi
}

install_optional_plugins() {
    local id="$1"
    local pack="$2"
    local plugins_dir
    local cache_dir="${SERVER_ROOT}/.cache/optional"
    local luckperms
    local essentialsx

    plugins_dir="$(server_dir "${id}")/plugins"
    [[ -d "${plugins_dir}" ]] || die "Missing plugins directory: ${plugins_dir}. Run $(basename "$0") init-only first."
    validate_optional_plugin_pack_for_server "${id}" "${pack}"

    find "${plugins_dir}" -maxdepth 1 -type f \( \
        -name 'LuckPerms-*.jar' -o \
        -name 'EssentialsX-*.jar' \
    \) -delete

    [[ "${pack}" == "blank" ]] && return

    require_common_commands
    mkdir -p "${cache_dir}"

    case "${pack}" in
        luckperms|both)
            luckperms="$(download_managed_jar "$(resolve_luckperms_bukkit_url)" "${cache_dir}" LuckPerms)"
            cp "${luckperms}" "${plugins_dir}/"
            printf 'Installed LuckPerms -> %s\n' "${plugins_dir}"
            ;;
    esac

    case "${pack}" in
        essentialsx|both)
            essentialsx="$(download_managed_jar "$(resolve_essentialsx_url)" "${cache_dir}" EssentialsX)"
            cp "${essentialsx}" "${plugins_dir}/"
            printf 'Installed EssentialsX -> %s\n' "${plugins_dir}"
            ;;
    esac
}

prompt_server_id() {
    local choice
    local platform
    local version_choice
    local version

    printf 'Select test platform:\n' >&2
    printf '  1) Paper\n' >&2
    printf '  2) Folia\n' >&2
    printf '  3) Spigot\n' >&2
    printf 'Choice [1]: ' >&2
    read -r choice
    choice="${choice:-1}"

    case "${choice}" in
        1) platform="paper" ;;
        2) platform="folia" ;;
        3) platform="spigot" ;;
        *) die "Invalid platform selection '${choice}'." ;;
    esac

    printf 'Select %s version:\n' "${platform}" >&2
    printf '  1) latest\n' >&2
    printf '  2) custom exact version\n' >&2
    printf 'Choice [1]: ' >&2
    read -r version_choice
    version_choice="${version_choice:-1}"

    case "${version_choice}" in
        1) printf '%s-latest\n' "${platform}" ;;
        2)
            printf '%s version, for example 1.20.4 or 26.1.2: ' "${platform}" >&2
            read -r version
            [[ "${version}" =~ ^[0-9]+(\.[0-9]+){1,2}$ ]] || die "Invalid ${platform} version '${version}'."
            assert_custom_server_version_exists "${platform}" "${version}"
            printf '%s-%s\n' "${platform}" "${version}"
            ;;
        *) die "Invalid version selection '${version_choice}'." ;;
    esac
}

prompt_optional_plugin_pack() {
    local choice

    printf 'Optional plugin pack:\n' >&2
    printf '  1) blank slate\n' >&2
    printf '  2) LuckPerms\n' >&2
    printf '  3) EssentialsX\n' >&2
    printf '  4) LuckPerms + EssentialsX\n' >&2
    printf 'Choice [1]: ' >&2
    read -r choice
    choice="${choice:-1}"

    case "${choice}" in
        1) printf 'blank\n' ;;
        2) printf 'luckperms\n' ;;
        3) printf 'essentialsx\n' ;;
        4) printf 'both\n' ;;
        *) die "Invalid optional plugin selection '${choice}'." ;;
    esac
}

prepare_prompted_server() {
    local id="$1"
    local plugin_pack="$2"

    validate_optional_plugin_pack_for_server "${id}" "${plugin_pack}"
    require_common_commands
    validate_port_base
    mkdir -p "${SERVER_ROOT}"
    refresh_server_script
    prepare_server_dir "${id}"
    copy_test_config "${id}"
    copy_wwc_artifacts "${id}"
    install_via_suite "${id}"
    install_optional_plugins "${id}" "${plugin_pack}"
}

start_prompted_server() {
    [[ -t 0 ]] || die "start without a server id requires an interactive terminal. Usage: $(basename "$0") start <server-id> [run.sh args...]"

    local id
    local plugin_pack

    select_setup_root true
    id="$(prompt_server_id)"
    plugin_pack="$(prompt_optional_plugin_pack)"
    prepare_prompted_server "${id}" "${plugin_pack}"
    start_server "${id}" "$@"
}

start_server() {
    local id="$1"
    shift

    valid_server_id "${id}" || die "Unknown server id '${id}'. Valid ids: ${SERVER_IDS[*]} or <platform>-<version>."

    local dir
    dir="$(server_dir "${id}")"
    [[ -x "${dir}/run.sh" ]] || die "Missing executable server script: ${dir}/run.sh. Run $(basename "$0") init-only first."

    (
        cd "${dir}"
        ./run.sh start "$@"
    )
}

command_name="${1:-init}"
if [[ $# -gt 0 ]]; then
    shift
fi

case "${command_name}" in
    init)
        select_setup_root
        init_servers
        copy_test_config
        copy_wwc_artifacts
        install_via_suite
        ;;
    init-only)
        select_setup_root
        init_servers
        copy_test_config
        install_via_suite
        ;;
    copy)
        copy_wwc_artifacts
        ;;
    start)
        if [[ $# -eq 0 || "${1:-}" == --* ]]; then
            start_prompted_server "$@"
        else
            start_server "$@"
        fi
        ;;
    help|--help|-h)
        usage
        ;;
    *)
        usage >&2
        exit 1
        ;;
esac
