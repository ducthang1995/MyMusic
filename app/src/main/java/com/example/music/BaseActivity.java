package com.example.music;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {

    // Navigation buttons
    protected ImageButton btnHome, btnRanking, btnSearch, btnPlaylist, btnUser;

    protected MusicService musicService;
    protected boolean isBound = false;
    protected List<Song> currentPlaylist = new ArrayList<>();
    protected int currentSongIndex = 0;

    // Music control buttons
    protected Button btnPrevious, btnPlay, btnNext;
    protected TextView tvSongName;

    // Trạng thái nhạc
    protected boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Không setContentView ở đây, Activity con set layout riêng
    }

    // Phải gọi sau khi Activity con setContentView rồi
    protected void setupNavigationBar() {
        btnHome = findViewById(R.id.btnHome);
        btnRanking = findViewById(R.id.btnRanking);
        btnSearch = findViewById(R.id.btnSearch);
        btnPlaylist = findViewById(R.id.btnPlaylist);
        btnUser = findViewById(R.id.btnUser);

        btnHome.setOnClickListener(v -> openActivity(MainActivity.class));
        btnRanking.setOnClickListener(v -> openActivity(RankingActivity.class));
        btnSearch.setOnClickListener(v -> openActivity(SearchActivity.class));
        btnPlaylist.setOnClickListener(v -> openActivity(PlaylistActivity.class));
        btnUser.setOnClickListener(v -> openActivity(UserActivity.class));
    }

    protected void setupControlButtons() {
        btnPrevious = findViewById(R.id.btnPrevious);
        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        tvSongName = findViewById(R.id.tvSongName);

        btnPrevious.setOnClickListener(v -> onPreviousClicked());
        btnPlay.setOnClickListener(v -> onPlayClicked());
        btnNext.setOnClickListener(v -> onNextClicked());

        updatePlayButtonIcon();
    }

    protected void openActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    // Xử lý nút Previous
    protected void onPreviousClicked() {
        Log.d("BaseActivity", "Previous clicked");
        playPreviousSong();
    }

    // Xử lý nút Play/Pause
    protected void onPlayClicked() {
        Log.d("BaseActivity", "Play clicked");
        if (isPlaying) {
            pauseMusic();
        } else {
            playMusic();
        }
        isPlaying = !isPlaying;
        updatePlayButtonIcon();
    }

    // Xử lý nút Next
    protected void onNextClicked() {
        Log.d("BaseActivity", "Next clicked");
        playNextSong();
    }

    // Các phương thức giả định xử lý nhạc
    protected void playPreviousSong() {
        if (!isBound || musicService == null || currentPlaylist.isEmpty()) return;

        currentSongIndex = (currentSongIndex - 1 + currentPlaylist.size()) % currentPlaylist.size();
        Song previousSong = currentPlaylist.get(currentSongIndex);

        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("ACTION_PLAY_NEW");
        intent.putExtra("songTitle", previousSong.getTitle());
        intent.putExtra("songUrl", previousSong.getUrl());
        intent.putExtra("songResId", previousSong.getResourceId());
        startService(intent);



        Log.d("BaseActivity", "Playing previous song");
        tvSongName.setText("Previous song name");

    }

    protected void playNextSong() {
        if (!isBound || musicService == null || currentPlaylist.isEmpty()) return;

        currentSongIndex = (currentSongIndex + 1) % currentPlaylist.size();
        Song nextSong = currentPlaylist.get(currentSongIndex);

        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("ACTION_PLAY_NEW");
        intent.putExtra("songTitle", nextSong.getTitle());
        intent.putExtra("songUrl", nextSong.getUrl());
        intent.putExtra("songResId", nextSong.getResourceId());
        startService(intent);

        tvSongName.setText(nextSong.getTitle());
        isPlaying = true;
        updatePlayButtonIcon();
    }

    protected void playMusic() {
        if (!isBound || musicService == null) return;

        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("ACTION_PLAY");
        startService(intent);

        Song song = currentPlaylist.get(currentSongIndex);
        tvSongName.setText(song.getTitle());
    }

    protected void pauseMusic() {
        if (!isBound || musicService == null) return;

        Intent intent = new Intent(this, MusicService.class);
        intent.setAction("ACTION_PAUSE");
        startService(intent);

        tvSongName.setText("Music paused");
    }

    // Cập nhật icon nút play theo trạng thái isPlaying
    protected void updatePlayButtonIcon() {
        if (btnPlay != null) {
            btnPlay.setText(isPlaying ? "||" : "▶");
        }
    }
}
