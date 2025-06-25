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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RankingActivity extends BaseActivity implements MusicAdapter.OnItemClickListener {

    private RecyclerView rankingRecyclerView;
    private MusicAdapter musicAdapter;
    private ArrayList<Song> topSongs;

    private ImageView iconFavorite;
    private boolean isFavorite = false;
    private ImageView iconFollow;

    private MusicService musicService;
    private boolean isBound = false;
    private int currentSongIndex = 0;

    // Bottom bar controls
    private Button btnPlay, btnNext, btnPrevious;
    private TextView tvSongName;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
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
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        setupNavigationBar();

        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        // Ánh xạ view
        btnPrevious = findViewById(R.id.btnPrevious);
        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        iconFavorite = findViewById(R.id.iconFavorite);
        iconFollow = findViewById(R.id.iconFollow);
        tvSongName = findViewById(R.id.tvSongName);

        setupBottomBar();
        createNotificationChannel();

        rankingRecyclerView = findViewById(R.id.rankingRecyclerView);
        topSongs = new ArrayList<>();

        topSongs.add(new Song("SOS", "ACTIVICI", R.raw.aaaas));

        musicAdapter = new MusicAdapter(topSongs, this, this);
        rankingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rankingRecyclerView.setAdapter(musicAdapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/dangdong2003/music_data/main/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SongApiService apiService = retrofit.create(SongApiService.class);

        apiService.getSongs().enqueue(new retrofit2.Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    topSongs.addAll(response.body());
                    musicAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(RankingActivity.this, "Không thể tải danh sách online", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Toast.makeText(RankingActivity.this, "Lỗi khi tải nhạc online: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Song selectedSong = topSongs.get(position);
        currentSongIndex = position;

        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("ACTION_PLAY_NEW");
        intent.putExtra("songTitle", selectedSong.getTitle());
        intent.putExtra("songUrl", selectedSong.getUrl());
        intent.putExtra("songResId", selectedSong.getResourceId());
        startService(intent);

        Toast.makeText(this, "Phát: " + selectedSong.getTitle(), Toast.LENGTH_SHORT).show();

        if (tvSongName != null) {
            tvSongName.setText(selectedSong.getTitle());
        }

        btnPlay.setText("||");
        btnPlay.setEnabled(true);
    }

    private void setupBottomBar() {
        btnPlay.setOnClickListener(v -> {
            if (!isBound || musicService == null) return;

            if (musicService.isPlaying()) {
                musicService.pause();
                btnPlay.setText("▶");
            } else {
                musicService.resume();
                btnPlay.setText("||");
            }

            tvSongName.setText(musicService.getCurrentSongTitle());
            updateNotification(this);
        });

        btnNext.setOnClickListener(v -> {
            if (!isBound || musicService == null) return;

            if (currentSongIndex < topSongs.size() - 1) {
                playSong(currentSongIndex + 1);
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (!isBound || musicService == null) return;

            if (currentSongIndex > 0) {
                playSong(currentSongIndex - 1);
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


        iconFollow.setOnClickListener(v -> {
            Song currentSong = musicService.getCurrentSong();
            if (currentSong != null) {

                // Thêm bài hát vào playlist của MusicService
                if (musicService != null && isBound) {
                    musicService.addSongToPlaylist(currentSong);  // sử dụng currentSong vì bạn muốn thêm bài hiện tại
                    Toast.makeText(RankingActivity.this, "Đã thêm bài hát vào playlist", Toast.LENGTH_SHORT).show();
                }

                Intent intent = new Intent(RankingActivity.this, PlaylistActivity.class);
                // Truyền thông tin bài hát qua intent
                intent.putExtra("songTitle", currentSong.getTitle());
                intent.putExtra("songArtist", currentSong.getArtist());
                intent.putExtra("songUrl", currentSong.getUrl());
                intent.putExtra("songResId", currentSong.getResourceId());
                startActivity(intent);
            } else {
                Toast.makeText(RankingActivity.this, "Không có bài hát đang phát", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBottomBarState() {
        if (musicService != null && musicService.getCurrentSongTitle() != null) {
            tvSongName.setText(musicService.getCurrentSongTitle());
            btnPlay.setText(musicService.isPlaying() ? "||" : "▶");
            btnPlay.setEnabled(true);
        }
    }

    private void playSong(int index) {
        if (!isBound || index < 0 || index >= topSongs.size()) return;

        Song song = topSongs.get(index);
        currentSongIndex = index;

        musicService.play(song, () -> {
            runOnUiThread(() -> {
                tvSongName.setText(song.getTitle());
                btnPlay.setEnabled(true);
                btnPlay.setText("||");
                Toast.makeText(RankingActivity.this, "Đang phát: " + song.getTitle(), Toast.LENGTH_SHORT).show();
                updateNotification(this);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

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
