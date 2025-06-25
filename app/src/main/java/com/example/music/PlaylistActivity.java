package com.example.music;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlaylistActivity extends BaseActivity implements MusicAdapter.OnItemClickListener {

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


            playlistSongs.clear();
            playlistSongs.addAll(musicService.getPlaylist());  // cần tạo hàm getPlaylist() trong MusicService

            playlistAdapter.notifyDataSetChanged();

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        setupNavigationBar();

        // Khởi động và bind tới MusicService
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        // Bottom bar setup
        setupBottomBar();

        // Danh sách bài hát

        playlistSongs = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        playlistAdapter = new MusicAdapter(playlistSongs, this, this);
        recyclerView.setAdapter(playlistAdapter);

        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onItemClick(int position) {
        Song song = playlistSongs.get(position);

        // Gửi intent tới MusicService để phát bài hát mới
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("ACTION_PLAY_NEW");
        intent.putExtra("songTitle", song.getTitle());
        intent.putExtra("songUrl", song.getUrl());
        intent.putExtra("songResId", song.getResourceId());
        startService(intent);

        tvSongName.setText(song.getTitle());
        btnPlay.setText("||");


        Toast.makeText(this, "Đang phát: " + song.getTitle(), Toast.LENGTH_SHORT).show();
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


    // ================= Notification ======================

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "music_channel",
                    "Thông báo phát nhạc",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void updateNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    1001
            );
            return;
        }

        boolean isPlaying = musicService != null && musicService.isPlaying();
        Song currentSong = musicService != null ? musicService.getCurrentSong() : null;

        if (currentSong == null) return;

        Intent intent = new Intent(context, NotificationActionReceiver.class);
        intent.setAction("ACTION_PLAY_PAUSE");

        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                0,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "music_channel")
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentTitle(currentSong.getTitle())
                .setContentText("Đang phát: " + currentSong.getArtist())
                .addAction(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow,
                        isPlaying ? "Tạm dừng" : "Phát", pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(isPlaying);

        NotificationManagerCompat.from(context).notify(1, builder.build());
    }
}
