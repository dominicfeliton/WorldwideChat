# Compiling WWC

Compiling this project is designed to be straightforward.

First, make sure you put a YAMLTranslator configuration file in the root of the project, titled `yt-settings.yml`.
[You can view the default configuration values here.](https://github.com/dominicfeliton/YAMLTranslator/blob/main/src/main/resources/yt-settings.yml)

Next, install Docker Desktop for your distribution.

Run `./dev/setup-dev-env.sh` on macOS/Linux, or `dev\setup-dev-env.bat` on Windows. This starts MongoDB, MySQL, and PostgreSQL for the unit tests.

Then import the WorldwideChat directory as a Maven project in IntelliJ/Eclipse and build with:

```bash
mvn clean package
```

If you cannot run YAMLTranslator because you do not have access to Amazon Translate, use:

```bash
mvn clean package -Dyamltranslator.skip=true
```

To stop the database containers, run `./dev/takedown-dev-env.sh` on macOS/Linux, or `dev\takedown-dev-env.bat` on Windows.

## Local Test Servers

The canonical macOS/Linux helper `./dev/copy_wwc_macos_linux.sh` creates disposable test servers under `/tmp/wwc-test-servers`.

On Windows, use `dev\copy_wwc_windows.bat`. It is a WSL-only wrapper around the same macOS/Linux helper, so it requires `wsl.exe`, a default WSL distro, and these WSL-side commands: `bash`, `git`, `curl`, `jq`, `rsync`, and Java. Windows test servers are created in WSL at `/tmp/wwc-test-servers`, not in a Windows user directory.

It manages these server ids:

- `spigot-1.20`
- `spigot-latest`
- `paper-1.20`
- `paper-latest`
- `folia-1.20`
- `folia-latest`

The helper keeps a managed `minecraft-server-script` checkout at `${WWC_TEST_SERVER_ROOT}/minecraft-server-script`. During `init`, `init-only`, and prompted `start` setup, it clones that checkout if missing or fetches the latest repo state if it already exists. It copies `run.sh` from that managed checkout into each generated server directory; no separate local checkout is required.

When `init`, `init-only`, or prompted `start` is run in an interactive terminal, the helper asks whether to reuse the current test root or create a fresh random root. A fresh root is a new `${TMPDIR:-/tmp}/wwc-test-servers.*` directory; the helper never deletes the previous root. The selected root is remembered so a chained `init && start --no-tmux` reuses the same fresh root instead of asking twice. If `WWC_TEST_SERVER_ROOT` is set, the helper uses that explicit root. If stdin is non-interactive, the helper uses the remembered/current root without prompting. The Windows batch helper inherits the same behavior through WSL.

Common commands:

```bash
./dev/copy_wwc_macos_linux.sh init
./dev/copy_wwc_macos_linux.sh init-only
./dev/copy_wwc_macos_linux.sh copy
./dev/copy_wwc_macos_linux.sh start paper-latest --no-tmux
```

Windows equivalents:

```bat
dev\copy_wwc_windows.bat init
dev\copy_wwc_windows.bat init-only
dev\copy_wwc_windows.bat copy
dev\copy_wwc_windows.bat start paper-latest --no-tmux
```

One-shot IntelliJ run command from the repo root for Paper latest:

```bash
./dev/copy_wwc_macos_linux.sh init && ./dev/copy_wwc_macos_linux.sh start paper-latest --no-tmux
```

Windows:

```bat
dev\copy_wwc_windows.bat init && dev\copy_wwc_windows.bat start paper-latest --no-tmux
```

Interactive IntelliJ run command from the repo root:

```bash
./dev/copy_wwc_macos_linux.sh start --no-tmux
```

Windows:

```bat
dev\copy_wwc_windows.bat start --no-tmux
```

When `start` is run without a server id in an interactive terminal, the helper prompts for:

- platform: Paper, Folia, or Spigot
- version: latest, or a custom exact version such as `1.20.4` or `26.1.2`
- blank slate, LuckPerms, EssentialsX, or LuckPerms + EssentialsX

Custom versions are checked before server prep: Spigot versions are checked against Mojang's release manifest, and Paper/Folia versions are checked against PaperMC.

The prompted path prepares the selected server under the selected test root, copies the matching WWC artifact, installs ViaVersion/ViaBackwards/ViaRewind, installs the selected optional plugin pack, then starts the server.

EssentialsX is blocked for Folia targets because EssentialsX does not support Folia yet.

Useful overrides:

```bash
WWC_TEST_SERVER_ROOT=/tmp/wwc-test-servers-smoke ./dev/copy_wwc_macos_linux.sh init
WWC_TEST_PORT_BASE=25600 ./dev/copy_wwc_macos_linux.sh init
WWC_JAVA_CMD=/path/to/java ./dev/copy_wwc_macos_linux.sh start folia-latest --no-tmux
MINECRAFT_SERVER_SCRIPT_REPO_URL=https://github.com/your-fork/minecraft-server-script.git ./dev/copy_wwc_macos_linux.sh init
```

## Local Test Config

`dev/test-config/` is local-only and ignored by git. The init commands copy these files into every generated test server:

- `dev/test-config/ops.json`
- `dev/test-config/usercache.json`
- `dev/test-config/plugins/WorldwideChat/`

The helper uses the player name from `dev/test-config/ops.json`, resolves the current online-mode UUID through Mojang, and writes fresh `ops.json` and `usercache.json` into each generated server. This avoids stale UUIDs preventing the configured local test player from being op on Paper/Spigot/Folia servers with `online-mode=true`.

This repo includes a tracked sample tree in `dev/test-config.sample/`. To bootstrap a new machine, copy the sample tree and then edit credentials/player data locally:

```bash
cp -R dev/test-config.sample dev/test-config
```

If you already have a known-good local test server, copy its `ops.json`, `usercache.json`, and `plugins/WorldwideChat/` config into `dev/test-config/`.

The helper intentionally excludes `messages*` localization files from copied test config. Those should be generated by the plugin/YAMLTranslator flow instead of carried in local test seeds.

The helper also downloads ViaVersion, ViaBackwards, and ViaRewind into each generated server so a newer client can join older test servers.

For prompted starts, LuckPerms is resolved from `https://metadata.luckperms.net/data/all`. EssentialsX is resolved from the current successful Jenkins dev build at `https://ci.ender.zone/job/EssentialsX/`, because the stable `2.21.2` release does not understand Paper's `26.1.x` Bukkit version format.
