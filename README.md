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

Automated with [semantic-release](https://github.com/semantic-release/semantic-release) on push to `main`; use [Conventional Commits](https://www.conventionalcommits.org/) (`fix:` patch, `feat:` minor, `feat!:` major). Do not edit the version by hand.

## License

See [LICENSE](LICENSE).
