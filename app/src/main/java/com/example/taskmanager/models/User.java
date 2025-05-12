package com.example.taskmanager.models;

import java.io.Serializable;

public class User implements Serializable {
    private long id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String createdAt;
    private int loginAttempts;
    private String lockedUntil;

    // Constructor rỗng
    public User() {}

    // Constructor đầy đủ thông tin không có ID
    public User(String username, String password, String fullName, String email) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.loginAttempts = 0;
        this.lockedUntil = null;
    }

    // Constructor đầy đủ thông tin có ID
    public User(long id, String username, String password, String fullName, String email,
                String createdAt, int loginAttempts, String lockedUntil) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = createdAt;
        this.loginAttempts = loginAttempts;
        this.lockedUntil = lockedUntil;
    }

    // Getters và Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(int loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    public String getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(String lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    // Phương thức kiểm tra tài khoản bị khóa
    public boolean isLocked() {
        if (lockedUntil == null) {
            return false;
        }
        try {
            // Chuyển đổi thời gian khóa từ chuỗi sang long
            long lockedTime = Long.parseLong(lockedUntil);
            // Kiểm tra xem thời gian hiện tại có lớn hơn thời gian khóa không
            return System.currentTimeMillis() < lockedTime;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Tăng số lần đăng nhập thất bại
    public void incrementLoginAttempts() {
        this.loginAttempts++;
    }

    // Reset số lần đăng nhập thất bại
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.lockedUntil = null;
    }

    // Khóa tài khoản trong 24 giờ
    public void lockAccount() {
        // Thời gian hiện tại + 24 giờ (tính bằng mili giây)
        long lockTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
        this.lockedUntil = String.valueOf(lockTime);
    }
}