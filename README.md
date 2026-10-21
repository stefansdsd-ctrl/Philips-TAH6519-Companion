# Philips TAH6519 Companion

Modern Android companion app for Philips TAH6519 over-ear ANC headphones.

## Features
- Live connection status & battery monitoring (via bonded device + reflection)
- Auto-refresh on Bluetooth events + periodic
- Media controls with live metadata updates (active MediaSession)
- System equalizer + BassBoost presets
- ANC mode tips (physical button)
- Improved Material 3 UI with icons and progress
- Dutch UI labels

## Notes
- Battery level depends on headset reporting via hidden API / HFP
- Equalizer applies to global audio session (0)
- Requires BLUETOOTH_CONNECT permission on Android 12+
- Runtime permissions recommended

Built with Kotlin + Jetpack Compose.

Version 1.1.0: Better UI, live media callback, icons, version bump.
