package com.example.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Intent serviceIntent = new Intent(context, MusicService.class);

        if ("ACTION_PLAY_PAUSE".equals(action)) {
            // Gửi action PLAY hoặc PAUSE tùy trạng thái hiện tại
            // Giả sử bạn lưu trạng thái chơi nhạc ở đâu đó hoặc dùng service trực tiếp

            // Đơn giản gửi 2 action, service tự kiểm tra và xử lý
            serviceIntent.setAction("ACTION_PLAY_PAUSE");
            context.startService(serviceIntent);
        } else if ("ACTION_NEXT".equals(action)) {
            serviceIntent.setAction("ACTION_NEXT");
            context.startService(serviceIntent);
        } else if ("ACTION_PREVIOUS".equals(action)) {
            serviceIntent.setAction("ACTION_PREVIOUS");
            context.startService(serviceIntent);
        } else if ("ACTION_STOP".equals(action)) {
            serviceIntent.setAction("ACTION_STOP");
            context.startService(serviceIntent);
        }
    }
}
