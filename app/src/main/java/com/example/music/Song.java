package com.example.music;

import java.io.Serializable;
import java.util.Objects;

public class Song implements Serializable {
    private String title;
    private String artist;
    private String url;       // Dùng cho nhạc online
    private int resourceId;   // Dùng cho nhạc offline (res/raw)



    public Song() {}     // Bắt buộc với Retrofit + Gson

    // Constructor cho nhạc online
    public Song(String title, String artist, String url) {
        this.title = title;
        this.artist = artist;
        this.url = url;
        this.resourceId = 0;
    }

    // Constructor cho nhạc offline
    public Song(String title, String artist, int resourceId) {
        this.title = title;
        this.artist = artist;
        this.resourceId = resourceId;
        this.url = null;
    }

    // Getter
    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getUrl() {
        return url;
    }

    public int getResourceId() {
        return resourceId;
    }

    public boolean isOnline() {
        return url != null && !url.isEmpty();
    }

    public boolean isLocal() {
        return resourceId != 0;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Song song = (Song) obj;

        if (resourceId != 0 && song.resourceId != 0) {
            return resourceId == song.resourceId;
        }

        return url != null && url.equals(song.url);
    }

    @Override
    public int hashCode() {
        if (resourceId != 0) {
            return Integer.hashCode(resourceId);
        } else if (url != null) {
            return url.hashCode();
        } else {
            return 0;
        }
    }
}
