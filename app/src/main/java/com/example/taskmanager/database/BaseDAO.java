package com.example.taskmanager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public abstract class BaseDAO {
    private static final String TAG = "BaseDAO";
    protected Context context;
    protected SQLiteDatabase database;
    protected DatabaseManager dbManager;
    protected boolean ownDatabase; // Theo dõi xem database có được tạo bởi DAO này hay không
    protected String daoName; // Tên của DAO để hiển thị trong log

    public BaseDAO(Context context) {
        if (context != null) {
            this.context = context.getApplicationContext();
            this.dbManager = DatabaseManager.getInstance(this.context);
            this.ownDatabase = true; // Mặc định DAO sẽ tự quản lý database
            this.daoName = this.getClass().getSimpleName();
        } else {
            // Ghi log lỗi hoặc xử lý trường hợp context null
            throw new IllegalArgumentException("Context cannot be null in BaseDAO constructor");
        }
    }

    /**
     * Thiết lập database từ bên ngoài
     */
    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
        this.ownDatabase = false; // Database được cung cấp từ bên ngoài, không tự quản lý
        Log.d(TAG, daoName + ": Using external database in thread: " + Thread.currentThread().getId());
    }

    /**
     * Mở kết nối database
     */
    public void open() {
        long threadId = Thread.currentThread().getId();

        try {
            // Kiểm tra nếu database đã mở
            if (database != null && database.isOpen()) {
                Log.d(TAG, daoName + ": Database already open in thread: " + threadId);
                return;
            }

            // Mở database mới
            if (dbManager != null) {
                database = dbManager.openDatabase();
                this.ownDatabase = true; // DAO quản lý database này
                Log.d(TAG, daoName + ": Opened database in thread: " + threadId);
            }
        } catch (Exception e) {
            Log.e(TAG, daoName + ": Error opening database in thread: " + threadId, e);
            throw e;
        }
    }

    /**
     * Đóng kết nối database
     */
//    public void close() {
//        long threadId = Thread.currentThread().getId();
//
//        if (ownDatabase && dbManager != null) {
//            dbManager.closeDatabase();
//            Log.d(TAG, daoName + ": Closed database in thread: " + threadId);
//        }
//
//        // Đặt database = null để tránh sử dụng lại database đã đóng
//        database = null;
//    }

    /**
     * Đóng kết nối database
     */
    public void close() {
        long threadId = Thread.currentThread().getId();

        if (ownDatabase && dbManager != null) {
            dbManager.closeDatabase();
            Log.d(TAG, daoName + ": Closed database in thread: " + threadId);
        } else {
            Log.d(TAG, daoName + ": Skipping close - not the owner (Thread: " + threadId + ")");
        }

        // Đặt database = null để tránh sử dụng lại database đã đóng
        database = null;
    }

    /**
     * Lấy database hiện tại
     */
    public SQLiteDatabase getDatabase() {
        ensureDatabaseOpen();
        return database;
    }

    /**
     * Kiểm tra xem database có đang mở không, nếu không thì mở nó
     */
    protected void ensureDatabaseOpen() {
        if (database == null || !database.isOpen()) {
            Log.d(TAG, daoName + ": Re-opening database in thread: " + Thread.currentThread().getId());
            open();
        }
    }

    /**
     * Chạy truy vấn với kiểm tra connection tự động
     */
    protected void executeWithDbCheck(Runnable action) {
        ensureDatabaseOpen();
        try {
            action.run();
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("already-closed object")) {
                Log.w(TAG, daoName + ": Database was closed. Attempting to reopen...");
                open();
                action.run(); // Thử lại sau khi mở lại database
            } else {
                throw e; // Ném lại nếu không phải lỗi database đã đóng
            }
        }
    }

    /**
     * Mở transaction cho database
     */
    protected void beginTransaction() {
        ensureDatabaseOpen();
        if (ownDatabase && dbManager != null) {
            dbManager.beginTransaction();
        } else if (database != null && database.isOpen()) {
            database.beginTransaction();
        }
    }

    /**
     * Đánh dấu transaction thành công
     */
    protected void setTransactionSuccessful() {
        if (ownDatabase && dbManager != null) {
            dbManager.setTransactionSuccessful();
        } else if (database != null && database.isOpen() && database.inTransaction()) {
            database.setTransactionSuccessful();
        }
    }

    /**
     * Kết thúc transaction
     */
    protected void endTransaction() {
        if (ownDatabase && dbManager != null) {
            dbManager.endTransaction();
        } else if (database != null && database.isOpen() && database.inTransaction()) {
            database.endTransaction();
        }
    }


}