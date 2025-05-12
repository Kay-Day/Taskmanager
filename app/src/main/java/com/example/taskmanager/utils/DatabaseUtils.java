package com.example.taskmanager.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.taskmanager.database.CalendarEventDAO;
import com.example.taskmanager.database.CommentDAO;
import com.example.taskmanager.database.DatabaseManager;
import com.example.taskmanager.database.NotificationDAO;
import com.example.taskmanager.database.ProjectDAO;
import com.example.taskmanager.database.TaskDAO;
import com.example.taskmanager.database.TaskHistoryDAO;
import com.example.taskmanager.database.UserDAO;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Lớp tiện ích để thực hiện các thao tác cơ sở dữ liệu an toàn
 */
public class DatabaseUtils {
    private static final String TAG = "DatabaseUtils";
    private static final ReentrantLock LOCK = new ReentrantLock();

    /**
     * Interface cho các thao tác cơ sở dữ liệu
     */
    public interface DatabaseOperation<T> {
        T execute(SQLiteDatabase database, ProjectDAO projectDAO, TaskDAO taskDAO,
                  UserDAO userDAO, TaskHistoryDAO historyDAO, CommentDAO commentDAO,
                  CalendarEventDAO eventDAO, NotificationDAO notificationDAO) throws Exception;
    }

    /**
     * Thực thi thao tác cơ sở dữ liệu an toàn
     */
    public static <T> T executeWithTransaction(Context context, DatabaseOperation<T> operation) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        LOCK.lock();
        DatabaseManager dbManager = null;
        SQLiteDatabase database = null;

        try {
            dbManager = DatabaseManager.getInstance(context);
            database = dbManager.openDatabase();
            Log.d(TAG, "Database opened for thread: " + Thread.currentThread().getId());

            // Khởi tạo các DAO với cùng một kết nối
            ProjectDAO projectDAO = new ProjectDAO(context);
            TaskDAO taskDAO = new TaskDAO(context);
            UserDAO userDAO = new UserDAO(context);
            TaskHistoryDAO historyDAO = new TaskHistoryDAO(context);
            CommentDAO commentDAO = new CommentDAO(context);
            CalendarEventDAO eventDAO = new CalendarEventDAO(context);
            NotificationDAO notificationDAO = new NotificationDAO(context);

            // Thiết lập database cho các DAO
            projectDAO.setDatabase(database);
            taskDAO.setDatabase(database);
            userDAO.setDatabase(database);
            historyDAO.setDatabase(database);
            commentDAO.setDatabase(database);
            eventDAO.setDatabase(database);
            notificationDAO.setDatabase(database);

            // Bắt đầu transaction
            database.beginTransaction();

            try {
                // Thực thi thao tác
                T result = operation.execute(database, projectDAO, taskDAO, userDAO,
                        historyDAO, commentDAO, eventDAO, notificationDAO);

                // Đánh dấu transaction thành công
                database.setTransactionSuccessful();
                return result;
            } finally {
                // Kết thúc transaction
                database.endTransaction();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error executing database operation: " + e.getMessage(), e);
            throw new RuntimeException("Database operation failed", e);
        } finally {
            // Đóng kết nối
            if (database != null && dbManager != null) {
                dbManager.closeDatabase();
                Log.d(TAG, "Database closed for thread: " + Thread.currentThread().getId());
            }

            LOCK.unlock();
        }
    }
}