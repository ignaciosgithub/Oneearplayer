#!/bin/bash
echo "Building One Ear Player for Linux..."

pip install -r requirements.txt

pyinstaller --onefile --windowed --name="OneEarPlayer" oneearplayer.py

echo "Build complete! Executable is in dist/OneEarPlayer"
