VvVvVvv<div align="center">

# DroidSpec

**Know your Android device. For real.**

Offline-first device intelligence — every number straight from the Android SDK, nothing invented.

[![Build](https://img.shields.io/github/actions/workflow/status/CLXV11/DroidSpec/build.yml?branch=main&label=Build&logo=github)](https://github.com/CLXV11/DroidSpec/actions)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.06-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen?logo=android)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue)](LICENSE)
[![Offline](https://img.shields.io/badge/Network-Not%20Required-success)]()

</div>

---

Most "device info" apps guess. They scrape model databases, extrapolate GPU names,
and print a fake *"Battery health: 98%"* that nobody ever measured.

**DroidSpec works the other way around.** If Android exposes a value, you see it —
clearly labeled and sourced. If it doesn't, you see **"Not available"**. No
estimates dressed as facts. No hardware fairy tales.

---

## Highlights

<table>
<tr>
<td width="50%">

![Dashboard](https://img.shields.io/badge/-Live%20Dashboard-006A5E)
Device header, battery, CPU, RAM, storage, temperature & system health at a glance

![CPU](https://img.shields.io/badge/-CPU-005047)
Core count, architecture, per-core frequencies (best-effort via kernel nodes), real usage from `/proc/stat`

![GPU](https://img.shields.io/badge/-GPU-0B3D2E)
Renderer, vendor, GLES & Vulkan versions read through a temporary EGL context

![Battery](https://img.shields.io/badge/-Battery-1B5E20)
Level, source, temperature, voltage, current, technology, health **exactly as Android reports it**

![Thermal](https://img.shields.io/badge/-Thermal-B26A00)
Battery + ambient sensor + kernel thermal zones, with configurable severity thresholds

</td>
<td width="50%">

![Live](https://img.shields.io/badge/-Live%20Monitor-334155)
Flow-driven metrics that stop when you leave the screen (battery-friendly by design)

![Camera](https://img.shields.io/badge/-Camera-1E3A5F)
Camera2 characteristics, resolutions labeled *calculated* when derived

![Sensors](https://img.shields.io/badge/-Sensors-3B3B6D)
Full sensor catalog with live per-sensor readings

![Network](https://img.shields.io/badge/-Network-004D40)
Wi-Fi/cellular/VPN/metered state, Bluetooth, NFC, GPS, USB — no location permission needed

![Reports](https://img.shields.io/badge/-Reports-37474F)
Clean TXT/JSON export, share & save — plus instant, fully offline global search

</td>
</tr>
</table>

Plus: an internal, cancellable, time-boxed **benchmark** (clearly *not*
Geekbench), English & **Arabic with full RTL**, Material 3 themes, and zero
runtime permissions.

---

## The Accuracy Policy

| What others do | What DroidSpec does |
|---|---|
| Hardcoded device databases | Public Android APIs first, database-free |
| Invented battery-health % | Shows what `BatteryManager` reports, nothing more |
| Guessed GPU models | Renderer string via EGL, or "Not available" |
| Fake per-core CPU temps | Kernel thermal zones only, labeled as such |
| `"Rooted: yes"` from one check | Clearly-labeled *non-authoritative heuristic* |

> **Rule #1:** an honest "Not available" beats a confident lie.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         UI (Compose)                         │
│   Dashboard · Sections · Live Monitor · Benchmark · Reports  │
└──────────────────────────┬──────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              ViewModel (StateFlow, lifecycle-aware)          │
└──────────────────────────┬──────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────┐
│        Use Cases  (Observe* / Snapshot* / Search / Report)   │
└──────────────────────────┬──────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────┐
│   Data Sources (failure-safe wrappers, no fabrication)       │
│   Build · BatteryManager · ActivityManager · StatFs          │
│   Camera2 · SensorManager · ConnectivityManager · EGL · /proc│
└─────────────────────────────────────────────────────────────┘
```

- **Manual DI** via `core/di/AppContainer.kt` — one testable composition root, no magic.
- **Settings** persist with **DataStore**; theme, locale, refresh interval (1s/2s/5s/10s), export format.
- **Polling is lifecycle-aware**: flows collect only while the screen is visible, then cancel themselves.

```
app/src/main/java/com/droidspec/
├── core/            # DI container, calculation utils, flow helpers
├── data/            # battery · cpu · gpu · memory · storage · thermal
│                    # display · camera · sensors · network · device
├── domain/          # models · policies · use cases · search · reports · benchmark
├── presentation/    # viewmodels · screens · settings · report writer
├── navigation/      # routes, adaptive nav (rail ↔ drawer + bottom bar)
└── ui/              # Material 3 theme · reusable components
```

---

## Getting Started

```bash
git clone https://github.com/CLXV11/DroidSpec.git && cd DroidSpec

# Build the debug APK
gradle assembleDebug          # or: ./gradlew assembleDebug (after generating the wrapper)

# Run the test suite (pure JVM, no emulator needed)
gradle testDebugUnitTest
```

**Requirements:** JDK 17 · AGP 8.5 · `minSdk 24` → `targetSdk 34`

> New to the project? Run `gradle wrapper --gradle-version 8.7` once to generate
> `gradlew` scripts and commit them — the standard workflow for local development.

---

## Testing

| Suite | What it guards |
|---|---|
| `CalcUtilsTest` | Percentages, used-bytes, MHz/Mpx/inch math |
| `BatteryMappersTest` | Health/status/plugged int→key mapping |
| `ThermalPolicyTest` | Severity threshold boundaries |
| `BenchmarkScoreTest` | Deterministic internal scoring (weighted sum, 0–1000) |
| `LocalizationTest` | EN ⇄ AR string-key parity |

CI runs all of them on every push, plus **Android Lint**, and uploads the debug
APK + reports as artifacts. See `.github/workflows/build.yml`.

---

## Localization & Accessibility

- **English & العربية** — full RTL layouts via `AppCompatDelegate.setApplicationLocales`
- Every user-visible string lives in `res/values*/strings.xml` — none hardcoded in composables
- Dark / Light / System themes, dynamic-friendly Material 3 palette, screen-reader labels, 48dp+ touch targets

---

## Privacy

```
No runtime permissions   No network calls   No analytics
No accounts              No trackers        No personal-file access
```

Everything is computed on-device, in memory. The app never phones home —
because it literally never phones anywhere.

---

## Contributing

PRs are welcome — especially around new Android API levels and thermal
data sources. Please keep the golden rule intact:

> If the OS doesn't expose it, it ships as "Not available".

1. Fork → branch → commit → PR
2. `./gradlew testDebugUnitTest` must stay green
3. New user-visible strings → add to **both** `values/` and `values-ar/`

---

## License

```
Apache License 2.0 — see LICENSE
```

Built with AndroidX, Jetpack Compose and Kotlin. Third-party components are
distributed under the Apache License 2.0.

<div align="center">
<sub>Made with an obsession for honest numbers.</sub>
</div>
