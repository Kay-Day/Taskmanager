package com.example.taskmanager.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Project implements Serializable {
    private long id;
    private String name;
    private String description;
    private long createdBy;
    private String startDate;
    private String endDate;
    private String priority;
    private String createdAt;
    private List<User> members;
    private List<Task> tasks;

    // Constructor rỗng
    public Project() {
        this.members = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }

    // Constructor không có ID và danh sách thành viên, nhiệm vụ
    public Project(String name, String description, long createdBy, String startDate, String endDate, String priority) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priority = priority;
        this.members = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }

    // Constructor đầy đủ
    public Project(long id, String name, String description, long createdBy, String startDate,
                   String endDate, String priority, String createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priority = priority;
        this.createdAt = createdAt;
        this.members = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }

    // Getters và Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    // Thêm một thành viên vào dự án
    public void addMember(User user) {
        if (!members.contains(user)) {
            members.add(user);
        }
    }

    // Xóa một thành viên khỏi dự án
    public boolean removeMember(User user) {
        return members.remove(user);
    }

    // Thêm một nhiệm vụ vào dự án
    public void addTask(Task task) {
        if (!tasks.contains(task)) {
            tasks.add(task);
        }
    }

    // Xóa một nhiệm vụ khỏi dự án
    public boolean removeTask(Task task) {
        return tasks.remove(task);
    }

    // Đếm số nhiệm vụ đã hoàn thành
    public int getCompletedTasksCount() {
        int count = 0;
        for (Task task : tasks) {
            if (task.getStatus().equals("Hoàn thành")) {
                count++;
            }
        }
        return count;
    }

    // Đếm số nhiệm vụ đang thực hiện
    public int getInProgressTasksCount() {
        int count = 0;
        for (Task task : tasks) {
            if (task.getStatus().equals("Đang thực hiện")) {
                count++;
            }
        }
        return count;
    }

    // Tính phần trăm hoàn thành của dự án
    public int getCompletionPercentage() {
        if (tasks.isEmpty()) {
            return 0;
        }
        return (getCompletedTasksCount() * 100) / tasks.size();
    }
}