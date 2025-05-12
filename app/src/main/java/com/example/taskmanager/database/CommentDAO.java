package com.example.taskmanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.taskmanager.models.Comment;
import com.example.taskmanager.models.User;

import java.util.ArrayList;
import java.util.List;

public class CommentDAO extends BaseDAO{
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private UserDAO userDAO;

    public CommentDAO(Context context) {
        super(context);
        dbHelper = DatabaseHelper.getInstance(context);
        userDAO = new UserDAO(context);
    }

    // Mở kết nối đến database
    public void open() {
        database = dbHelper.getWritableDatabase();
        userDAO.open();
    }

    // Đóng kết nối đến database
    public void close() {
        userDAO.close();
        dbHelper.close();
    }
    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
        this.ownDatabase = false; // Không tự quản lý database
    }

    // Tạo bình luận mới
    public long createComment(Comment comment) {
        ContentValues values = new ContentValues();
        values.put(TaskManagerContract.CommentEntry.COLUMN_TASK_ID, comment.getTaskId());
        values.put(TaskManagerContract.CommentEntry.COLUMN_USER_ID, comment.getUserId());
        values.put(TaskManagerContract.CommentEntry.COLUMN_CONTENT, comment.getContent());

        return database.insert(TaskManagerContract.CommentEntry.TABLE_NAME, null, values);
    }




    // Cập nhật bình luận
    public int updateComment(Comment comment) {
        ContentValues values = new ContentValues();
        values.put(TaskManagerContract.CommentEntry.COLUMN_CONTENT, comment.getContent());

        return database.update(
                TaskManagerContract.CommentEntry.TABLE_NAME,
                values,
                TaskManagerContract.CommentEntry._ID + " = ?",
                new String[]{String.valueOf(comment.getId())}
        );
    }

    // Xóa bình luận
    public int deleteComment(long commentId) {
        return database.delete(
                TaskManagerContract.CommentEntry.TABLE_NAME,
                TaskManagerContract.CommentEntry._ID + " = ?",
                new String[]{String.valueOf(commentId)}
        );
    }

    // Lấy bình luận theo ID
    public Comment getCommentById(long id) {
        Cursor cursor = database.query(
                TaskManagerContract.CommentEntry.TABLE_NAME,
                null,
                TaskManagerContract.CommentEntry._ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        Comment comment = null;
        if (cursor != null && cursor.moveToFirst()) {
            comment = cursorToComment(cursor);

            // Lấy thông tin người bình luận
            User user = userDAO.getUserById(comment.getUserId());
            comment.setUser(user);

            cursor.close();
        }

        return comment;
    }

    // Lấy danh sách bình luận của một nhiệm vụ
    public List<Comment> getCommentsByTaskId(long taskId) {
        List<Comment> comments = new ArrayList<>();

        Cursor cursor = database.query(
                TaskManagerContract.CommentEntry.TABLE_NAME,
                null,
                TaskManagerContract.CommentEntry.COLUMN_TASK_ID + " = ?",
                new String[]{String.valueOf(taskId)},
                null,
                null,
                TaskManagerContract.CommentEntry.COLUMN_CREATED_AT + " ASC"
        );

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Comment comment = cursorToComment(cursor);

                // Lấy thông tin người bình luận
                User user = userDAO.getUserById(comment.getUserId());
                comment.setUser(user);

                comments.add(comment);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return comments;
    }

    // Lấy danh sách bình luận của một người dùng
    public List<Comment> getCommentsByUserId(long userId) {
        List<Comment> comments = new ArrayList<>();

        Cursor cursor = database.query(
                TaskManagerContract.CommentEntry.TABLE_NAME,
                null,
                TaskManagerContract.CommentEntry.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                TaskManagerContract.CommentEntry.COLUMN_CREATED_AT + " DESC"
        );

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Comment comment = cursorToComment(cursor);
                comments.add(comment);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return comments;
    }

    // Chuyển đổi từ Cursor sang đối tượng Comment
    private Comment cursorToComment(Cursor cursor) {
        Comment comment = new Comment();
        comment.setId(cursor.getLong(cursor.getColumnIndexOrThrow(TaskManagerContract.CommentEntry._ID)));
        comment.setTaskId(cursor.getLong(cursor.getColumnIndexOrThrow(TaskManagerContract.CommentEntry.COLUMN_TASK_ID)));
        comment.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(TaskManagerContract.CommentEntry.COLUMN_USER_ID)));
        comment.setContent(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.CommentEntry.COLUMN_CONTENT)));
        comment.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.CommentEntry.COLUMN_CREATED_AT)));

        return comment;
    }
}