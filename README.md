# Philips TAH6519 Companion

Modern Android companion app for Philips TAH6519 over-ear ANC headphones.

## Features
- Live connection status & battery monitoring (via bonded device + reflection)
- Auto-refresh on Bluetooth events
- Media controls (active MediaSession)
- System equalizer + BassBoost
- ANC mode tips (physical button)
- Material 3 UI

## Notes
- Battery level depends on headset reporting via hidden API / HFP
- Equalizer applies to global audio session (0)
- Requires BLUETOOTH_CONNECT permission on Android 12+

Built with Kotlin + Jetpack Compose.
