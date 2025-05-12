package com.example.taskmanager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "task_manager.db";
    private static final int DATABASE_VERSION = 1;

    // Singleton instance
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Users
        db.execSQL("CREATE TABLE " + TaskManagerContract.UserEntry.TABLE_NAME + " (" +
                TaskManagerContract.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskManagerContract.UserEntry.COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, " +
                TaskManagerContract.UserEntry.COLUMN_PASSWORD + " TEXT NOT NULL, " +
                TaskManagerContract.UserEntry.COLUMN_FULL_NAME + " TEXT NOT NULL, " +
                TaskManagerContract.UserEntry.COLUMN_EMAIL + " TEXT NOT NULL UNIQUE, " +
                TaskManagerContract.UserEntry.COLUMN_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                TaskManagerContract.UserEntry.COLUMN_LOGIN_ATTEMPTS + " INTEGER DEFAULT 0, " +
                TaskManagerContract.UserEntry.COLUMN_LOCKED_UNTIL + " TEXT DEFAULT NULL)");

        // Tạo bảng Projects
        db.execSQL("CREATE TABLE " + TaskManagerContract.ProjectEntry.TABLE_NAME + " (" +
                TaskManagerContract.ProjectEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskManagerContract.ProjectEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                TaskManagerContract.ProjectEntry.COLUMN_DESCRIPTION + " TEXT, " +
                TaskManagerContract.ProjectEntry.COLUMN_CREATED_BY + " INTEGER NOT NULL, " +
                TaskManagerContract.ProjectEntry.COLUMN_START_DATE + " TEXT NOT NULL, " +
                TaskManagerContract.ProjectEntry.COLUMN_END_DATE + " TEXT NOT NULL, " +
                TaskManagerContract.ProjectEntry.COLUMN_PRIORITY + " TEXT NOT NULL, " +
                TaskManagerContract.ProjectEntry.COLUMN_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + TaskManagerContract.ProjectEntry.COLUMN_CREATED_BY + ") REFERENCES " +
                TaskManagerContract.UserEntry.TABLE_NAME + "(" + TaskManagerContract.UserEntry._ID + "))");

        // Tạo bảng Project_Members
        db.execSQL("CREATE TABLE " + TaskManagerContract.ProjectMemberEntry.TABLE_NAME + " (" +
                TaskManagerContract.ProjectMemberEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskManagerContract.ProjectMemberEntry.COLUMN_PROJECT_ID + " INTEGER NOT NULL, " +
                TaskManagerContract.ProjectMemberEntry.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                TaskManagerContract.ProjectMemberEntry.COLUMN_ROLE + " TEXT NOT NULL, " +
                TaskManagerContract.ProjectMemberEntry.COLUMN_JOINED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + TaskManagerContract.ProjectMemberEntry.COLUMN_PROJECT_ID + ") REFERENCES " +
                TaskManagerContract.ProjectEntry.TABLE_NAME + "(" + TaskManagerContract.ProjectEntry._ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + TaskManagerContract.ProjectMemberEntry.COLUMN_USER_ID + ") REFERENCES " +
                TaskManagerContract.UserEntry.TABLE_NAME + "(" + TaskManagerContract.UserEntry._ID + "), " +
                "UNIQUE(" + TaskManagerContract.ProjectMemberEntry.COLUMN_PROJECT_ID + ", " +
                TaskManagerContract.ProjectMemberEntry.COLUMN_USER_ID + "))");

        // Tạo bảng Tasks
        db.execSQL("CREATE TABLE " + TaskManagerContract.TaskEntry.TABLE_NAME + " (" +
                TaskManagerContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskManagerContract.TaskEntry.COLUMN_PROJECT_ID + " INTEGER NOT NULL, " +
                TaskManagerContract.TaskEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                TaskManagerContract.TaskEntry.COLUMN_DESCRIPTION + " TEXT, " +
                TaskManagerContract.TaskEntry.COLUMN_ASSIGNED_TO + " INTEGER, " +
                TaskManagerContract.TaskEntry.COLUMN_CREATED_BY + " INTEGER NOT NULL, " +
                TaskManagerContract.TaskEntry.COLUMN_PRIORITY + " TEXT NOT NULL, " +
                TaskManagerContract.TaskEntry.COLUMN_STATUS + " TEXT NOT NULL, " +
                TaskManagerContract.TaskEntry.COLUMN_RESOURCE_LINK + " TEXT, " +
                TaskManagerContract.TaskEntry.COLUMN_START_DATE + " TEXT NOT NULL, " +
                TaskManagerContract.TaskEntry.COLUMN_DUE_DATE + " TEXT NOT NULL, " +
                TaskManagerContract.TaskEntry.COLUMN_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + TaskManagerContract.TaskEntry.COLUMN_PROJECT_ID + ") REFERENCES " +
                TaskManagerContract.ProjectEntry.TABLE_NAME + "(" + TaskManagerContract.ProjectEntry._ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + TaskManagerContract.TaskEntry.COLUMN_ASSIGNED_TO + ") REFERENCES " +
                TaskManagerContract.UserEntry.TABLE_NAME + "(" + TaskManagerContract.UserEntry._ID + "), " +
                "FOREIGN KEY (" + TaskManagerContract.TaskEntry.COLUMN_CREATED_BY + ") REFERENCES " +
                TaskManagerContract.UserEntry.TABLE_NAME + "(" + TaskManagerContract.UserEntry._ID + "))");

        // Tạo bảng Task_History
        db.execSQL("CREATE TABLE " + TaskManagerContract.TaskHistoryEntry.TABLE_NAME + " (" +
                TaskManagerContract.TaskHistoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskManagerContract.TaskHistoryEntry.COLUMN_TASK_ID + " INTEGER NOT NULL, " +
                TaskManagerContract.TaskHistoryEntry.COLUMN_CHANGED_BY + " INTEGER NOT NULL, " +
                TaskManagerContract.TaskHistoryEntry.COLUMN_FIELD_CHANGED + " TEXT NOT NULL, " +
                TaskManagerContract.TaskHistoryEntry.COLUMN_OLD_VALUE + " TEXT, " +
                TaskManagerContract.TaskHistoryEntry.COLUMN_NEW_VALUE + " TEXT, " +
                TaskManagerContract.TaskHistoryEntry.COLUMN_CHANGED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + TaskManagerContract.TaskHistoryEntry.COLUMN_TASK_ID + ") REFERENCES " +
                TaskManagerContract.TaskEntry.TABLE_NAME + "(" + TaskManagerContract.TaskEntry._ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + TaskManagerContract.TaskHistoryEntry.COLUMN_CHANGED_BY + ") REFERENCES " +
                TaskManagerContract.UserEntry.TABLE_NAME + "(" + TaskManagerContract.UserEntry._ID + "))");

        // Tạo bảng Comments
        db.execSQL("CREATE TABLE " + TaskManagerContract.CommentEntry.TABLE_NAME + " (" +
                TaskManagerContract.CommentEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskManagerContract.CommentEntry.COLUMN_TASK_ID + " INTEGER NOT NULL, " +
                TaskManagerContract.CommentEntry.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                TaskManagerContract.CommentEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                TaskManagerContract.CommentEntry.COLUMN_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + TaskManagerContract.CommentEntry.COLUMN_TASK_ID + ") REFERENCES " +
                TaskManagerContract.TaskEntry.TABLE_NAME + "(" + TaskManagerContract.TaskEntry._ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + TaskManagerContract.CommentEntry.COLUMN_USER_ID + ") REFERENCES " +
                TaskManagerContract.UserEntry.TABLE_NAME + "(" + TaskManagerContract.UserEntry._ID + "))");

        // Tạo bảng Notifications
        db.execSQL("CREATE TABLE " + TaskManagerContract.NotificationEntry.TABLE_NAME + " (" +
                TaskManagerContract.NotificationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskManagerContract.NotificationEntry.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                TaskManagerContract.NotificationEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                TaskManagerContract.NotificationEntry.COLUMN_MESSAGE + " TEXT NOT NULL, " +
                TaskManagerContract.NotificationEntry.COLUMN_RELATED_ID + " INTEGER, " +
                TaskManagerContract.NotificationEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                TaskManagerContract.NotificationEntry.COLUMN_IS_READ + " INTEGER DEFAULT 0, " +
                TaskManagerContract.NotificationEntry.COLUMN_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + TaskManagerContract.NotificationEntry.COLUMN_USER_ID + ") REFERENCES " +
                TaskManagerContract.UserEntry.TABLE_NAME + "(" + TaskManagerContract.UserEntry._ID + "))");

        // Tạo bảng Calendar_Events
        db.execSQL("CREATE TABLE " + TaskManagerContract.CalendarEventEntry.TABLE_NAME + " (" +
                TaskManagerContract.CalendarEventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskManagerContract.CalendarEventEntry.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                TaskManagerContract.CalendarEventEntry.COLUMN_TASK_ID + " INTEGER, " +
                TaskManagerContract.CalendarEventEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                TaskManagerContract.CalendarEventEntry.COLUMN_RESOURCE_LINK + " TEXT, " +
                TaskManagerContract.CalendarEventEntry.COLUMN_EVENT_DATE + " TEXT NOT NULL, " +
                TaskManagerContract.CalendarEventEntry.COLUMN_EVENT_TIME + " TEXT, " +
                TaskManagerContract.CalendarEventEntry.COLUMN_IS_COMPLETED + " INTEGER DEFAULT 0, " +
                TaskManagerContract.CalendarEventEntry.COLUMN_CREATED_AT + " TEXT DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + TaskManagerContract.CalendarEventEntry.COLUMN_USER_ID + ") REFERENCES " +
                TaskManagerContract.UserEntry.TABLE_NAME + "(" + TaskManagerContract.UserEntry._ID + "), " +
                "FOREIGN KEY (" + TaskManagerContract.CalendarEventEntry.COLUMN_TASK_ID + ") REFERENCES " +
                TaskManagerContract.TaskEntry.TABLE_NAME + "(" + TaskManagerContract.TaskEntry._ID + ") ON DELETE SET NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xử lý nâng cấp database trong các phiên bản sau
        db.execSQL("DROP TABLE IF EXISTS " + TaskManagerContract.CalendarEventEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TaskManagerContract.NotificationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TaskManagerContract.CommentEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TaskManagerContract.TaskHistoryEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TaskManagerContract.TaskEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TaskManagerContract.ProjectMemberEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TaskManagerContract.ProjectEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TaskManagerContract.UserEntry.TABLE_NAME);

        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}