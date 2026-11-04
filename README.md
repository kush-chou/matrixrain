# Matrix Rain ⚡

[![License: MIT](https://img.shields.io/badge/License-MIT-00ff66.svg?style=flat-square&logo=opensourceinitiative&logoColor=black)](LICENSE)
[![Android: 7.0+](https://img.shields.io/badge/Android-7.0%2B%20(API%2024%2B)-00ff66.svg?style=flat-square&logo=android&logoColor=black)](app/build.gradle)
[![F-Droid: Ready](https://img.shields.io/badge/F--Droid-Ready-00ff66.svg?style=flat-square&logo=f-droid&logoColor=black)](metadata/app.matrixrain.yml)
[![Trackers: 0](https://img.shields.io/badge/Trackers-0%20(100%25%20Offline)-00ff66.svg?style=flat-square&logo=shield&logoColor=black)](web/privacy.html)
[![Web Demo: matrixrain.app](https://img.shields.io/badge/Web%20Demo-matrixrain.app-00ff66.svg?style=flat-square&logo=googlechrome&logoColor=black)](https://matrixrain.app/)

> The authentic, battery-optimized digital code rain live wallpaper for Android.

---

## Overview

**Matrix Rain** brings the iconic digital glyph cascade to your Android home screen with zero battery compromise. Engineered specifically for high-refresh-rate AMOLED and OLED displays, it combines pixel-perfect movie authenticity with aggressive power-saving optimizations.

### Why Matrix Rain?

- 🖤 **True OLED Black (`#000000`)**: Keeps individual display pixels physically turned off, minimizing power consumption and providing infinite contrast.
- ⚡ **0% Standby Battery Drain**: Rendering pauses immediately when your screen turns off or another application covers the wallpaper. Zero background CPU wake locks.
- 🚀 **Adaptive High-Refresh (60Hz / 90Hz / 120Hz)**: Seamless fluid motion synchronized with your device's native display refresh rate.
- 🌊 **Interactive Touch Shockwaves**: Tap or drag across the screen to scatter and deflect falling glyphs in real-time.
- 🎨 **Dynamic Palettes & Special Drops**: Choose between Classic Terminal Green, Crimson Virus, Gold Neo, and Cyan Cyberpunk, complete with rare glowing lead glyphs and custom message drops.
- 🛡️ **100% Offline & Private**: Zero network permissions requested. No telemetry, no third-party SDKs, no trackers, and no advertisements.

---

## Features

- **Authentic Matrix Glyphstream**: Uses the classic combination of Japanese half-width Katakana, Latin letters, and numerals with bright, glowing leading characters.
- **Hardware Acceleration**: Canvas rendering optimized to minimize draw calls and memory allocations during frame loops.
- **Interactive Touch Physics**: Touch input generates radial deflection shockwaves across active glyph columns.
- **Deep Customization Engine**:
  - Rain speed and column density
  - Glyph font size and stream length
  - Character glitch frequency
  - Color themes (Classic Green, Crimson Virus, Gold Neo, Cyan Cyberpunk)
  - Custom falling words and Easter eggs
- **Live Wallpaper Preview**: Dedicated in-app configuration screen to fine-tune settings before applying.

---

## 🌐 Live Web Demo

Experience the digital rain directly in your browser:

🔗 **[https://matrixrain.app/](https://matrixrain.app/)**

The web visualizer is built with pure HTML5 Canvas and vanilla JavaScript, featuring responsive HUD controls and real-time parameter tweaking.

---

## 🛠️ Building from Source

### Prerequisites
- JDK 17 or higher
- Android SDK (API 35, Build Tools 35.0.0+)
- Gradle 8.x / 9.x (wrapper included)

### Compile Release APK
Clone the repository and run the Gradle wrapper:

```bash
git clone https://github.com/kush-chou/matrixrain.git
cd matrixrain

# Build the release APK
./gradlew assembleRelease
```

The compiled APK will be generated at:
```
app/build/outputs/apk/release/app-release-unsigned.apk
```
*(or `app-release.apk` if signed with a local keystore)*.

### Run Verification Suite
Run the automated repository sanity and asset verification checks:

```bash
sh scripts/ci.sh
```

---

## 📦 F-Droid Ready

Matrix Rain is built adhering strictly to F-Droid Inclusion Policies:
- 100% Free and Open Source Software (FOSS).
- Zero proprietary dependencies, non-free binary blobs, or tracking libraries.
- Ready-to-build metadata manifest provided in [`metadata/app.matrixrain.yml`](metadata/app.matrixrain.yml).

---

## 🔒 Privacy Policy

Matrix Rain respects user privacy unconditionally:
- **No Internet Access**: The application does not declare the `android.permission.INTERNET` permission in its manifest. It cannot communicate over any network.
- **No Analytics or Trackers**: No Google Analytics, Firebase, Sentry, or third-party tracking libraries of any kind.
- **No Data Collection**: No telemetry, crash data, or personal identifiers are collected or stored.
- **Zero Ads**: 100% free software forever.

Review our full [Privacy Policy](web/privacy.html) online at [matrixrain.app/privacy.html](https://matrixrain.app/privacy.html).

---

## 📄 License

Distributed under the **MIT License**. See [`LICENSE`](LICENSE) for details.
