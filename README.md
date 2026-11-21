# TaskManager

Task manager for Android: live CPU, RAM/swap, GPU, network, disk and battery stats, plus a per-process list you can sort, filter, pin and kill.

## Features

- **Processes**: RAM, CPU and disk-write rate per process; sort by RAM, CPU, name or disk write; filter user apps, system apps and Linux processes; pin and kill.
- **CPU**: usage graph, SoC, cores, clusters, frequencies, governor, temperature, uptime.
- **RAM / Swap**: usage graph and totals.
- **GPU**: usage graph (Adreno/Mali), OpenGL and Vulkan info.
- **Network**: per-interface speed and totals.
- **Disk**: read/write totals and speed per device.
- **Battery**: charge cycle count.
- Themes, Monet, optional notification with live CPU/GPU usage.

## Requirements

- Android 8.0+ (minSdk 26)
- Root **or** [Shizuku](https://shizuku.rikka.app/). The native daemon (`taskmanagerd`) runs with that privilege and reads `/proc` and `/sys`.

Disk-write numbers for other apps' processes need access to `/proc/<pid>/io`; with Shizuku some processes may show 0.

## Build

```
./gradlew assembleRelease
```

Requires JDK 21 and the Android NDK (CMake 3.22.1) for the daemon. Without a keystore the release build is debug-signed.

For signed builds, either set `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, or create `signing.properties` in the repo root:

```
keyAlias=...
keyPassword=...
storeFile=/path/to/keystore
storePassword=...
```

## Updates

The in-app update check reads the latest release of [d224407/TaskManager](https://github.com/d224407/TaskManager/releases/latest) and offers its APK. The repo is set in `main/build.gradle.kts` (`githubRepo`).

## Releases

Releases are automated with [release-please](https://github.com/googleapis/release-please). Write commits in [Conventional Commits](https://www.conventionalcommits.org/) style:

| Commit | Version bump |
| --- | --- |
| `fix: ...` | patch (1.5.2 -> 1.5.3) |
| `feat: ...` | minor (1.5.2 -> 1.6.0) |
| `feat!: ...` or `BREAKING CHANGE:` | major (1.5.2 -> 2.0.0) |
| `docs:`, `chore:`, `refactor:` | no release |

On every push to `main`, release-please opens or updates a "Release x.y.z" pull request with the changelog and the new version in `app/build.gradle.kts`. Merging that PR creates the tag and the GitHub release, and the workflow attaches the APK when the `KEYSTORE`, `STOREPASSWORD`, `ALIAS` and `PASSWORD` secrets are set. `versionCode` is derived from the version (`major*10000 + minor*100 + patch`). Do not edit the version by hand.

GitHub Actions (`.github/workflows/android.yml`) also builds on push to `dev`.

## Project layout

| Module | Purpose |
| --- | --- |
| `app` | Application entry, versioning, signing |
| `main` | UI, screens, daemon client, settings |
| `components` | Shared Compose preference components |
| `taskmanagerd` | Native C++ daemon (JSON over stdin/stdout) |

## License

See [LICENSE](LICENSE).
