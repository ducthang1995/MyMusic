package com.example.music;

import android.os.Bundle;
import android.widget.TextView;

public class SongInfoActivity extends BaseActivity {

    private TextView tvTitle, tvArtist, tvAlbum, tvYear, tvLyrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_info);

        tvTitle = findViewById(R.id.tvSongTitle);
        tvArtist = findViewById(R.id.tvSongArtist);
        tvAlbum = findViewById(R.id.tvSongAlbum);
        tvYear = findViewById(R.id.tvSongYear);
        tvLyrics = findViewById(R.id.tvLyrics);

        // TODO: Lấy dữ liệu bài hát hiện tại (có thể qua Intent extras) và hiển thị ở đây

        setupNavigationBar();
    }
}
