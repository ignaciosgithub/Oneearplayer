@echo off
echo Building One Ear Player for Windows...

pip install -r requirements.txt

pyinstaller --onefile --windowed --name="OneEarPlayer" --icon=NONE oneearplayer.py

echo Build complete! Executable is in dist/OneEarPlayer.exe
pause
