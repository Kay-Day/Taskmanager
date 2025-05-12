package com.example.taskmanager.models;

import java.io.Serializable;

public class TaskHistory implements Serializable {
    private long id;
    private long taskId;
    private long changedBy;
    private String fieldChanged;
    private String oldValue;
    private String newValue;
    private String changedAt;
    private User user; // Thông tin người thay đổi

    // Constructor rỗng
    public TaskHistory() {}

    // Constructor không có ID
    public TaskHistory(long taskId, long changedBy, String fieldChanged, String oldValue, String newValue) {
        this.taskId = taskId;
        this.changedBy = changedBy;
        this.fieldChanged = fieldChanged;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    // Constructor đầy đủ
    public TaskHistory(long id, long taskId, long changedBy, String fieldChanged,
                       String oldValue, String newValue, String changedAt) {
        this.id = id;
        this.taskId = taskId;
        this.changedBy = changedBy;
        this.fieldChanged = fieldChanged;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedAt = changedAt;
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

    public long getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(long changedBy) {
        this.changedBy = changedBy;
    }

    public String getFieldChanged() {
        return fieldChanged;
    }

    public void setFieldChanged(String fieldChanged) {
        this.fieldChanged = fieldChanged;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(String changedAt) {
        this.changedAt = changedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Phương thức trả về mô tả thay đổi
    public String getChangeDescription() {
        return fieldChanged + " đã được thay đổi từ '" + oldValue + "' thành '" + newValue + "'";
    }
}