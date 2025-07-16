# VibraLux Android App

A Kotlin-based Android application to control and monitor ESP32 devices for earthquake detection and smart lighting.



## Features

- 🔔 Real-time earthquake alerts using Firebase Realtime Database
- 💡 Smart lighting control (Auto, Manual, Schedule)
- 📶 Device connection via QR code and local HTTP setup
- 📡 Realtime status updates (WiFi, lamp, vibration)
- 🔒 Foreground service for critical alerts (even in Do Not Disturb mode)
- 📱 Multiple device support with intuitive UI

## Tech Stack

- Kotlin + Jetpack Compose
- Firebase Realtime Database
- WifiNetworkSpecifier (Android 13+)
- QR Code Scanner (ZXing)

## Screenshots

_To be added_

## Setup

1. Clone the repo
2. Open in Android Studio
3. Set minimum SDK to **Android 13 (API 33)**
4. Add your Firebase config in `google-services.json`
5. Build & Run

## Related Repositories

This app is designed to work with the following firmware:
👉 **[VibraLux ESP32 Firmware](https://github.com/bintangalsyahadat/VibraLux-ESP32)**

> Make sure your ESP32 is flashed with the latest firmware and connected to Firebase with the correct credentials.

## License

MIT