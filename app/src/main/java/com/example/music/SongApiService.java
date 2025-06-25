package com.example.music;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface SongApiService {
    @GET("songs.json")  // Vì baseUrl đã trỏ sẵn đến thư mục chứa
    Call<List<Song>> getSongs();
}

