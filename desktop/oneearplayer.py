#!/usr/bin/env python3
"""
Oneearplayer - Audio player that switches stereo output between left and right channels
at a configurable frequency (default 120 Hz)
"""

import sys
import os
import threading
import time
from pathlib import Path
from PyQt5.QtWidgets import (QApplication, QMainWindow, QWidget, QVBoxLayout, 
                             QHBoxLayout, QPushButton, QLabel, QSlider, 
                             QFileDialog, QStyle, QSpinBox)
from PyQt5.QtCore import Qt, QTimer, pyqtSignal, QObject
from PyQt5.QtGui import QFont
import pygame
import numpy as np


class AudioPlayer(QObject):
    position_changed = pyqtSignal(int)
    duration_changed = pyqtSignal(int)
    playback_finished = pyqtSignal()
    
    def __init__(self):
        super().__init__()
        pygame.mixer.init(frequency=44100, size=-16, channels=2, buffer=512)
        self.current_file = None
        self.is_playing = False
        self.is_paused = False
        self.switch_frequency = 120
        self.duration = 0
        self.position = 0
        self.switch_thread = None
        self.stop_switching = False
        self.current_channel = 0
        self.sound = None
        self.channel = None
        self.base_volume = 0.7
        
    def load_file(self, filepath):
        try:
            self.stop()
            self.current_file = filepath
            
            self.sound = pygame.mixer.Sound(filepath)
            self.duration = int(self.sound.get_length() * 1000)
            self.duration_changed.emit(self.duration)
            
            return True
        except Exception as e:
            print(f"Error loading file: {e}")
            return False
    
    def switch_channels_thread(self):
        switch_interval = 1.0 / self.switch_frequency
        
        while not self.stop_switching and self.is_playing:
            if not self.is_paused and self.channel:
                self.current_channel = 1 - self.current_channel
                
                if self.current_channel == 0:
                    self.channel.set_volume(self.base_volume, 0.0)
                else:
                    self.channel.set_volume(0.0, self.base_volume)
            
            time.sleep(switch_interval)
    
    def play(self):
        if self.sound is None:
            return
        
        if self.is_paused and self.channel:
            self.channel.unpause()
            self.is_paused = False
            self.is_playing = True
            
            if not self.switch_thread or not self.switch_thread.is_alive():
                self.stop_switching = False
                self.switch_thread = threading.Thread(target=self.switch_channels_thread, daemon=True)
                self.switch_thread.start()
        else:
            self.channel = self.sound.play()
            if self.channel:
                self.channel.set_volume(self.base_volume, 0.0)
                self.is_playing = True
                self.is_paused = False
                
                self.stop_switching = False
                self.switch_thread = threading.Thread(target=self.switch_channels_thread, daemon=True)
                self.switch_thread.start()
    
    def pause(self):
        if self.is_playing and not self.is_paused and self.channel:
            self.channel.pause()
            self.is_paused = True
    
    def stop(self):
        self.stop_switching = True
        if self.switch_thread:
            self.switch_thread.join(timeout=1)
        
        if self.channel:
            self.channel.stop()
        
        self.is_playing = False
        self.is_paused = False
        self.position = 0
        self.position_changed.emit(0)
    
    def set_position(self, position_ms):
        if self.sound:
            was_playing = self.is_playing and not self.is_paused
            self.stop()
            
            position_sec = position_ms / 1000.0
            self.channel = self.sound.play(maxtime=int((self.duration - position_ms)))
            
            if self.channel:
                self.channel.set_volume(self.base_volume, 0.0)
                
                if not was_playing:
                    self.channel.pause()
                    self.is_paused = True
                
                self.is_playing = True
                self.position = position_ms
                
                if was_playing:
                    self.stop_switching = False
                    self.switch_thread = threading.Thread(target=self.switch_channels_thread, daemon=True)
                    self.switch_thread.start()
    
    def set_volume(self, volume):
        self.base_volume = volume / 100.0
        if self.channel and self.is_playing:
            if self.current_channel == 0:
                self.channel.set_volume(self.base_volume, 0.0)
            else:
                self.channel.set_volume(0.0, self.base_volume)
    
    def set_switch_frequency(self, frequency):
        self.switch_frequency = frequency


class OneEarPlayerWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.player = AudioPlayer()
        self.init_ui()
        self.setup_connections()
        
        self.update_timer = QTimer()
        self.update_timer.timeout.connect(self.update_position)
        self.update_timer.start(100)
        
        self.playback_start_time = 0
        self.playback_offset = 0
    
    def init_ui(self):
        self.setWindowTitle('One Ear Player')
        self.setGeometry(100, 100, 600, 300)
        
        central_widget = QWidget()
        self.setCentralWidget(central_widget)
        
        layout = QVBoxLayout()
        central_widget.setLayout(layout)
        
        title_label = QLabel('One Ear Player')
        title_font = QFont()
        title_font.setPointSize(16)
        title_font.setBold(True)
        title_label.setFont(title_font)
        title_label.setAlignment(Qt.AlignCenter)
        layout.addWidget(title_label)
        
        self.file_label = QLabel('No file loaded')
        self.file_label.setAlignment(Qt.AlignCenter)
        layout.addWidget(self.file_label)
        
        freq_layout = QHBoxLayout()
        freq_label = QLabel('Switch Frequency (Hz):')
        freq_layout.addWidget(freq_label)
        
        self.freq_spinbox = QSpinBox()
        self.freq_spinbox.setMinimum(1)
        self.freq_spinbox.setMaximum(1000)
        self.freq_spinbox.setValue(120)
        self.freq_spinbox.valueChanged.connect(self.on_frequency_changed)
        freq_layout.addWidget(self.freq_spinbox)
        
        freq_layout.addStretch()
        layout.addLayout(freq_layout)
        
        position_layout = QHBoxLayout()
        self.position_label = QLabel('00:00')
        position_layout.addWidget(self.position_label)
        
        self.position_slider = QSlider(Qt.Horizontal)
        self.position_slider.setMinimum(0)
        self.position_slider.setMaximum(1000)
        self.position_slider.sliderPressed.connect(self.on_slider_pressed)
        self.position_slider.sliderReleased.connect(self.on_slider_released)
        position_layout.addWidget(self.position_slider)
        
        self.duration_label = QLabel('00:00')
        position_layout.addWidget(self.duration_label)
        
        layout.addLayout(position_layout)
        
        controls_layout = QHBoxLayout()
        
        self.open_button = QPushButton()
        self.open_button.setIcon(self.style().standardIcon(QStyle.SP_DirOpenIcon))
        self.open_button.clicked.connect(self.open_file)
        controls_layout.addWidget(self.open_button)
        
        self.play_button = QPushButton()
        self.play_button.setIcon(self.style().standardIcon(QStyle.SP_MediaPlay))
        self.play_button.clicked.connect(self.play)
        self.play_button.setEnabled(False)
        controls_layout.addWidget(self.play_button)
        
        self.pause_button = QPushButton()
        self.pause_button.setIcon(self.style().standardIcon(QStyle.SP_MediaPause))
        self.pause_button.clicked.connect(self.pause)
        self.pause_button.setEnabled(False)
        controls_layout.addWidget(self.pause_button)
        
        self.stop_button = QPushButton()
        self.stop_button.setIcon(self.style().standardIcon(QStyle.SP_MediaStop))
        self.stop_button.clicked.connect(self.stop)
        self.stop_button.setEnabled(False)
        controls_layout.addWidget(self.stop_button)
        
        layout.addLayout(controls_layout)
        
        volume_layout = QHBoxLayout()
        volume_label = QLabel('Volume:')
        volume_layout.addWidget(volume_label)
        
        self.volume_slider = QSlider(Qt.Horizontal)
        self.volume_slider.setMinimum(0)
        self.volume_slider.setMaximum(100)
        self.volume_slider.setValue(70)
        self.volume_slider.valueChanged.connect(self.on_volume_changed)
        volume_layout.addWidget(self.volume_slider)
        
        self.volume_label = QLabel('70%')
        volume_layout.addWidget(self.volume_label)
        
        layout.addLayout(volume_layout)
        
        info_label = QLabel('Switches stereo output between left and right channels')
        info_label.setAlignment(Qt.AlignCenter)
        info_label.setStyleSheet('color: gray; font-size: 10px;')
        layout.addWidget(info_label)
    
    def setup_connections(self):
        self.player.position_changed.connect(self.on_position_changed)
        self.player.duration_changed.connect(self.on_duration_changed)
        self.player.playback_finished.connect(self.on_playback_finished)
    
    def open_file(self):
        filename, _ = QFileDialog.getOpenFileName(
            self,
            "Open Audio File",
            "",
            "Audio Files (*.mp3 *.wav *.ogg *.flac);;All Files (*.*)"
        )
        
        if filename:
            if self.player.load_file(filename):
                self.file_label.setText(Path(filename).name)
                self.play_button.setEnabled(True)
                self.pause_button.setEnabled(True)
                self.stop_button.setEnabled(True)
                self.playback_start_time = 0
                self.playback_offset = 0
    
    def play(self):
        if not self.player.is_playing or self.player.is_paused:
            self.playback_start_time = time.time()
        self.player.play()
    
    def pause(self):
        if self.player.is_playing and not self.player.is_paused:
            elapsed = (time.time() - self.playback_start_time) * 1000
            self.playback_offset += elapsed
        self.player.pause()
    
    def stop(self):
        self.player.stop()
        self.playback_start_time = 0
        self.playback_offset = 0
    
    def on_volume_changed(self, value):
        self.player.set_volume(value)
        self.volume_label.setText(f'{value}%')
    
    def on_frequency_changed(self, value):
        self.player.set_switch_frequency(value)
    
    def on_position_changed(self, position):
        if not self.position_slider.isSliderDown():
            if self.player.duration > 0:
                slider_value = int((position / self.player.duration) * 1000)
                self.position_slider.setValue(slider_value)
            self.position_label.setText(self.format_time(position))
    
    def on_duration_changed(self, duration):
        self.duration_label.setText(self.format_time(duration))
    
    def on_playback_finished(self):
        self.stop()
    
    def on_slider_pressed(self):
        pass
    
    def on_slider_released(self):
        if self.player.duration > 0:
            position = int((self.position_slider.value() / 1000.0) * self.player.duration)
            self.player.set_position(position)
            self.playback_offset = position
            self.playback_start_time = time.time()
    
    def update_position(self):
        if self.player.is_playing and not self.player.is_paused:
            elapsed = (time.time() - self.playback_start_time) * 1000
            current_pos = int(self.playback_offset + elapsed)
            
            if current_pos >= self.player.duration:
                self.stop()
            else:
                self.on_position_changed(current_pos)
    
    def format_time(self, milliseconds):
        seconds = milliseconds // 1000
        minutes = seconds // 60
        seconds = seconds % 60
        return f'{minutes:02d}:{seconds:02d}'
    
    def closeEvent(self, event):
        self.player.stop()
        pygame.mixer.quit()
        event.accept()


def main():
    app = QApplication(sys.argv)
    window = OneEarPlayerWindow()
    window.show()
    sys.exit(app.exec_())


if __name__ == '__main__':
    main()
