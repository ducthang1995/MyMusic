package com.example.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {

    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;

    private List<Song> playlist = new ArrayList<>();
    private int currentIndex = 0;
    public String currentSongTitle = "No Song";

    private static final String CHANNEL_ID = "music_channel";

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void initMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        mediaPlayer.setOnCompletionListener(mp -> playNextSong());
    }

    public void play(Song song, Runnable onPrepared) {
        if (mediaPlayer == null) initMediaPlayer();

        try {
            mediaPlayer.reset();

            if (song.getResourceId() != 0) {
                mediaPlayer = MediaPlayer.create(this, song.getResourceId());
                currentSongTitle = song.getTitle();
                mediaPlayer.setOnCompletionListener(mp -> playNextSong());
                mediaPlayer.start();
                showNotification();
                if (onPrepared != null) onPrepared.run();
            } else {
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build());
                mediaPlayer.setDataSource(song.getUrl());
                mediaPlayer.setOnPreparedListener(mp -> {
                    currentSongTitle = song.getTitle();
                    mp.start();
                    showNotification();
                    if (onPrepared != null) onPrepared.run();
                });
                mediaPlayer.prepareAsync();
            }

            currentIndex = playlist.indexOf(song);
            if (currentIndex == -1) {
                playlist.add(song);
                currentIndex = playlist.size() - 1;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void addSongToPlaylist(Song song) {
        // Kiểm tra bài hát đã có trong playlist chưa, tránh trùng
        for (Song s : playlist) {
            if (s.getTitle().equals(song.getTitle()) &&
                    ((s.getUrl() != null && s.getUrl().equals(song.getUrl())) ||
                            s.getResourceId() == song.getResourceId())) {
                return; // đã có, không thêm
            }
        }
        playlist.add(song);
    }


    public List<Song> getPlaylist() {
        return new ArrayList<>(playlist);  // trả về bản sao để tránh sửa playlist trực tiếp
    }




    public void playPreviousSong() {
        if (playlist.isEmpty()) return;

        currentIndex--;
        if (currentIndex < 0) currentIndex = playlist.size() - 1;

        play(playlist.get(currentIndex), null);
    }

    public void playNextSong() {
        if (playlist.isEmpty()) return;

        currentIndex++;
        if (currentIndex >= playlist.size()) currentIndex = 0;

        play(playlist.get(currentIndex), null);
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            showNotification();
        }
    }

    public void resume() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            showNotification();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            stopForeground(true);
        }
    }

    public void setPlaylist(List<Song> songs) {
        this.playlist = new ArrayList<>(songs);
    }

    public Song getCurrentSong() {
        if (playlist.isEmpty() || currentIndex < 0 || currentIndex >= playlist.size()) {
            return null;
        }
        return playlist.get(currentIndex);
    }

    private void showNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent prevIntent = new Intent(this, MusicService.class);
        prevIntent.setAction("ACTION_PREVIOUS");
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 3, prevIntent,
                PendingIntent.FLAG_IMMUTABLE);

        Intent playPauseIntent = new Intent(this, MusicService.class);
        playPauseIntent.setAction(isPlaying() ? "ACTION_PAUSE" : "ACTION_PLAY");
        PendingIntent playPausePendingIntent = PendingIntent.getService(this, 1, playPauseIntent,
                PendingIntent.FLAG_IMMUTABLE);

        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction("ACTION_NEXT");
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 2, nextIntent,
                PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Đang phát nhạc")
                .setContentText(currentSongTitle)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
                .addAction(isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        isPlaying() ? "Pause" : "Play",
                        playPausePendingIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
                .setOngoing(isPlaying())
                .build();

        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, "Music Playback", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case "ACTION_PLAY_NEW":
                    String songTitle = intent.getStringExtra("songTitle");
                    String songUrl = intent.getStringExtra("songUrl");
                    int songResId = intent.getIntExtra("songResId", 0);

                    Song newSong = new Song(songTitle, "", songUrl);
                    // Nếu có resourceId, set lại cho chính xác
                    if (songResId != 0) {
                        newSong.setResourceId(songResId);
                    }

                    play(newSong, null);
                    break;

                case "ACTION_PLAY":
                    resume();
                    break;
                case "ACTION_PAUSE":
                    pause();
                    break;
                case "ACTION_PLAY_PAUSE":
                    if (isPlaying()) pause();
                    else resume();
                    break;
                case "ACTION_PREVIOUS":
                    playPreviousSong();
                    break;
                case "ACTION_NEXT":
                    playNextSong();
                    break;
                case "ACTION_STOP":
                    stopSong();
                    stopSelf();
                    break;
            }
        }
        return START_STICKY;
    }

    public String getCurrentSongTitle() {
        return currentSongTitle;
    }




    @Override
    public void onDestroy() {
        stopSong();
        super.onDestroy();
    }
}
