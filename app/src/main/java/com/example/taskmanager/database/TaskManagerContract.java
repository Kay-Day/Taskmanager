package com.example.taskmanager.database;

import android.provider.BaseColumns;

public final class TaskManagerContract {
    // Ngăn việc tạo instance
    private TaskManagerContract() {}

    // Định nghĩa bảng User
    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_FULL_NAME = "full_name";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_LOGIN_ATTEMPTS = "login_attempts";
        public static final String COLUMN_LOCKED_UNTIL = "locked_until";
    }

    // Định nghĩa bảng Project
    public static class ProjectEntry implements BaseColumns {
        public static final String TABLE_NAME = "projects";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_CREATED_BY = "created_by";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_END_DATE = "end_date";
        public static final String COLUMN_PRIORITY = "priority";
        public static final String COLUMN_CREATED_AT = "created_at";
    }

    // Định nghĩa bảng ProjectMember
    public static class ProjectMemberEntry implements BaseColumns {
        public static final String TABLE_NAME = "project_members";
        public static final String COLUMN_PROJECT_ID = "project_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_ROLE = "role";
        public static final String COLUMN_JOINED_AT = "joined_at";
    }

    // Định nghĩa bảng Task
    public static class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "tasks";
        public static final String COLUMN_PROJECT_ID = "project_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_ASSIGNED_TO = "assigned_to";
        public static final String COLUMN_CREATED_BY = "created_by";
        public static final String COLUMN_PRIORITY = "priority";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_RESOURCE_LINK = "resource_link";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_DUE_DATE = "due_date";
        public static final String COLUMN_CREATED_AT = "created_at";

        public static final String STATUS_NOT_STARTED = "Chưa bắt đầu";
        public static final String STATUS_IN_PROGRESS = "Đang thực hiện";
        public static final String STATUS_COMPLETED = "Hoàn thành";
        public static final String STATUS_DELAYED = "Tạm hoãn";
    }

    // Định nghĩa bảng TaskHistory
    public static class TaskHistoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "task_history";
        public static final String COLUMN_TASK_ID = "task_id";
        public static final String COLUMN_CHANGED_BY = "changed_by";
        public static final String COLUMN_FIELD_CHANGED = "field_changed";
        public static final String COLUMN_OLD_VALUE = "old_value";
        public static final String COLUMN_NEW_VALUE = "new_value";
        public static final String COLUMN_CHANGED_AT = "changed_at";
    }

    // Định nghĩa bảng Comment
    public static class CommentEntry implements BaseColumns {
        public static final String TABLE_NAME = "comments";
        public static final String COLUMN_TASK_ID = "task_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_CREATED_AT = "created_at";
    }

    // Định nghĩa bảng Notification
    public static class NotificationEntry implements BaseColumns {
        public static final String TABLE_NAME = "notifications";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_RELATED_ID = "related_id";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_IS_READ = "is_read";
        public static final String COLUMN_CREATED_AT = "created_at";
    }

    // Định nghĩa bảng CalendarEvent
    public static class CalendarEventEntry implements BaseColumns {
        public static final String TABLE_NAME = "calendar_events";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_EVENT_DATE = "event_date";
        public static final String COLUMN_EVENT_TIME = "event_time";
        public static final String COLUMN_TASK_ID = "task_id";
        public static final String COLUMN_COLOR = "color";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_RESOURCE_LINK = "resource_link";
        public static final String COLUMN_IS_COMPLETED = "is_completed";
    }
}