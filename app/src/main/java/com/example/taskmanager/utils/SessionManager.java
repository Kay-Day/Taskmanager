package com.example.taskmanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.taskmanager.models.User;

public class SessionManager {
    private static final String PREF_NAME = "TaskManagerPref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_EMAIL = "email";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    // Constructor
    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Tạo phiên đăng nhập
    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_FULL_NAME, user.getFullName());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.commit();
    }

    // Lấy thông tin người dùng đang đăng nhập
    public User getUserDetails() {
        if (!isLoggedIn()) {
            return null;
        }

        User user = new User();
        user.setId(sharedPreferences.getLong(KEY_USER_ID, -1));
        user.setUsername(sharedPreferences.getString(KEY_USERNAME, null));
        user.setFullName(sharedPreferences.getString(KEY_FULL_NAME, null));
        user.setEmail(sharedPreferences.getString(KEY_EMAIL, null));

        return user;
    }

    // Lấy ID người dùng đang đăng nhập
    public long getUserId() {
        return sharedPreferences.getLong(KEY_USER_ID, -1);
    }

    // Kiểm tra người dùng đã đăng nhập chưa
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Đăng xuất
    public void logout() {
        editor.clear();
        editor.commit();
    }
}