# One Ear Player

Cross-platform audio player that switches stereo output between left and right channels at a configurable frequency (default 120 Hz).

## Overview

One Ear Player creates a unique listening experience by rapidly alternating audio between the left and right stereo channels. This creates an interesting effect where the sound appears to bounce between your ears at the specified frequency.

## Platforms

### Desktop (Linux/Windows)
- Cross-platform Python application with PyQt5 GUI
- Supports MP3, WAV, OGG, FLAC
- Configurable switching frequency (1-1000 Hz)
- See [desktop/README.md](desktop/README.md) for details

### Android
- Native Android application
- Material Design UI
- Supports all common audio formats
- See [android/README.md](android/README.md) for details

## Features

- **Configurable Frequency**: Adjust the channel switching rate from 1 Hz to 1000 Hz (default: 120 Hz)
- **Multiple Audio Formats**: Support for MP3, WAV, OGG, FLAC, and more
- **Full Playback Controls**: Play, pause, stop, and seek functionality
- **Volume Control**: Adjust playback volume
- **Clean UI**: Intuitive interface on all platforms

## Quick Start

### Desktop (Linux/Windows)

```bash
cd desktop
pip install -r requirements.txt
python oneearplayer.py
```

### Android

Open the `android` folder in Android Studio and build the project.

## How It Works

The player rapidly switches the audio output between the left and right stereo channels at the specified frequency. On desktop, this is achieved through precise audio buffer manipulation. On Android, it's done by alternating the volume levels of each channel in real-time.

## Default Settings

- **Switch Frequency**: 120 Hz
- **Volume**: 70%

## License

MIT License

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
