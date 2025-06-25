package com.example.music;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity {

    private EditText etSearch;
    private RecyclerView searchRecyclerView;
    private MusicAdapter adapter;
    private List<Song> allSongs;
    private List<Song> filteredSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etSearch = findViewById(R.id.etSearch);
        searchRecyclerView = findViewById(R.id.searchRecyclerView);

        // Dữ liệu giả lập ban đầu
        allSongs = generateDummySongs();
        filteredSongs = new ArrayList<>(allSongs);

        adapter = new MusicAdapter(filteredSongs, this, position -> {
            // TODO: Xử lý khi click vào một bài hát (nếu cần)
        });

        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchRecyclerView.setAdapter(adapter);

        // Lắng nghe thay đổi trong ô tìm kiếm
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSongs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        setupNavigationBar();
    }

    // Lọc danh sách bài hát theo từ khóa
    private void filterSongs(String keyword) {
        filteredSongs.clear();
        for (Song song : allSongs) {
            if (song.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                    song.getArtist().toLowerCase().contains(keyword.toLowerCase())) {
                filteredSongs.add(song);
            }
        }
        adapter.notifyDataSetChanged();
    }

    // Tạo danh sách bài hát giả lập
    private List<Song> generateDummySongs() {
        List<Song> songs = new ArrayList<>();
        songs.add(new Song("Lạc Trôi", "Sơn Tùng", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"));
        songs.add(new Song("Em Gái Mưa", "Hương Tràm", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"));
        songs.add(new Song("Nơi Này Có Anh", "Sơn Tùng", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"));
        songs.add(new Song("Dreams", "Bensound", "https://www.bensound.com/bensound-music/bensound-dreams.mp3"));
        songs.add(new Song("My Offline Song 1", "My Artist 1", R.raw.aaaas));
        songs.add(new Song("My Offline Song 2", "My Artist 2", R.raw.talacuanhau));
        songs.add(new Song("My Offline Song 3", "My Artist 3", R.raw.thefatrat));
        songs.add(new Song("Sunny", "KODOMOi", "https://files.freemusicarchive.org/storage-freemusicarchive-org/music/no_curator/KODOMOi/Free_BGM_Vol1/KODOMOi_-_Sunny.mp3"));
        return songs;
    }
}
