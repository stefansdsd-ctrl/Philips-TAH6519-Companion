# Philips TAH6519 Companion

Modern Android companion app for Philips TAH6519 over-ear ANC headphones.

## Features
- Live connection status & battery monitoring (reflection + HF indicators)
- Device MAC address display
- Battery history (last measurements) + visuele grafiek
- Persistent status notification + configurable low battery alert
- Auto-refresh on Bluetooth events + periodic (15s, toggleable)
- Media controls with volume slider, live metadata, album art en seekbar/progress
- System equalizer + BassBoost + Virtualizer + LoudnessEnhancer
- EQ-curve (5 banden) + 12 presets (Flat, Bass+, Treble, Rock, Pop, Jazz, Hip Hop, Dance, Classical, Acoustic, Vocal, Podcast)
- EQ enable/disable + persistentie van instellingen (inclusief loudness)
- Settings screen for alert threshold and auto-refresh
- ANC mode tips (physical button + awareness info)
- Improved Material 3 UI with icons and progress
- Dutch UI labels
- Runtime permission requests
- R8 minify + shrink voor kleinere/snellere release builds

## Notes
- Battery level depends on headset reporting via hidden API / HFP indicators
- Equalizer, bass, virtualizer and loudness apply to global audio session (0)
- Loudness range is 0 to +15 dB; some devices do not expose LoudnessEnhancer
- Requires BLUETOOTH_CONNECT permission on Android 12+
- Runtime permissions requested on start
- ANC/Awareness controlled via physical button (official Philips Headphones app has more advanced modes)

Built with Kotlin + Jetpack Compose.

Version 2.0.0: Loudness booster, EQ-curve en extra presets.
