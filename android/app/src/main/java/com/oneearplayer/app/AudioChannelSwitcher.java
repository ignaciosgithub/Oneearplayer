package com.oneearplayer.app;

import android.media.MediaPlayer;
import android.media.audiofx.Virtualizer;
import android.os.Handler;
import android.os.Looper;

public class AudioChannelSwitcher {
    
    private MediaPlayer mediaPlayer;
    private int frequency;
    private boolean isRunning = false;
    private Handler handler;
    private Runnable switchRunnable;
    private boolean currentChannel = false;
    
    public AudioChannelSwitcher(MediaPlayer mediaPlayer, int frequency) {
        this.mediaPlayer = mediaPlayer;
        this.frequency = frequency;
        this.handler = new Handler(Looper.getMainLooper());
    }
    
    public void start() {
        if (isRunning) {
            return;
        }
        
        isRunning = true;
        switchRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning && mediaPlayer != null && mediaPlayer.isPlaying()) {
                    switchChannel();
                    
                    int delayMillis = 1000 / frequency;
                    handler.postDelayed(this, delayMillis);
                }
            }
        };
        
        handler.post(switchRunnable);
    }
    
    public void stop() {
        isRunning = false;
        if (handler != null && switchRunnable != null) {
            handler.removeCallbacks(switchRunnable);
        }
        
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(1.0f, 1.0f);
        }
    }
    
    private void switchChannel() {
        if (mediaPlayer == null) {
            return;
        }
        
        currentChannel = !currentChannel;
        
        if (currentChannel) {
            mediaPlayer.setVolume(1.0f, 0.0f);
        } else {
            mediaPlayer.setVolume(0.0f, 1.0f);
        }
    }
    
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
