package com.example.taskmanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.taskmanager.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationDAO extends BaseDAO{
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public NotificationDAO(Context context) {
        super(context);
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // Mở kết nối đến database
    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    // Đóng kết nối đến database
    public void close() {
        dbHelper.close();
    }

    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
        this.ownDatabase = false; // Không tự quản lý database
    }



    // Tạo thông báo mới
    public long createNotification(Notification notification) {
        ContentValues values = new ContentValues();
        values.put(TaskManagerContract.NotificationEntry.COLUMN_USER_ID, notification.getUserId());
        values.put(TaskManagerContract.NotificationEntry.COLUMN_TITLE, notification.getTitle());
        values.put(TaskManagerContract.NotificationEntry.COLUMN_MESSAGE, notification.getMessage());
        values.put(TaskManagerContract.NotificationEntry.COLUMN_RELATED_ID, notification.getRelatedId());
        values.put(TaskManagerContract.NotificationEntry.COLUMN_TYPE, notification.getType());
        values.put(TaskManagerContract.NotificationEntry.COLUMN_IS_READ, notification.isRead() ? 1 : 0);

        return database.insert(TaskManagerContract.NotificationEntry.TABLE_NAME, null, values);
    }

    // Đánh dấu thông báo đã đọc
    public int markNotificationAsRead(long notificationId) {
        ContentValues values = new ContentValues();
        values.put(TaskManagerContract.NotificationEntry.COLUMN_IS_READ, 1);

        return database.update(
                TaskManagerContract.NotificationEntry.TABLE_NAME,
                values,
                TaskManagerContract.NotificationEntry._ID + " = ?",
                new String[]{String.valueOf(notificationId)}
        );
    }

    // Đánh dấu tất cả thông báo đã đọc
    public int markAllNotificationsAsRead(long userId) {
        ContentValues values = new ContentValues();
        values.put(TaskManagerContract.NotificationEntry.COLUMN_IS_READ, 1);

        return database.update(
                TaskManagerContract.NotificationEntry.TABLE_NAME,
                values,
                TaskManagerContract.NotificationEntry.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
    }

    // Xóa thông báo
    public int deleteNotification(long notificationId) {
        return database.delete(
                TaskManagerContract.NotificationEntry.TABLE_NAME,
                TaskManagerContract.NotificationEntry._ID + " = ?",
                new String[]{String.valueOf(notificationId)}
        );
    }

    // Xóa tất cả thông báo đã đọc
    public int deleteReadNotifications(long userId) {
        return database.delete(
                TaskManagerContract.NotificationEntry.TABLE_NAME,
                TaskManagerContract.NotificationEntry.COLUMN_USER_ID + " = ? AND " +
                        TaskManagerContract.NotificationEntry.COLUMN_IS_READ + " = 1",
                new String[]{String.valueOf(userId)}
        );
    }

    // Lấy thông báo theo ID
    public Notification getNotificationById(long id) {
        Cursor cursor = database.query(
                TaskManagerContract.NotificationEntry.TABLE_NAME,
                null,
                TaskManagerContract.NotificationEntry._ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        Notification notification = null;
        if (cursor != null && cursor.moveToFirst()) {
            notification = cursorToNotification(cursor);
            cursor.close();
        }

        return notification;
    }

    // Lấy danh sách thông báo của một người dùng
    public List<Notification> getNotificationsByUser(long userId) {
        List<Notification> notifications = new ArrayList<>();

        Cursor cursor = database.query(
                TaskManagerContract.NotificationEntry.TABLE_NAME,
                null,
                TaskManagerContract.NotificationEntry.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                TaskManagerContract.NotificationEntry.COLUMN_CREATED_AT + " DESC"
        );

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Notification notification = cursorToNotification(cursor);
                notifications.add(notification);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return notifications;
    }

    // Lấy danh sách thông báo chưa đọc
    public List<Notification> getUnreadNotifications(long userId) {
        List<Notification> notifications = new ArrayList<>();

        Cursor cursor = database.query(
                TaskManagerContract.NotificationEntry.TABLE_NAME,
                null,
                TaskManagerContract.NotificationEntry.COLUMN_USER_ID + " = ? AND " +
                        TaskManagerContract.NotificationEntry.COLUMN_IS_READ + " = 0",
                new String[]{String.valueOf(userId)},
                null,
                null,
                TaskManagerContract.NotificationEntry.COLUMN_CREATED_AT + " DESC"
        );

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Notification notification = cursorToNotification(cursor);
                notifications.add(notification);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return notifications;
    }

    // Đếm số lượng thông báo chưa đọc
    public int countUnreadNotifications(long userId) {
        Cursor cursor = database.query(
                TaskManagerContract.NotificationEntry.TABLE_NAME,
                new String[]{"COUNT(*) AS count"},
                TaskManagerContract.NotificationEntry.COLUMN_USER_ID + " = ? AND " +
                        TaskManagerContract.NotificationEntry.COLUMN_IS_READ + " = 0",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        return count;
    }

    // Chuyển đổi từ Cursor sang đối tượng Notification
    private Notification cursorToNotification(Cursor cursor) {
        Notification notification = new Notification();
        notification.setId(cursor.getLong(cursor.getColumnIndexOrThrow(TaskManagerContract.NotificationEntry._ID)));
        notification.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(TaskManagerContract.NotificationEntry.COLUMN_USER_ID)));
        notification.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.NotificationEntry.COLUMN_TITLE)));
        notification.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.NotificationEntry.COLUMN_MESSAGE)));

        int relatedIdIndex = cursor.getColumnIndexOrThrow(TaskManagerContract.NotificationEntry.COLUMN_RELATED_ID);
        if (!cursor.isNull(relatedIdIndex)) {
            notification.setRelatedId(cursor.getLong(relatedIdIndex));
        }

        notification.setType(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.NotificationEntry.COLUMN_TYPE)));
        notification.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(TaskManagerContract.NotificationEntry.COLUMN_IS_READ)) == 1);
        notification.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.NotificationEntry.COLUMN_CREATED_AT)));

        return notification;
    }
}