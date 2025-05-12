package com.example.taskmanager.models;

import java.io.Serializable;

public class Notification implements Serializable {
    private long id;
    private long userId;
    private String title;
    private String message;
    private Long relatedId; // ID của đối tượng liên quan (task, project...)
    private String type;
    private boolean isRead;
    private String createdAt;

    public static final String TYPE_PROJECT_ADDED = "project_added";

    // Constructor rỗng
    public Notification() {}

    // Constructor không có ID
    public Notification(long userId, String title, String message, Long relatedId, String type) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.relatedId = relatedId;
        this.type = type;
        this.isRead = false;
    }

    // Constructor đầy đủ
    public Notification(long id, long userId, String title, String message,
                        Long relatedId, String type, boolean isRead, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.relatedId = relatedId;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Getters và Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Các loại thông báo
    public static final String TYPE_NEW_TASK = "new_task";
    public static final String TYPE_TASK_UPDATED = "task_updated";
    public static final String TYPE_TASK_REMINDER = "task_reminder";
    public static final String TYPE_TASK_OVERDUE = "task_overdue";
    public static final String TYPE_TASK_COMPLETED = "task_completed";
}