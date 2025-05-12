package com.example.taskmanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.taskmanager.models.Project;
import com.example.taskmanager.models.User;
import com.example.taskmanager.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class ProjectDAO extends BaseDAO {

    public ProjectDAO(Context context) {
        super(context); // Sửa lại - đảm bảo truyền context cho lớp cha
    }

    // Tạo dự án mới
    public long createProject(Project project) {
        if (database == null) {
            open(); // Đảm bảo database đã được mở
        }

        ContentValues values = new ContentValues();
        values.put(TaskManagerContract.ProjectEntry.COLUMN_NAME, project.getName());
        values.put(TaskManagerContract.ProjectEntry.COLUMN_DESCRIPTION, project.getDescription());
        values.put(TaskManagerContract.ProjectEntry.COLUMN_CREATED_BY, project.getCreatedBy());
        values.put(TaskManagerContract.ProjectEntry.COLUMN_START_DATE, project.getStartDate());
        values.put(TaskManagerContract.ProjectEntry.COLUMN_END_DATE, project.getEndDate());
        values.put(TaskManagerContract.ProjectEntry.COLUMN_PRIORITY, project.getPriority());

        long projectId = database.insert(TaskManagerContract.ProjectEntry.TABLE_NAME, null, values);

        // Thêm người tạo dự án vào danh sách thành viên với vai trò quản lý
        if (projectId != -1) {
            ContentValues memberValues = new ContentValues();
            memberValues.put(TaskManagerContract.ProjectMemberEntry.COLUMN_PROJECT_ID, projectId);
            memberValues.put(TaskManagerContract.ProjectMemberEntry.COLUMN_USER_ID, project.getCreatedBy());
            memberValues.put(TaskManagerContract.ProjectMemberEntry.COLUMN_ROLE, "Quản lý");
            memberValues.put(TaskManagerContract.ProjectMemberEntry.COLUMN_JOINED_AT, DateTimeUtils.getCurrentDateTime());

            database.insert(TaskManagerContract.ProjectMemberEntry.TABLE_NAME, null, memberValues);
        }

        return projectId;
    }

    // Cập nhật thông tin dự án
    // Cập nhật thông tin dự án
    public int updateProject(Project project) {
        if (database == null) {
            open();
        }

        ContentValues values = new ContentValues();
        values.put(TaskManagerContract.ProjectEntry.COLUMN_NAME, project.getName());
        values.put(TaskManagerContract.ProjectEntry.COLUMN_DESCRIPTION, project.getDescription());
        values.put(TaskManagerContract.ProjectEntry.COLUMN_START_DATE, project.getStartDate());
        values.put(TaskManagerContract.ProjectEntry.COLUMN_END_DATE, project.getEndDate());
        values.put(TaskManagerContract.ProjectEntry.COLUMN_PRIORITY, project.getPriority());

        int result = database.update(
                TaskManagerContract.ProjectEntry.TABLE_NAME,
                values,
                TaskManagerContract.ProjectEntry._ID + " = ?",
                new String[]{String.valueOf(project.getId())}
        );

        // Ghi log để debug
        if (result > 0) {
            Log.d("ProjectDAO", "Project updated successfully: " + project.getId());
        } else {
            Log.d("ProjectDAO", "Failed to update project: " + project.getId());
        }

        return result;
    }

    // Xóa dự án
    public int deleteProject(long projectId) {
        if (database == null) {
            open();
        }

        // Xóa tất cả dữ liệu liên quan đến dự án sẽ được xử lý tự động bởi ON DELETE CASCADE
        return database.delete(
                TaskManagerContract.ProjectEntry.TABLE_NAME,
                TaskManagerContract.ProjectEntry._ID + " = ?",
                new String[]{String.valueOf(projectId)}
        );
    }

    // Lấy dự án theo ID
    public Project getProjectById(long id) {
        if (database == null) {
            open();
        }

        Project project = null;
        Cursor cursor = null;

        try {
            cursor = database.query(
                    TaskManagerContract.ProjectEntry.TABLE_NAME,
                    null,
                    TaskManagerContract.ProjectEntry._ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                project = cursorToProject(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return project;
    }

    // Lấy danh sách thành viên của dự án
    public List<User> getProjectMembers(long projectId) {
        if (database == null) {
            open();
        }

        List<User> members = new ArrayList<>();
        Cursor cursor = null;

        try {
            String query = "SELECT u.* FROM " + TaskManagerContract.UserEntry.TABLE_NAME + " u " +
                    "INNER JOIN " + TaskManagerContract.ProjectMemberEntry.TABLE_NAME + " pm " +
                    "ON u." + TaskManagerContract.UserEntry._ID + " = pm." + TaskManagerContract.ProjectMemberEntry.COLUMN_USER_ID + " " +
                    "WHERE pm." + TaskManagerContract.ProjectMemberEntry.COLUMN_PROJECT_ID + " = ? " +
                    "ORDER BY u." + TaskManagerContract.UserEntry.COLUMN_FULL_NAME + " ASC";

            cursor = database.rawQuery(query, new String[]{String.valueOf(projectId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    User user = new User();
                    user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(TaskManagerContract.UserEntry._ID)));
                    user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.UserEntry.COLUMN_USERNAME)));
                    user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.UserEntry.COLUMN_FULL_NAME)));
                    user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.UserEntry.COLUMN_EMAIL)));
                    members.add(user);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return members;
    }

    // Lấy danh sách tất cả dự án
    public List<Project> getAllProjects() {
        if (database == null) {
            open();
        }

        List<Project> projects = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = database.query(
                    TaskManagerContract.ProjectEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    TaskManagerContract.ProjectEntry.COLUMN_START_DATE + " DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Project project = cursorToProject(cursor);
                    projects.add(project);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return projects;
    }

    // Lấy danh sách dự án mà người dùng tham gia
    public List<Project> getProjectsByUser(long userId) {
        if (database == null) {
            open();
        }

        List<Project> projects = new ArrayList<>();
        Cursor cursor = null;

        try {
            // Sử dụng JOIN để lấy các dự án mà người dùng là thành viên
            String query = "SELECT p.* FROM " + TaskManagerContract.ProjectEntry.TABLE_NAME + " p " +
                    "INNER JOIN " + TaskManagerContract.ProjectMemberEntry.TABLE_NAME + " pm " +
                    "ON p." + TaskManagerContract.ProjectEntry._ID + " = pm." + TaskManagerContract.ProjectMemberEntry.COLUMN_PROJECT_ID + " " +
                    "WHERE pm." + TaskManagerContract.ProjectMemberEntry.COLUMN_USER_ID + " = ? " +
                    "ORDER BY p." + TaskManagerContract.ProjectEntry.COLUMN_START_DATE + " DESC";

            cursor = database.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Project project = cursorToProject(cursor);
                    projects.add(project);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return projects;
    }

    // Ví dụ: phương thức thêm thành viên vào dự án
    // Thêm thành viên vào dự án
    public boolean addMember(long projectId, long userId, String role) {
        if (database == null) {
            open();
        }

        // Kiểm tra xem thành viên đã tồn tại trong dự án chưa
        if (isUserMemberOfProject(projectId, userId)) {
            Log.d("ProjectDAO", "User " + userId + " is already a member of project " + projectId);
            return true; // Đã là thành viên, coi như thành công
        }

        ContentValues values = new ContentValues();
        values.put(TaskManagerContract.ProjectMemberEntry.COLUMN_PROJECT_ID, projectId);
        values.put(TaskManagerContract.ProjectMemberEntry.COLUMN_USER_ID, userId);
        values.put(TaskManagerContract.ProjectMemberEntry.COLUMN_ROLE, role);
        values.put(TaskManagerContract.ProjectMemberEntry.COLUMN_JOINED_AT, DateTimeUtils.getCurrentDateTime());

        long id = database.insert(TaskManagerContract.ProjectMemberEntry.TABLE_NAME, null, values);

        // Ghi log để debug
        if (id != -1) {
            Log.d("ProjectDAO", "Member added successfully: User " + userId + " to Project " + projectId);
        } else {
            Log.d("ProjectDAO", "Failed to add member: User " + userId + " to Project " + projectId);
        }

        return id != -1;
    }

    // Xóa thành viên khỏi dự án
    public int removeMemberFromProject(long projectId, long userId) {
        if (database == null) {
            open();
        }

        return database.delete(
                TaskManagerContract.ProjectMemberEntry.TABLE_NAME,
                TaskManagerContract.ProjectMemberEntry.COLUMN_PROJECT_ID + " = ? AND " +
                        TaskManagerContract.ProjectMemberEntry.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(projectId), String.valueOf(userId)}
        );
    }

    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
        this.ownDatabase = false; // Database được cung cấp từ bên ngoài, không tự quản lý
    }

    // Cập nhật vai trò của thành viên
    public int updateMemberRole(long projectId, long userId, String newRole) {
        if (database == null) {
            open();
        }

        ContentValues values = new ContentValues();
        values.put(TaskManagerContract.ProjectMemberEntry.COLUMN_ROLE, newRole);

        return database.update(
                TaskManagerContract.ProjectMemberEntry.TABLE_NAME,
                values,
                TaskManagerContract.ProjectMemberEntry.COLUMN_PROJECT_ID + " = ? AND " +
                        TaskManagerContract.ProjectMemberEntry.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(projectId), String.valueOf(userId)}
        );
    }

    // Kiểm tra xem người dùng có phải là thành viên của dự án không
    public boolean isUserMemberOfProject(long projectId, long userId) {
        if (database == null) {
            open();
        }

        Cursor cursor = null;
        boolean isMember = false;

        try {
            cursor = database.query(
                    TaskManagerContract.ProjectMemberEntry.TABLE_NAME,
                    new String[]{TaskManagerContract.ProjectMemberEntry._ID},
                    TaskManagerContract.ProjectMemberEntry.COLUMN_PROJECT_ID + " = ? AND " +
                            TaskManagerContract.ProjectMemberEntry.COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(projectId), String.valueOf(userId)},
                    null,
                    null,
                    null
            );

            if (cursor != null) {
                isMember = cursor.getCount() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return isMember;
    }

    // Lấy vai trò của người dùng trong dự án
    public String getUserRoleInProject(long projectId, long userId) {
        if (database == null) {
            open();
        }

        Cursor cursor = null;
        String role = null;

        try {
            cursor = database.query(
                    TaskManagerContract.ProjectMemberEntry.TABLE_NAME,
                    new String[]{TaskManagerContract.ProjectMemberEntry.COLUMN_ROLE},
                    TaskManagerContract.ProjectMemberEntry.COLUMN_PROJECT_ID + " = ? AND " +
                            TaskManagerContract.ProjectMemberEntry.COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(projectId), String.valueOf(userId)},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                role = cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.ProjectMemberEntry.COLUMN_ROLE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return role;
    }

    // Chuyển đổi từ Cursor sang đối tượng Project
    private Project cursorToProject(Cursor cursor) {
        Project project = new Project();
        try {
            project.setId(cursor.getLong(cursor.getColumnIndexOrThrow(TaskManagerContract.ProjectEntry._ID)));
            project.setName(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.ProjectEntry.COLUMN_NAME)));
            project.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.ProjectEntry.COLUMN_DESCRIPTION)));
            project.setCreatedBy(cursor.getLong(cursor.getColumnIndexOrThrow(TaskManagerContract.ProjectEntry.COLUMN_CREATED_BY)));
            project.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.ProjectEntry.COLUMN_START_DATE)));
            project.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.ProjectEntry.COLUMN_END_DATE)));
            project.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.ProjectEntry.COLUMN_PRIORITY)));
            project.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(TaskManagerContract.ProjectEntry.COLUMN_CREATED_AT)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return project;
    }
}