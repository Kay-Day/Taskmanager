package com.example.taskmanager.models;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Task implements Serializable {
    private long id;
    private long projectId;
    private String title;
    private String description;
    private Long assignedTo;
    private long createdBy;
    private String priority;
    private String status;
    private String resourceLink;
    private String startDate;
    private String dueDate;
    private String createdAt;
    private List<Comment> comments;
    private List<TaskHistory> history;

    // Constructor rỗng
    public Task() {
        this.comments = new ArrayList<>();
        this.history = new ArrayList<>();
    }

    // Constructor không có ID và danh sách bình luận, lịch sử
    public Task(long projectId, String title, String description, Long assignedTo, long createdBy,
                String priority, String status, String resourceLink, String startDate, String dueDate) {
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.assignedTo = assignedTo;
        this.createdBy = createdBy;
        this.priority = priority;
        this.status = status;
        this.resourceLink = resourceLink;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.comments = new ArrayList<>();
        this.history = new ArrayList<>();
    }

    // Constructor đầy đủ
    public Task(long id, long projectId, String title, String description, Long assignedTo, long createdBy,
                String priority, String status, String resourceLink, String startDate, String dueDate, String createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.assignedTo = assignedTo;
        this.createdBy = createdBy;
        this.priority = priority;
        this.status = status;
        this.resourceLink = resourceLink;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.comments = new ArrayList<>();
        this.history = new ArrayList<>();
    }

    // Getters và Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResourceLink() {
        return resourceLink;
    }

    public void setResourceLink(String resourceLink) {
        this.resourceLink = resourceLink;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<TaskHistory> getHistory() {
        return history;
    }

    public void setHistory(List<TaskHistory> history) {
        this.history = history;
    }

    // Thêm một bình luận vào nhiệm vụ
    public void addComment(Comment comment) {
        if (!comments.contains(comment)) {
            comments.add(comment);
        }
    }

    // Xóa một bình luận khỏi nhiệm vụ
    public boolean removeComment(Comment comment) {
        return comments.remove(comment);
    }

    // Thêm một lịch sử thay đổi vào nhiệm vụ
    public void addHistory(TaskHistory historyItem) {
        if (!history.contains(historyItem)) {
            history.add(historyItem);
        }
    }

    // Kiểm tra xem nhiệm vụ có quá hạn hay không
    public boolean isOverdue() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dueDateTime = sdf.parse(dueDate);
            Date currentDate = new Date();
            return currentDate.after(dueDateTime) && !status.equals("Hoàn thành");
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Kiểm tra xem nhiệm vụ có sắp đến hạn hay không (trong vòng 2 ngày)
    public boolean isDueSoon() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dueDateTime = sdf.parse(dueDate);
            Date currentDate = new Date();

            // Tính số mili giây giữa thời hạn và thời gian hiện tại
            long diff = dueDateTime.getTime() - currentDate.getTime();
            // Chuyển đổi sang số ngày
            long daysDiff = diff / (24 * 60 * 60 * 1000);

            return daysDiff >= 0 && daysDiff <= 2 && !status.equals("Hoàn thành");
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Tính số ngày còn lại đến hạn
    public int getDaysRemaining() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dueDateTime = sdf.parse(dueDate);
            Date currentDate = new Date();

            // Tính số mili giây giữa thời hạn và thời gian hiện tại
            long diff = dueDateTime.getTime() - currentDate.getTime();
            // Chuyển đổi sang số ngày
            long daysDiff = diff / (24 * 60 * 60 * 1000);

            return (int) daysDiff;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}