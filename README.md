# Philips TAH6519 Companion

Modern Android companion app for Philips TAH6519 over-ear ANC headphones.

## Features
- Live connection status & battery monitoring (reflection + HF indicators)
- Persistent status notification
- Auto-refresh on Bluetooth events + periodic
- Media controls with volume slider, live metadata and album art
- System equalizer + BassBoost presets (Bass+, Flat, Vocal, Treble, Music, Podcast)
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

Version 1.3.0: Better battery (HF indicators), permissions, album art, extra EQ presets.
