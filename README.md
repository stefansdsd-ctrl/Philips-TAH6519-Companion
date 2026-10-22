# Philips TAH6519 Companion

Modern Android companion app for Philips TAH6519 over-ear ANC headphones.

## Features
- Live connection status & battery monitoring (reflection + HF indicators)
- Persistent status notification
- Auto-refresh on Bluetooth events + periodic (15s)
- Media controls with volume slider, live metadata and album art
- System equalizer + BassBoost + custom 5-band sliders + presets (Flat, Bass+, Treble, Rock, Pop, Jazz)
- EQ frequentielabels + persistentie van instellingen
- ANC mode tips (physical button)
- Improved Material 3 UI with icons and progress
- Dutch UI labels
- Runtime permission requests

## Notes
- Battery level depends on headset reporting via hidden API / HFP indicators
- Equalizer applies to global audio session (0)
- Requires BLUETOOTH_CONNECT permission on Android 12+
- Runtime permissions requested on start

Built with Kotlin + Jetpack Compose.

Version 1.5.0: EQ labels + persistent settings.
