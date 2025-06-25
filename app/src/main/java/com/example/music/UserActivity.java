package com.example.music;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserActivity extends AppCompatActivity {

    private Button btnEditProfile;
    private ImageButton btnSettings;
    private TextView tvRecent,tvMyPlaylists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user); // Gán layout XML

        // Ánh xạ view
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnSettings = findViewById(R.id.btnSettings);
        tvRecent = findViewById(R.id.tvRecent);
        tvMyPlaylists =findViewById(R.id.tvMyPlaylists);

        // Xử lý sự kiện click vào nút "Chỉnh sửa hồ sơ"
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở EditProfileActivity khi nhấn nút
                Intent intent = new Intent(UserActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        tvMyPlaylists.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, PlaylistActivity.class);
            startActivity(intent);
        });


        tvRecent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở EditProfileActivity khi nhấn nút
                Intent intent = new Intent(UserActivity.this, RecentlyPlayedActivity.class);
                startActivity(intent);
            }
        });


    }
}
