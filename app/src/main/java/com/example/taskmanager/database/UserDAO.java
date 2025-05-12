package com.example.taskmanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.taskmanager.models.User;
import com.example.taskmanager.utils.SecurityUtils;

import java.util.ArrayList;
import java.util.List;

public class UserDAO extends BaseDAO {

    public UserDAO(Context context) {
        super(context);
    }

    // Tạo người dùng mới
    public long createUser(User user) {
        if (database == null) {
            open(); // Đảm bảo database đã được mở
        }

        ContentValues values = new ContentValues();
        values.put(TaskManagerContract.UserEntry.COLUMN_USERNAME, user.getUsername());
        values.put(TaskManagerContract.UserEntry.COLUMN_PASSWORD, SecurityUtils.hashPassword(user.getPassword()));
        values.put(TaskManagerContract.UserEntry.COLUMN_FULL_NAME, user.getFullName());
        values.put(TaskManagerContract.UserEntry.COLUMN_EMAIL, user.getEmail());
        values.put(TaskManagerContract.UserEntry.COLUMN_LOGIN_ATTEMPTS, user.getLoginAttempts());
        values.put(TaskManagerContract.UserEntry.COLUMN_LOCKED_UNTIL, user.getLockedUntil());

        return database.insert(TaskManagerContract.UserEntry.TABLE_NAME, null, values);
    }

    // Cập nhật thông tin người dùng
    public int updateUser(User user) {
        if (database == null) {
            open();
        }

        ContentValues values = new ContentValues();
        values.put(TaskManagerContract.UserEntry.COLUMN_FULL_NAME, user.getFullName());
        values.put(TaskManagerContract.UserEntry.COLUMN_EMAIL, user.getEmail());

        return database.update(
                TaskManagerContract.UserEntry.TABLE_NAME,
                values,
                TaskManagerContract.UserEntry._ID + " = ?",
                new String[]{String.valueOf(user.getId())}
        );
    }

    // Cập nhật mật khẩu người dùng
    public int updatePassword(long userId, String newPassword) {
        if (database == null) {
            open();
        }

        ContentValues values = new ContentValues();
        values.put(TaskManagerContract.UserEntry.COLUMN_PASSWORD, SecurityUtils.hashPassword(newPassword));

        return database.update(
                TaskManagerContract.UserEntry.TABLE_NAME,
                values,
                TaskManagerContract.UserEntry._ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
    }

    // Cập nhật số lần đăng nhập không thành công và trạng thái khóa
    public int updateLoginAttempts(long userId, int attempts, String lockedUntil) {
        if (database == null) {
            open();
        }

        ContentValues values = new ContentValues();
        values.put(TaskManagerContract.UserEntry.COLUMN_LOGIN_ATTEMPTS, attempts);
        values.put(TaskManagerContract.UserEntry.COLUMN_LOCKED_UNTIL, lockedUntil);

        return database.update(
                TaskManagerContract.UserEntry.TABLE_NAME,
                values,
                TaskManagerContract.UserEntry._ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
    }

    // Xóa người dùng
    public int deleteUser(long userId) {
        if (database == null) {
            open();
        }

        return database.delete(
                TaskManagerContract.UserEntry.TABLE_NAME,
                TaskManagerContract.UserEntry._ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
    }

    // Lấy người dùng theo ID
    public User getUserById(long id) {
        if (database == null) {
            open();
        }

        Cursor cursor = null;
        User user = null;

        try {
            cursor = database.query(
                    TaskManagerContract.UserEntry.TABLE_NAME,
                    null,
                    TaskManagerContract.UserEntry._ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return user;
    }

    // Lấy người dùng theo tên đăng nhập
    public User getUserByUsername(String username) {
        if (database == null) {
            open();
        }

        Cursor cursor = null;
        User user = null;

        try {
            cursor = database.query(
                    TaskManagerContract.UserEntry.TABLE_NAME,
                    null,
                    TaskManagerContract.UserEntry.COLUMN_USERNAME + " = ?",
                    new String[]{username},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return user;
    }

    // Lấy người dùng theo email
    public User getUserByEmail(String email) {
        if (database == null) {
            open();
        }

        Cursor cursor = null;
        User user = null;

        try {
            cursor = database.query(
                    TaskManagerContract.UserEntry.TABLE_NAME,
                    null,
                    TaskManagerContract.UserEntry.COLUMN_EMAIL + " = ?",
                    new String[]{email},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return user;
    }

    // Tìm kiếm người dùng theo tên đầy đủ hoặc tên đăng nhập
    public List<User> searchUsersByName(String query) {
        if (database == null) {
            open();
        }

        List<User> users = new ArrayList<>();
        Cursor cursor = null;

        try {
            String searchQuery = "%" + query + "%";
            String[] selectionArgs = {searchQuery, searchQuery};

            cursor = database.query(
                    TaskManagerContract.UserEntry.TABLE_NAME,
                    null,
                    TaskManagerContract.UserEntry.COLUMN_FULL_NAME + " LIKE ? OR " +
                            TaskManagerContract.UserEntry.COLUMN_USERNAME + " LIKE ?",
                    selectionArgs,
                    null,
                    null,
                    TaskManagerContract.UserEntry.COLUMN_FULL_NAME + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    User user = cursorToUser(cursor);
                    users.add(user);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return users;
    }

    // Lấy danh sách tất cả người dùng
    public List<User> getAllUsers() {
        if (database == null) {
            open();
        }

        List<User> users = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = database.query(
                    TaskManagerContract.UserEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    TaskManagerContract.UserEntry.COLUMN_FULL_NAME + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    User user = cursorToUser(cursor);
                    users.add(user);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return users;
    }

    // Kiểm tra đăng nhập
    public User checkLogin(String username, String password) {
        if (database == null) {
            open();
        }

        Cursor cursor = null;
        User user = null;

        try {
            cursor = database.query(
                    TaskManagerContract.UserEntry.TABLE_NAME,
                    null,
                    TaskManagerContract.UserEntry.COLUMN_USERNAME + " = ?",
                    new String[]{username},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                String hashedPassword = cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.UserEntry.COLUMN_PASSWORD));
                if (SecurityUtils.verifyPassword(password, hashedPassword)) {
                    user = cursorToUser(cursor);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return user;
    }

    public boolean isUsernameExists(String username) {
        if (database == null) {
            open();
        }

        Cursor cursor = null;
        boolean exists = false;

        try {
            cursor = database.query(
                    TaskManagerContract.UserEntry.TABLE_NAME,
                    new String[]{TaskManagerContract.UserEntry._ID},
                    TaskManagerContract.UserEntry.COLUMN_USERNAME + " = ?",
                    new String[]{username},
                    null,
                    null,
                    null
            );

            exists = (cursor != null && cursor.getCount() > 0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return exists;
    }

    // Thêm phương thức kiểm tra email đã tồn tại
    public boolean isEmailExists(String email) {
        if (database == null) {
            open();
        }

        Cursor cursor = null;
        boolean exists = false;

        try {
            cursor = database.query(
                    TaskManagerContract.UserEntry.TABLE_NAME,
                    new String[]{TaskManagerContract.UserEntry._ID},
                    TaskManagerContract.UserEntry.COLUMN_EMAIL + " = ?",
                    new String[]{email},
                    null,
                    null,
                    null
            );

            exists = (cursor != null && cursor.getCount() > 0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return exists;
    }

    // Thêm phương thức kiểm tra tên đăng nhập hoặc email đã tồn tại (trừ người dùng hiện tại)
    public boolean isUsernameOrEmailExists(String username, String email, long userId) {
        if (database == null) {
            open();
        }

        Cursor cursor = null;
        boolean exists = false;

        try {
            cursor = database.query(
                    TaskManagerContract.UserEntry.TABLE_NAME,
                    new String[]{TaskManagerContract.UserEntry._ID},
                    "(" + TaskManagerContract.UserEntry.COLUMN_USERNAME + " = ? OR " +
                            TaskManagerContract.UserEntry.COLUMN_EMAIL + " = ?) AND " +
                            TaskManagerContract.UserEntry._ID + " != ?",
                    new String[]{username, email, String.valueOf(userId)},
                    null,
                    null,
                    null
            );

            exists = (cursor != null && cursor.getCount() > 0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return exists;
    }

    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
        this.ownDatabase = false; // Database được cung cấp từ bên ngoài, không tự quản lý
    }

    // Chuyển đổi từ Cursor sang đối tượng User
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        try {
            user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(TaskManagerContract.UserEntry._ID)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.UserEntry.COLUMN_USERNAME)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.UserEntry.COLUMN_PASSWORD)));
            user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.UserEntry.COLUMN_FULL_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.UserEntry.COLUMN_EMAIL)));
            user.setLoginAttempts(cursor.getInt(cursor.getColumnIndexOrThrow(TaskManagerContract.UserEntry.COLUMN_LOGIN_ATTEMPTS)));
            user.setLockedUntil(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.UserEntry.COLUMN_LOCKED_UNTIL)));
            user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.UserEntry.COLUMN_CREATED_AT)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }
}