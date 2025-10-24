# One Ear Player - Android

Android audio player that switches stereo output between left and right channels at a configurable frequency (default 120 Hz).

## Features

- Play audio files from device storage
- Switch between left and right channels at configurable frequency (1-1000 Hz)
- Default switching frequency: 120 Hz
- Volume control
- Playback controls (play, pause, stop)
- Seek functionality
- Material Design UI

## Requirements

- Android 7.0 (API level 24) or higher
- Android Studio Arctic Fox or later
- Gradle 8.2.0 or later

## Building the App

1. Open the `android` folder in Android Studio
2. Wait for Gradle sync to complete
3. Connect an Android device or start an emulator
4. Click Run or press Shift+F10

### Building APK

To build a release APK:

```bash
cd android
./gradlew assembleRelease
```

The APK will be generated at: `app/build/outputs/apk/release/app-release-unsigned.apk`

### Building AAB (for Google Play)

```bash
cd android
./gradlew bundleRelease
```

The AAB will be generated at: `app/build/outputs/bundle/release/app-release.aab`

## Usage

1. Tap "Open" to select an audio file from your device
2. Use the play/pause/stop buttons to control playback
3. Adjust the switch frequency using the frequency slider (default: 120 Hz)
4. Control volume with the volume slider
5. Seek through the track using the position slider

## Permissions

The app requires the following permissions:
- `READ_EXTERNAL_STORAGE` (Android 12 and below)
- `READ_MEDIA_AUDIO` (Android 13 and above)

These permissions are requested at runtime when you first try to open an audio file.

## How It Works

The player rapidly switches the audio output between the left and right stereo channels at the specified frequency by alternating the volume levels of each channel. This creates a unique listening experience where the audio alternates between ears.

## Supported Audio Formats

- MP3
- WAV
- OGG
- AAC
- FLAC
- M4A

## Platform Support

- Android 7.0 (Nougat) and above
- Tested on Android 10, 11, 12, 13, and 14
