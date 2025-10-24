package com.oneearplayer.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    private MediaPlayer mediaPlayer;
    private AudioChannelSwitcher channelSwitcher;
    private Handler handler = new Handler();
    
    private Button btnSelectFile;
    private Button btnPlay;
    private Button btnPause;
    private Button btnStop;
    private SeekBar seekBarPosition;
    private SeekBar seekBarVolume;
    private SeekBar seekBarFrequency;
    private TextView tvFileName;
    private TextView tvCurrentTime;
    private TextView tvDuration;
    private TextView tvFrequency;
    private TextView tvVolume;
    
    private Uri currentFileUri;
    private boolean isPlaying = false;
    private boolean userIsSeeking = false;
    private int switchFrequency = 120;
    
    private ActivityResultLauncher<Intent> filePickerLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        setupListeners();
        checkPermissions();
        
        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        loadAudioFile(uri);
                    }
                }
            }
        );
    }
    
    private void initializeViews() {
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.btnStop);
        seekBarPosition = findViewById(R.id.seekBarPosition);
        seekBarVolume = findViewById(R.id.seekBarVolume);
        seekBarFrequency = findViewById(R.id.seekBarFrequency);
        tvFileName = findViewById(R.id.tvFileName);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvDuration = findViewById(R.id.tvDuration);
        tvFrequency = findViewById(R.id.tvFrequency);
        tvVolume = findViewById(R.id.tvVolume);
        
        btnPlay.setEnabled(false);
        btnPause.setEnabled(false);
        btnStop.setEnabled(false);
        
        seekBarVolume.setProgress(70);
        tvVolume.setText("70%");
        
        seekBarFrequency.setMax(999);
        seekBarFrequency.setProgress(119);
        tvFrequency.setText("120 Hz");
    }
    
    private void setupListeners() {
        btnSelectFile.setOnClickListener(v -> openFilePicker());
        btnPlay.setOnClickListener(v -> playAudio());
        btnPause.setOnClickListener(v -> pauseAudio());
        btnStop.setOnClickListener(v -> stopAudio());
        
        seekBarPosition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    int duration = mediaPlayer.getDuration();
                    int newPosition = (duration * progress) / 100;
                    mediaPlayer.seekTo(newPosition);
                    updateCurrentTime();
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userIsSeeking = true;
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userIsSeeking = false;
            }
        });
        
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null) {
                    float volume = progress / 100f;
                    mediaPlayer.setVolume(volume, volume);
                }
                tvVolume.setText(progress + "%");
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        seekBarFrequency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switchFrequency = progress + 1;
                tvFrequency.setText(switchFrequency + " Hz");
                if (channelSwitcher != null) {
                    channelSwitcher.setFrequency(switchFrequency);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                        PERMISSION_REQUEST_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }
    
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        filePickerLauncher.launch(intent);
    }
    
    private void loadAudioFile(Uri uri) {
        try {
            if (mediaPlayer != null) {
                stopAudio();
                mediaPlayer.release();
            }
            
            currentFileUri = uri;
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepare();
            
            String fileName = getFileName(uri);
            tvFileName.setText(fileName);
            
            int duration = mediaPlayer.getDuration();
            tvDuration.setText(formatTime(duration));
            tvCurrentTime.setText("00:00");
            seekBarPosition.setProgress(0);
            
            btnPlay.setEnabled(true);
            btnPause.setEnabled(true);
            btnStop.setEnabled(true);
            
            mediaPlayer.setOnCompletionListener(mp -> {
                stopAudio();
            });
            
            float volume = seekBarVolume.getProgress() / 100f;
            mediaPlayer.setVolume(volume, volume);
            
            Toast.makeText(this, "Audio file loaded", Toast.LENGTH_SHORT).show();
            
        } catch (IOException e) {
            Toast.makeText(this, "Error loading audio file: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
    
    private void playAudio() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
            
            if (channelSwitcher == null) {
                channelSwitcher = new AudioChannelSwitcher(mediaPlayer, switchFrequency);
            }
            channelSwitcher.start();
            
            updateSeekBar();
            Toast.makeText(this, "Playing", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void pauseAudio() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            
            if (channelSwitcher != null) {
                channelSwitcher.stop();
            }
            
            Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void stopAudio() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.pause();
                isPlaying = false;
            }
            
            if (channelSwitcher != null) {
                channelSwitcher.stop();
                channelSwitcher = null;
            }
            
            mediaPlayer.seekTo(0);
            seekBarPosition.setProgress(0);
            tvCurrentTime.setText("00:00");
            
            Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateSeekBar() {
        if (mediaPlayer != null && isPlaying && !userIsSeeking) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int duration = mediaPlayer.getDuration();
            
            if (duration > 0) {
                int progress = (currentPosition * 100) / duration;
                seekBarPosition.setProgress(progress);
            }
            
            updateCurrentTime();
            
            handler.postDelayed(this::updateSeekBar, 100);
        }
    }
    
    private void updateCurrentTime() {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            tvCurrentTime.setText(formatTime(currentPosition));
        }
    }
    
    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
    
    private String getFileName(Uri uri) {
        String path = uri.getPath();
        if (path != null) {
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash != -1) {
                return path.substring(lastSlash + 1);
            }
        }
        return "Unknown";
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (channelSwitcher != null) {
            channelSwitcher.stop();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
}
