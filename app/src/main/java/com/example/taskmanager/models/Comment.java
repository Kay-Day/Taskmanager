package com.example.taskmanager.models;

import java.io.Serializable;

public class Comment implements Serializable {
    private long id;
    private long taskId;
    private long userId;
    private String content;
    private String createdAt;
    private User user; // Thông tin người bình luận

    // Constructor rỗng
    public Comment() {}

    // Constructor không có ID
    public Comment(long taskId, long userId, String content) {
        this.taskId = taskId;
        this.userId = userId;
        this.content = content;
    }

    // Constructor đầy đủ
    public Comment(long id, long taskId, long userId, String content, String createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getters và Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}