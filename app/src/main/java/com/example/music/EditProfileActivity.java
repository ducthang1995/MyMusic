package com.example.music;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgEditAvatar;
    private EditText edtUserName, edtEmail, edtDescription;
    private Button btnChangeAvatar, btnSaveProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile); // Đảm bảo tên file XML là activity_edit_profile.xml

        // Ánh xạ các thành phần
        imgEditAvatar = findViewById(R.id.imgEditAvatar);
        edtUserName = findViewById(R.id.edtUserName);
        edtEmail = findViewById(R.id.edtEmail);
        edtDescription = findViewById(R.id.edtDescription);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Sự kiện nút "Thay đổi ảnh đại diện"
        btnChangeAvatar.setOnClickListener(v -> {
            // TODO: Xử lý thay đổi ảnh đại diện (ví dụ: mở thư viện hoặc chụp ảnh)
            Toast.makeText(this, "Chức năng thay ảnh chưa triển khai", Toast.LENGTH_SHORT).show();
        });

        // Sự kiện nút "Lưu thay đổi"
        btnSaveProfile.setOnClickListener(v -> {
            String name = edtUserName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String desc = edtDescription.getText().toString().trim();

            // TODO: Lưu thông tin vào cơ sở dữ liệu hoặc SharedPreferences
            Toast.makeText(this, "Đã lưu hồ sơ cho: " + name, Toast.LENGTH_SHORT).show();
            finish(); // Quay về màn hình trước
        });
    }
}
