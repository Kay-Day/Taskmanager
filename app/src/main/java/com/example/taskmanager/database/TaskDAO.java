package com.example.taskmanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.taskmanager.models.Task;
import com.example.taskmanager.models.TaskHistory;
import com.example.taskmanager.models.User;

import java.util.ArrayList;
import java.util.List;

public class TaskDAO extends BaseDAO{
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private UserDAO userDAO;
    private TaskHistoryDAO historyDAO;

    public TaskDAO(Context context) {
        super(context);
        dbHelper = DatabaseHelper.getInstance(context);
        userDAO = new UserDAO(context);
        historyDAO = new TaskHistoryDAO(context);
    }



    // Mở kết nối đến database
    public void open() {
        database = dbHelper.getWritableDatabase();
        userDAO.open();
        historyDAO.open();
    }

    // Đóng kết nối đến database
    public void close() {
        historyDAO.close();
        userDAO.close();
        dbHelper.close();
    }

    // Tạo nhiệm vụ mới
    public long createTask(Task task) {
        if (database == null) {
            open(); // Đảm bảo database đã được mở
        }
        ContentValues values = new ContentValues();
        values.put(TaskManagerContract.TaskEntry.COLUMN_PROJECT_ID, task.getProjectId());
        values.put(TaskManagerContract.TaskEntry.COLUMN_TITLE, task.getTitle());
        values.put(TaskManagerContract.TaskEntry.COLUMN_DESCRIPTION, task.getDescription());
        values.put(TaskManagerContract.TaskEntry.COLUMN_ASSIGNED_TO, task.getAssignedTo());
        values.put(TaskManagerContract.TaskEntry.COLUMN_CREATED_BY, task.getCreatedBy());
        values.put(TaskManagerContract.TaskEntry.COLUMN_PRIORITY, task.getPriority());
        values.put(TaskManagerContract.TaskEntry.COLUMN_STATUS, task.getStatus());
        values.put(TaskManagerContract.TaskEntry.COLUMN_RESOURCE_LINK, task.getResourceLink());
        values.put(TaskManagerContract.TaskEntry.COLUMN_START_DATE, task.getStartDate());
        values.put(TaskManagerContract.TaskEntry.COLUMN_DUE_DATE, task.getDueDate());

        return database.insert(TaskManagerContract.TaskEntry.TABLE_NAME, null, values);
    }

    // Cập nhật thông tin nhiệm vụ
    public int updateTask(Task newTask, Task oldTask, long userId) {
        if (database == null) {
            open();
        }
        ContentValues values = new ContentValues();
        values.put(TaskManagerContract.TaskEntry.COLUMN_TITLE, newTask.getTitle());
        values.put(TaskManagerContract.TaskEntry.COLUMN_DESCRIPTION, newTask.getDescription());
        values.put(TaskManagerContract.TaskEntry.COLUMN_ASSIGNED_TO, newTask.getAssignedTo());
        values.put(TaskManagerContract.TaskEntry.COLUMN_PRIORITY, newTask.getPriority());
        values.put(TaskManagerContract.TaskEntry.COLUMN_STATUS, newTask.getStatus());
        values.put(TaskManagerContract.TaskEntry.COLUMN_RESOURCE_LINK, newTask.getResourceLink());
        values.put(TaskManagerContract.TaskEntry.COLUMN_START_DATE, newTask.getStartDate());
        values.put(TaskManagerContract.TaskEntry.COLUMN_DUE_DATE, newTask.getDueDate());

        // Ghi lại lịch sử thay đổi
        recordTaskHistoryChanges(newTask, oldTask, userId);

        return database.update(
                TaskManagerContract.TaskEntry.TABLE_NAME,
                values,
                TaskManagerContract.TaskEntry._ID + " = ?",
                new String[]{String.valueOf(newTask.getId())}
        );
    }

    // Ghi lại lịch sử thay đổi của nhiệm vụ
    private void recordTaskHistoryChanges(Task newTask, Task oldTask, long userId) {
        // Kiểm tra và ghi lại từng thay đổi
        if (!newTask.getTitle().equals(oldTask.getTitle())) {
            historyDAO.createTaskHistory(new TaskHistory(
                    newTask.getId(), userId, "Tiêu đề", oldTask.getTitle(), newTask.getTitle()
            ));
        }

        if ((newTask.getDescription() == null && oldTask.getDescription() != null) ||
                (newTask.getDescription() != null && !newTask.getDescription().equals(oldTask.getDescription()))) {
            historyDAO.createTaskHistory(new TaskHistory(
                    newTask.getId(), userId, "Mô tả", oldTask.getDescription(), newTask.getDescription()
            ));
        }

        if ((newTask.getAssignedTo() == null && oldTask.getAssignedTo() != null) ||
                (newTask.getAssignedTo() != null && !newTask.getAssignedTo().equals(oldTask.getAssignedTo()))) {
            String oldAssignee = oldTask.getAssignedTo() != null ?
                    userDAO.getUserById(oldTask.getAssignedTo()).getFullName() : "Chưa gán";
            String newAssignee = newTask.getAssignedTo() != null ?
                    userDAO.getUserById(newTask.getAssignedTo()).getFullName() : "Chưa gán";

            historyDAO.createTaskHistory(new TaskHistory(
                    newTask.getId(), userId, "Người thực hiện", oldAssignee, newAssignee
            ));
        }

        if (!newTask.getPriority().equals(oldTask.getPriority())) {
            historyDAO.createTaskHistory(new TaskHistory(
                    newTask.getId(), userId, "Mức độ ưu tiên", oldTask.getPriority(), newTask.getPriority()
            ));
        }

        if (!newTask.getStatus().equals(oldTask.getStatus())) {
            historyDAO.createTaskHistory(new TaskHistory(
                    newTask.getId(), userId, "Trạng thái", oldTask.getStatus(), newTask.getStatus()
            ));
        }

        if ((newTask.getResourceLink() == null && oldTask.getResourceLink() != null) ||
                (newTask.getResourceLink() != null && !newTask.getResourceLink().equals(oldTask.getResourceLink()))) {
            historyDAO.createTaskHistory(new TaskHistory(
                    newTask.getId(), userId, "Đường dẫn tài nguyên", oldTask.getResourceLink(), newTask.getResourceLink()
            ));
        }

        if (!newTask.getStartDate().equals(oldTask.getStartDate())) {
            historyDAO.createTaskHistory(new TaskHistory(
                    newTask.getId(), userId, "Ngày bắt đầu", oldTask.getStartDate(), newTask.getStartDate()
            ));
        }

        if (!newTask.getDueDate().equals(oldTask.getDueDate())) {
            historyDAO.createTaskHistory(new TaskHistory(
                    newTask.getId(), userId, "Ngày đến hạn", oldTask.getDueDate(), newTask.getDueDate()
            ));
        }
    }

    // Xóa nhiệm vụ
    public int deleteTask(long taskId) {
        return database.delete(
                TaskManagerContract.TaskEntry.TABLE_NAME,
                TaskManagerContract.TaskEntry._ID + " = ?",
                new String[]{String.valueOf(taskId)}
        );
    }

    // Lấy nhiệm vụ theo ID
    public Task getTaskById(long taskId) {
        if (database == null || !database.isOpen()) {
            open(); // Đảm bảo database đã được mở và vẫn còn hoạt động
        }

        Cursor cursor = null;
        Task task = null;

        try {
            cursor = database.query(
                    TaskManagerContract.TaskEntry.TABLE_NAME,
                    null,
                    TaskManagerContract.TaskEntry._ID + " = ?",
                    new String[]{String.valueOf(taskId)},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                task = cursorToTask(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return task;
    }

    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
        this.ownDatabase = false; // Database được cung cấp từ bên ngoài, không tự quản lý
    }


    // Lấy danh sách tất cả nhiệm vụ của một dự án
    public List<Task> getTasksByProject(long projectId) {
        if (database == null) {
            open();
        }

        List<Task> tasks = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = database.query(
                    TaskManagerContract.TaskEntry.TABLE_NAME,
                    null,
                    TaskManagerContract.TaskEntry.COLUMN_PROJECT_ID + " = ?",
                    new String[]{String.valueOf(projectId)},
                    null,
                    null,
                    TaskManagerContract.TaskEntry.COLUMN_DUE_DATE + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Task task = cursorToTask(cursor);
                    tasks.add(task);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return tasks;
    }

    // Lấy danh sách nhiệm vụ được gán cho một người dùng
    public List<Task> getTasksByUser(long userId) {
        List<Task> tasks = new ArrayList<>();

        Cursor cursor = database.query(
                TaskManagerContract.TaskEntry.TABLE_NAME,
                null,
                TaskManagerContract.TaskEntry.COLUMN_ASSIGNED_TO + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                TaskManagerContract.TaskEntry.COLUMN_DUE_DATE + " ASC"
        );

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Task task = cursorToTask(cursor);
                tasks.add(task);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return tasks;
    }

    // Lấy danh sách nhiệm vụ của người dùng trong một dự án cụ thể
    public List<Task> getTasksByUserAndProject(long userId, long projectId) {
        List<Task> tasks = new ArrayList<>();

        Cursor cursor = database.query(
                TaskManagerContract.TaskEntry.TABLE_NAME,
                null,
                TaskManagerContract.TaskEntry.COLUMN_ASSIGNED_TO + " = ? AND " +
                        TaskManagerContract.TaskEntry.COLUMN_PROJECT_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(projectId)},
                null,
                null,
                TaskManagerContract.TaskEntry.COLUMN_DUE_DATE + " ASC"
        );

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Task task = cursorToTask(cursor);
                tasks.add(task);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return tasks;
    }

    // Lấy danh sách nhiệm vụ theo trạng thái
    public List<Task> getTasksByStatus(String status) {
        List<Task> tasks = new ArrayList<>();

        Cursor cursor = database.query(
                TaskManagerContract.TaskEntry.TABLE_NAME,
                null,
                TaskManagerContract.TaskEntry.COLUMN_STATUS + " = ?",
                new String[]{status},
                null,
                null,
                TaskManagerContract.TaskEntry.COLUMN_DUE_DATE + " ASC"
        );

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Task task = cursorToTask(cursor);
                tasks.add(task);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return tasks;
    }

    // Lấy danh sách nhiệm vụ sắp đến hạn (trong vòng 2 ngày)
    public List<Task> getUpcomingTasks() {
        List<Task> allTasks = new ArrayList<>();
        List<Task> upcomingTasks = new ArrayList<>();

        // Lấy tất cả nhiệm vụ chưa hoàn thành
        Cursor cursor = database.query(
                TaskManagerContract.TaskEntry.TABLE_NAME,
                null,
                TaskManagerContract.TaskEntry.COLUMN_STATUS + " != ?",
                new String[]{TaskManagerContract.TaskEntry.STATUS_COMPLETED},
                null,
                null,
                TaskManagerContract.TaskEntry.COLUMN_DUE_DATE + " ASC"
        );

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Task task = cursorToTask(cursor);
                allTasks.add(task);
                cursor.moveToNext();
            }
            cursor.close();
        }

        // Lọc các nhiệm vụ sắp đến hạn
        for (Task task : allTasks) {
            if (task.isDueSoon()) {
                upcomingTasks.add(task);
            }
        }

        return upcomingTasks;
    }

    // Lấy danh sách nhiệm vụ quá hạn
    public List<Task> getOverdueTasks() {
        List<Task> allTasks = new ArrayList<>();
        List<Task> overdueTasks = new ArrayList<>();

        // Lấy tất cả nhiệm vụ chưa hoàn thành
        Cursor cursor = database.query(
                TaskManagerContract.TaskEntry.TABLE_NAME,
                null,
                TaskManagerContract.TaskEntry.COLUMN_STATUS + " != ?",
                new String[]{TaskManagerContract.TaskEntry.STATUS_COMPLETED},
                null,
                null,
                TaskManagerContract.TaskEntry.COLUMN_DUE_DATE + " ASC"
        );

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Task task = cursorToTask(cursor);
                allTasks.add(task);
                cursor.moveToNext();
            }
            cursor.close();
        }

        // Lọc các nhiệm vụ quá hạn
        for (Task task : allTasks) {
            if (task.isOverdue()) {
                overdueTasks.add(task);
            }
        }

        return overdueTasks;
    }

    // Chuyển đổi từ Cursor sang đối tượng Task
    private Task cursorToTask(Cursor cursor) {
        Task task = new Task();
        task.setId(cursor.getLong(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskEntry._ID)));
        task.setProjectId(cursor.getLong(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskEntry.COLUMN_PROJECT_ID)));
        task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskEntry.COLUMN_TITLE)));
        task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskEntry.COLUMN_DESCRIPTION)));

        int assignedToIndex = cursor.getColumnIndexOrThrow(TaskManagerContract.TaskEntry.COLUMN_ASSIGNED_TO);
        if (!cursor.isNull(assignedToIndex)) {
            task.setAssignedTo(cursor.getLong(assignedToIndex));
        }

        task.setCreatedBy(cursor.getLong(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskEntry.COLUMN_CREATED_BY)));
        task.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskEntry.COLUMN_PRIORITY)));
        task.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskEntry.COLUMN_STATUS)));
        task.setResourceLink(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskEntry.COLUMN_RESOURCE_LINK)));
        task.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskEntry.COLUMN_START_DATE)));
        task.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskEntry.COLUMN_DUE_DATE)));
        task.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskEntry.COLUMN_CREATED_AT)));

        return task;
    }
}