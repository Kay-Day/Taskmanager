package com.example.taskmanager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private static DatabaseManager instance;
    private Context appContext;

    // Sử dụng ConcurrentHashMap để lưu trữ database connection theo thread ID
    private final ConcurrentHashMap<Long, ThreadDatabaseEntry> threadDatabaseEntries = new ConcurrentHashMap<>();
    private final ReentrantLock mLock = new ReentrantLock();

    /**
     * Lớp để quản lý SQLiteDatabase cho mỗi thread
     */
    private static class ThreadDatabaseEntry {
        SQLiteDatabase database;
        AtomicInteger referenceCount;
        Lock lock;
        boolean isClosed = false;
        private final Context context;

        ThreadDatabaseEntry(Context context) {
            this.context = context;
            this.database = null;  // Database sẽ được mở khi cần
            this.referenceCount = new AtomicInteger(0);
            this.lock = new ReentrantLock();
        }

        SQLiteDatabase openDatabase() {
            if (database == null || !database.isOpen()) {
                // Sử dụng getInstance thay vì tạo mới
                SQLiteOpenHelper helper = DatabaseHelper.getInstance(context);
                database = helper.getWritableDatabase();
                isClosed = false;
            }
            referenceCount.incrementAndGet();
            return database;
        }

        int decrementCount() {
            return referenceCount.decrementAndGet();
        }

        int getCount() {
            return referenceCount.get();
        }

        void resetCount() {
            referenceCount.set(0);
        }

        void close() {
            if (database != null && database.isOpen()) {
                try {
                    if (database.inTransaction()) {
                        database.endTransaction();
                    }
                    database.close();
                } catch (Exception e) {
                    // Bỏ qua
                }
                database = null;
            }

            isClosed = true;
        }

        boolean isClosed() {
            return isClosed || database == null || !database.isOpen();
        }
    }

    private DatabaseManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    /**
     * Mở hoặc lấy connection database cho thread hiện tại
     */
    public SQLiteDatabase openDatabase() {
        long threadId = Thread.currentThread().getId();
        Log.d(TAG, "openDatabase called in thread: " + threadId);

        mLock.lock();
        try {
            // Kiểm tra xem thread đã có entry chưa
            ThreadDatabaseEntry entry = threadDatabaseEntries.get(threadId);

            if (entry != null) {
                if (!entry.isClosed()) {
                    // Entry vẫn còn tốt, mở database
                    SQLiteDatabase db = entry.openDatabase();
                    Log.d(TAG, "Database reference acquired: " + entry.getCount() + " (Thread: " + threadId + ")");
                    return db;
                } else {
                    // Entry đã đóng, tạo mới
                    threadDatabaseEntries.remove(threadId);
                    Log.d(TAG, "Removed closed entry for thread: " + threadId);
                }
            }

            // Tạo entry mới
            try {
                ThreadDatabaseEntry newEntry = new ThreadDatabaseEntry(appContext);
                SQLiteDatabase db = newEntry.openDatabase();
                threadDatabaseEntries.put(threadId, newEntry);
                Log.d(TAG, "New database opened for thread: " + threadId + ", counter: 1");
                return db;
            } catch (Exception e) {
                Log.e(TAG, "Error opening database for thread: " + threadId, e);
                throw e;
            }
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Giảm reference count và đóng database nếu không còn reference nào khác
     */
//    public void closeDatabase() {
//        long threadId = Thread.currentThread().getId();
//        Log.d(TAG, "closeDatabase called in thread: " + threadId);
//
//        mLock.lock();
//        try {
//            // Lấy entry cho thread hiện tại
//            ThreadDatabaseEntry entry = threadDatabaseEntries.get(threadId);
//            if (entry == null || entry.isClosed()) {
//                // Không có entry hoặc đã đóng
//                if (entry != null) {
//                    threadDatabaseEntries.remove(threadId);
//                    Log.d(TAG, "Removed already closed entry for thread: " + threadId);
//                }
//                return;
//            }
//
//            // Giảm reference count
//            int count = entry.decrementCount();
//            Log.d(TAG, "Database close called: " + count + " (Thread: " + threadId + ")");
//
//            if (count <= 0) {
//                // Reset count nếu âm
//                if (count < 0) {
//                    entry.resetCount();
//                    Log.w(TAG, "Database close count negative, resetting to 0 (Thread: " + threadId + ")");
//                }
//
//                // Đóng entry
//                entry.lock.lock();
//                try {
//                    if (!entry.isClosed()) {
//                        Log.d(TAG, "Closing database for thread: " + threadId);
//                        entry.close();
//                    }
//
//                    // Xóa entry khỏi map
//                    threadDatabaseEntries.remove(threadId);
//                } catch (Exception e) {
//                    Log.e(TAG, "Error closing database: " + e.getMessage(), e);
//                } finally {
//                    entry.lock.unlock();
//                }
//            }
//        } finally {
//            mLock.unlock();
//        }
//    }

    /**
     * Giảm reference count và đóng database nếu không còn reference nào khác
     */
    public void closeDatabase() {
        long threadId = Thread.currentThread().getId();
        Log.d(TAG, "closeDatabase called in thread: " + threadId);

        mLock.lock();
        try {
            // Lấy entry cho thread hiện tại
            ThreadDatabaseEntry entry = threadDatabaseEntries.get(threadId);
            if (entry == null || entry.isClosed()) {
                // Không có entry hoặc đã đóng
                if (entry != null) {
                    threadDatabaseEntries.remove(threadId);
                    Log.d(TAG, "Removed already closed entry for thread: " + threadId);
                }
                return;
            }

            // Giảm reference count
            int count = entry.decrementCount();
            Log.d(TAG, "Database close called: " + count + " (Thread: " + threadId + ")");

            if (count <= 0) {
                // Reset count nếu âm
                if (count < 0) {
                    entry.resetCount();
                    Log.w(TAG, "Database close count negative, resetting to 0 (Thread: " + threadId + ")");
                }

                // Kiểm tra xem có luồng nào khác đang sử dụng cùng database
                boolean inUseByOthers = false;
                for (ThreadDatabaseEntry otherEntry : threadDatabaseEntries.values()) {
                    if (otherEntry != entry && !otherEntry.isClosed() && otherEntry.getCount() > 0) {
                        inUseByOthers = true;
                        break;
                    }
                }

                if (!inUseByOthers) {
                    // Không có luồng nào khác đang sử dụng, an toàn để đóng kết nối
                    entry.lock.lock();
                    try {
                        if (!entry.isClosed()) {
                            Log.d(TAG, "Closing database for thread: " + threadId);
                            entry.close();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing database: " + e.getMessage(), e);
                    } finally {
                        entry.lock.unlock();
                    }
                } else {
                    Log.d(TAG, "Not closing database - still in use by other threads (Thread: " + threadId + ")");
                }

                // Xóa entry khỏi map ngay cả khi không đóng database
                threadDatabaseEntries.remove(threadId);
            }
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Bắt đầu transaction
     */
    public void beginTransaction() {
        long threadId = Thread.currentThread().getId();
        mLock.lock();
        try {
            ThreadDatabaseEntry entry = threadDatabaseEntries.get(threadId);
            if (entry != null && !entry.isClosed() && entry.database != null) {
                try {
                    entry.database.beginTransaction();
                } catch (Exception e) {
                    Log.e(TAG, "Error beginning transaction: " + e.getMessage());
                    threadDatabaseEntries.remove(threadId);
                }
            }
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Đánh dấu transaction thành công
     */
    public void setTransactionSuccessful() {
        long threadId = Thread.currentThread().getId();
        mLock.lock();
        try {
            ThreadDatabaseEntry entry = threadDatabaseEntries.get(threadId);
            if (entry != null && !entry.isClosed() && entry.database != null) {
                try {
                    if (entry.database.inTransaction()) {
                        entry.database.setTransactionSuccessful();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error setting transaction successful: " + e.getMessage());
                    threadDatabaseEntries.remove(threadId);
                }
            }
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Kết thúc transaction
     */
    public void endTransaction() {
        long threadId = Thread.currentThread().getId();
        mLock.lock();
        try {
            ThreadDatabaseEntry entry = threadDatabaseEntries.get(threadId);
            if (entry != null && !entry.isClosed() && entry.database != null) {
                try {
                    if (entry.database.inTransaction()) {
                        entry.database.endTransaction();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error ending transaction: " + e.getMessage());
                    threadDatabaseEntries.remove(threadId);
                }
            }
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Đóng tất cả các connection và xóa khỏi map
     */
    public void reset() {
        mLock.lock();
        try {
            Log.d(TAG, "Resetting DatabaseManager - closing all connections");

            // Sử dụng ArrayList mới để tránh ConcurrentModificationException
            for (Long threadId : new ArrayList<>(threadDatabaseEntries.keySet())) {
                ThreadDatabaseEntry entry = threadDatabaseEntries.get(threadId);
                if (entry != null) {
                    entry.lock.lock();
                    try {
                        if (!entry.isClosed()) {
                            Log.d(TAG, "Closing database for thread: " + threadId);
                            try {
                                entry.close();
                            } catch (Exception e) {
                                Log.e(TAG, "Error closing database in reset for thread: " + threadId, e);
                            }
                        }
                    } finally {
                        entry.lock.unlock();
                    }
                }
            }

            threadDatabaseEntries.clear();
            Log.d(TAG, "DatabaseManager reset - all connections closed");
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Kiểm tra xem thread có connection đang mở không
     */
    public boolean isDatabaseOpenForThread(long threadId) {
        mLock.lock();
        try {
            ThreadDatabaseEntry entry = threadDatabaseEntries.get(threadId);
            return entry != null && !entry.isClosed();
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Lấy số lượng reference cho thread
     */
    public int getOpenCountForThread(long threadId) {
        mLock.lock();
        try {
            ThreadDatabaseEntry entry = threadDatabaseEntries.get(threadId);
            return entry != null ? entry.getCount() : 0;
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Đóng connection và clean up cho một thread cụ thể
     */
    public void closeThreadConnection(long threadId) {
        mLock.lock();
        try {
            ThreadDatabaseEntry entry = threadDatabaseEntries.get(threadId);
            if (entry != null) {
                entry.lock.lock();
                try {
                    if (!entry.isClosed()) {
                        Log.d(TAG, "Force closing database for thread: " + threadId);
                        entry.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error force closing database for thread: " + threadId, e);
                } finally {
                    entry.lock.unlock();
                }

                threadDatabaseEntries.remove(threadId);
            }
        } finally {
            mLock.unlock();
        }
    }
}