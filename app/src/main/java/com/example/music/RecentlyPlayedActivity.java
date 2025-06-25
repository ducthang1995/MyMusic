package com.example.music;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecentlyPlayedActivity extends BaseActivity implements MusicAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private MusicAdapter musicAdapter;
    private List<Song> recentSongs;



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
                // Nếu có thêm artist hoặc ảnh nhạc thì cập nhật ở đây
            }
        }
    }








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recently_played);
        setupNavigationBar();


        // Khởi động và bind tới MusicService
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        // Bottom bar setup
        setupBottomBar();


        recyclerView = findViewById(R.id.recyclerRecent);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tạo danh sách nhạc giả lập
        recentSongs = new ArrayList<>();
        recentSongs.add(new Song("Nhạc Onlne 1", "Sơn Tùng", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"));
        recentSongs.add(new Song("Nhạc Onlne 2", "Hương Tràm", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"));
        recentSongs.add(new Song("Nhạc Onlne 3", "Sơn Tùng", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"));
        recentSongs.add(new Song("Nhạc Onlne 4", "Bensound", "https://www.bensound.com/bensound-music/bensound-dreams.mp3"));
        recentSongs.add(new Song("Nhạc Onlne 5", "KODOMOi", "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/no_curator/KODOMOi/Free_BGM_Vol1/KODOMOi_-_Sunny.mp3"));
        recentSongs.add(new Song("My Offline Song 1", "My Artist 1", R.raw.aaaas));
        recentSongs.add(new Song("My Offline Song 2", "My Artist 2", R.raw.talacuanhau));
        recentSongs.add(new Song("My Offline Song 3", "My Artist 3", R.raw.thefatrat));

        musicAdapter = new MusicAdapter(recentSongs, this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(musicAdapter);


    }


    @Override
    public void onItemClick(int position) {
        Song selectedSong = recentSongs.get(position);
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
    }


        private void setupBottomBar() {
            btnPlay = findViewById(R.id.btnPlay);
            btnNext = findViewById(R.id.btnNext);
            btnPrevious = findViewById(R.id.btnPrevious);
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