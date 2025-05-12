package com.example.taskmanager.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.taskmanager.services.NotificationService;
import com.example.taskmanager.utils.SessionManager;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // Kiểm tra xem có người dùng đang đăng nhập không
            SessionManager sessionManager = new SessionManager(context);
            if (sessionManager.isLoggedIn()) {
                // Khởi động dịch vụ thông báo
                Intent serviceIntent = new Intent(context, NotificationService.class);
                context.startService(serviceIntent);
            }
        }
    }
}