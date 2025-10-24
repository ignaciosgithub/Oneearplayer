# One Ear Player - Desktop

Cross-platform audio player that switches stereo output between left and right channels at a configurable frequency (default 120 Hz).

## Features

- Play audio files (MP3, WAV, OGG, FLAC)
- Switch between left and right channels at configurable frequency (1-1000 Hz)
- Default switching frequency: 120 Hz
- Volume control
- Playback controls (play, pause, stop)
- Seek functionality
- Clean, intuitive GUI

## Requirements

- Python 3.8 or higher
- PyQt5
- pygame
- numpy

## Installation

### From Source

1. Install Python dependencies:
```bash
pip install -r requirements.txt
```

2. Run the application:
```bash
python oneearplayer.py
```

### Building Executables

#### Windows

Run the build script:
```cmd
build_windows.bat
```

The executable will be created in `dist/OneEarPlayer.exe`

#### Linux

Run the build script:
```bash
./build_linux.sh
```

The executable will be created in `dist/OneEarPlayer`

## Usage

1. Click the folder icon to open an audio file
2. Use the play/pause/stop buttons to control playback
3. Adjust the switch frequency using the spinbox (default: 120 Hz)
4. Control volume with the volume slider
5. Seek through the track using the position slider

## How It Works

The player rapidly switches the audio output between the left and right stereo channels at the specified frequency. This creates a unique listening experience where the audio alternates between ears.

## Supported Audio Formats

- MP3
- WAV
- OGG
- FLAC

## Platform Support

- Windows 10/11
- Linux (Ubuntu, Debian, Fedora, etc.)
- Any platform that supports Python 3.8+ and the required dependencies
