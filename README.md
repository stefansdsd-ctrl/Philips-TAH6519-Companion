# Philips TAH6519 Companion

Modern Android companion app for Philips TAH6519 over-ear ANC headphones.

## Features
- Live connection status & battery monitoring (reflection + HF indicators)
- Device MAC address display
- Battery history (last measurements)
- Persistent status notification + configurable low battery alert
- Auto-refresh on Bluetooth events + periodic (15s, toggleable)
- Media controls with volume slider, live metadata and album art
- System equalizer + BassBoost + Virtualizer + custom 5-band sliders + presets
- EQ enable/disable + persistentie van instellingen
- Settings screen for alert threshold and auto-refresh
- ANC mode tips (physical button + awareness info)
- Improved Material 3 UI with icons and progress
- Dutch UI labels
- Runtime permission requests
- R8 minify + shrink voor kleinere/snellere release builds

## Notes
- Battery level depends on headset reporting via hidden API / HFP indicators
- Equalizer applies to global audio session (0)
- Requires BLUETOOTH_CONNECT permission on Android 12+
- Runtime permissions requested on start
- ANC/Awareness controlled via physical button (official Philips Headphones app has more advanced modes)

Built with Kotlin + Jetpack Compose.

Version 1.8.0: Device address, battery history, improved tips.
