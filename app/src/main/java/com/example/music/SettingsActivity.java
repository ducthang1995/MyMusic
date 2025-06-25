package com.example.music;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Spinner;

import java.util.ArrayList;

public class SettingsActivity extends BaseActivity {

    private Switch switchNotification;
    private Spinner spinnerQuality;


    private ArrayList<Song> playlistSongs;
    private MusicAdapter playlistAdapter;

    private MusicService musicService;

    private ImageView iconFavorite;
    private boolean isFavorite = false;
    private boolean isBound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;

            // Cập nhật tên bài hát ngay sau khi kết nối service
            updateCurrentSongInfo();
            updateBottomBarState();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentSongInfo();
    }

    private void updateCurrentSongInfo() {
        if (musicService != null) {
            Song currentSong = musicService.getCurrentSong();
            if (currentSong != null) {
                tvSongName.setText(currentSong.getTitle());
                // Nếu có thêm artist hoặc ảnh nhạc thì cập nhật ở đây
            }
        }
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupNavigationBar();


        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        // Bottom bar setup
        setupBottomBar();

        switchNotification = findViewById(R.id.switchNotifications);
        spinnerQuality = findViewById(R.id.spinnerQuality);



    }


    private void setupBottomBar() {
        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        iconFavorite = findViewById(R.id.iconFavorite);

        tvSongName = findViewById(R.id.tvSongName);

        btnPlay.setOnClickListener(v -> {
            if (musicService != null) {
                if (musicService.isPlaying()) {
                    musicService.pause();
                    btnPlay.setText("▶");
                } else {
                    musicService.resume();
                    btnPlay.setText("||");
                }

                tvSongName.setText(musicService.getCurrentSongTitle());
            }
        });

        btnNext.setOnClickListener(v -> {
            if (musicService != null) {
                musicService.playNextSong();
                tvSongName.setText(musicService.getCurrentSongTitle());
                btnPlay.setText("||");
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (musicService != null) {
                musicService.playPreviousSong();
                tvSongName.setText(musicService.getCurrentSongTitle());
                btnPlay.setText("||");
            }
        });


        iconFavorite.setOnClickListener(v -> {
            // Đảo trạng thái
            isFavorite = !isFavorite;

            // Đổi màu icon (hoặc đổi hình)
            if (isFavorite) {
                iconFavorite.setColorFilter(Color.RED); // yêu thích
            } else {
                iconFavorite.setColorFilter(Color.WHITE); // không yêu thích
            }

            // Áp dụng hiệu ứng rung
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            iconFavorite.startAnimation(shake);
        });
    }

    private void updateBottomBarState() {
        if (musicService != null && musicService.getCurrentSongTitle() != null) {
            tvSongName.setText(musicService.getCurrentSongTitle());
            btnPlay.setText(musicService.isPlaying() ? "||" : "▶");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }
}
