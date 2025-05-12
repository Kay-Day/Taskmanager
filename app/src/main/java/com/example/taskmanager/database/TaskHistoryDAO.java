package com.example.taskmanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.taskmanager.models.TaskHistory;
import com.example.taskmanager.models.User;
import com.example.taskmanager.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class TaskHistoryDAO {
    private static final String TAG = "TaskHistoryDAO";
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private UserDAO userDAO;
    private boolean ownDatabase = true; // Theo dõi xem database có được tạo bởi DAO này không

    public TaskHistoryDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        userDAO = new UserDAO(context);
    }

    /**
     * Thiết lập database từ bên ngoài
     */
    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
        this.ownDatabase = false; // Database được cung cấp từ bên ngoài, không tự quản lý

        // Chia sẻ database với UserDAO
        if (userDAO != null) {
            userDAO.setDatabase(database);
        }

        Log.d(TAG, "Using external database in thread: " + Thread.currentThread().getId());
    }

    /**
     * Lấy database hiện tại
     */
    public SQLiteDatabase getDatabase() {
        ensureDatabaseOpen();
        return database;
    }

    // Mở kết nối đến database
    public void open() {
        if (database == null || !database.isOpen()) {
            database = dbHelper.getWritableDatabase();
            this.ownDatabase = true;
            Log.d(TAG, "Opened database in thread: " + Thread.currentThread().getId());

            // Nếu DAO này là chủ sở hữu database, mở UserDAO
            if (ownDatabase && userDAO != null) {
                userDAO.open();
            }
        }
    }

    // Đóng kết nối đến database
    public void close() {
        // Chỉ đóng database nếu DAO này là chủ sở hữu
        if (ownDatabase) {
            Log.d(TAG, "Closing database in thread: " + Thread.currentThread().getId());

            if (userDAO != null) {
                userDAO.close();
            }

            if (dbHelper != null) {
                dbHelper.close();
            }
        }
        database = null;
    }

    /**
     * Kiểm tra xem database có đang mở không, nếu không thì mở nó
     */
    private void ensureDatabaseOpen() {
        if (database == null || !database.isOpen()) {
            Log.d(TAG, "Re-opening database in thread: " + Thread.currentThread().getId());
            open();
        }
    }

    // Tạo lịch sử thay đổi mới
    public long createTaskHistory(TaskHistory history) {
        ensureDatabaseOpen();

        ContentValues values = new ContentValues();
        values.put(TaskManagerContract.TaskHistoryEntry.COLUMN_TASK_ID, history.getTaskId());
        values.put(TaskManagerContract.TaskHistoryEntry.COLUMN_CHANGED_BY, history.getChangedBy());
        values.put(TaskManagerContract.TaskHistoryEntry.COLUMN_FIELD_CHANGED, history.getFieldChanged());
        values.put(TaskManagerContract.TaskHistoryEntry.COLUMN_OLD_VALUE, history.getOldValue());
        values.put(TaskManagerContract.TaskHistoryEntry.COLUMN_NEW_VALUE, history.getNewValue());
        values.put(TaskManagerContract.TaskHistoryEntry.COLUMN_CHANGED_AT, DateTimeUtils.getCurrentDateTime());

        return database.insert(TaskManagerContract.TaskHistoryEntry.TABLE_NAME, null, values);
    }

    // Lấy lịch sử thay đổi theo ID
    public TaskHistory getTaskHistoryById(long id) {
        ensureDatabaseOpen();

        Cursor cursor = database.query(
                TaskManagerContract.TaskHistoryEntry.TABLE_NAME,
                null,
                TaskManagerContract.TaskHistoryEntry._ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        TaskHistory history = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                history = cursorToTaskHistory(cursor);
            }
            cursor.close();
        }

        return history;
    }

    // Lấy danh sách lịch sử thay đổi của một nhiệm vụ
    public List<TaskHistory> getTaskHistoryByTaskId(long taskId) {
        ensureDatabaseOpen();

        List<TaskHistory> historyList = new ArrayList<>();

        try {
            Cursor cursor = database.query(
                    TaskManagerContract.TaskHistoryEntry.TABLE_NAME,
                    null,
                    TaskManagerContract.TaskHistoryEntry.COLUMN_TASK_ID + " = ?",
                    new String[]{String.valueOf(taskId)},
                    null,
                    null,
                    TaskManagerContract.TaskHistoryEntry.COLUMN_CHANGED_AT + " DESC"
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    TaskHistory history = cursorToTaskHistory(cursor);
                    historyList.add(history);
                }
                cursor.close();
            }
        } catch (IllegalStateException e) {
            // Xử lý ngoại lệ khi database đã đóng
            Log.e(TAG, "Database was closed unexpectedly: " + e.getMessage());

            // Thử mở lại database và thực hiện lại truy vấn
            if (!ownDatabase) {
                Log.w(TAG, "Cannot reopen database as this DAO is not the owner");
                throw e;
            }

            // Thử mở lại database
            open();

            // Thực hiện lại truy vấn
            Cursor cursor = database.query(
                    TaskManagerContract.TaskHistoryEntry.TABLE_NAME,
                    null,
                    TaskManagerContract.TaskHistoryEntry.COLUMN_TASK_ID + " = ?",
                    new String[]{String.valueOf(taskId)},
                    null,
                    null,
                    TaskManagerContract.TaskHistoryEntry.COLUMN_CHANGED_AT + " DESC"
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    TaskHistory history = cursorToTaskHistory(cursor);
                    historyList.add(history);
                }
                cursor.close();
            }
        }

        return historyList;
    }

    // Chuyển đổi từ Cursor sang đối tượng TaskHistory
    private TaskHistory cursorToTaskHistory(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskHistoryEntry._ID));
        long taskId = cursor.getLong(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskHistoryEntry.COLUMN_TASK_ID));
        long changedBy = cursor.getLong(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskHistoryEntry.COLUMN_CHANGED_BY));
        String fieldChanged = cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskHistoryEntry.COLUMN_FIELD_CHANGED));
        String oldValue = cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskHistoryEntry.COLUMN_OLD_VALUE));
        String newValue = cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskHistoryEntry.COLUMN_NEW_VALUE));
        String changedAt = cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.TaskHistoryEntry.COLUMN_CHANGED_AT));

        TaskHistory history = new TaskHistory(id, taskId, changedBy, fieldChanged, oldValue, newValue, changedAt);

        // Lấy thông tin người thay đổi
        User user = userDAO.getUserById(changedBy);
        history.setUser(user);

        return history;
    }
}