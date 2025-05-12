package com.example.taskmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taskmanager.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1500; // 1.5 giây
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Sử dụng Handler để delay việc chuyển màn hình
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNextScreen, SPLASH_DELAY);
    }

    private void navigateToNextScreen() {
        // Kiểm tra trạng thái đăng nhập
        if (sessionManager.isLoggedIn()) {
            // Người dùng đã đăng nhập, chuyển đến MainActivity
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            // Người dùng chưa đăng nhập, chuyển đến LoginActivity
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }

        // Đóng SplashActivity
        finish();
    }
}