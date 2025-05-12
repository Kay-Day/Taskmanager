package com.example.taskmanager.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.taskmanager.R;
import com.example.taskmanager.activities.MainActivity;
import com.example.taskmanager.database.NotificationDAO;
import com.example.taskmanager.database.TaskDAO;
import com.example.taskmanager.models.Task;
import com.example.taskmanager.utils.NotificationHelper;
import com.example.taskmanager.utils.SessionManager;

import java.util.List;

public class NotificationService extends Service {

    private static final String TAG = "NotificationService";
    private static final long CHECK_INTERVAL = 60 * 60 * 1000; // Kiểm tra mỗi 1 giờ
    private static final String CHANNEL_ID = "task_manager_service_channel";
    private static final int NOTIFICATION_ID = 1001;

    private Handler handler;
    private Runnable checkTasksRunnable;
    private TaskDAO taskDAO;
    private NotificationDAO notificationDAO;
    private NotificationHelper notificationHelper;
    private SessionManager sessionManager;
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");

        // Tạo notification channel cho Android 8.0+
        createNotificationChannel();

        handler = new Handler();
        taskDAO = new TaskDAO(this);
        notificationDAO = new NotificationDAO(this);
        notificationHelper = new NotificationHelper(this);
        sessionManager = new SessionManager(this);

        // Tạo runnable để kiểm tra nhiệm vụ định kỳ
        checkTasksRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    checkTasks();
                    // Lên lịch chạy lại
                    handler.postDelayed(this, CHECK_INTERVAL);
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        // ĐÂY LÀ ĐIỂM QUAN TRỌNG: Bắt đầu foreground service ngay lập tức
        startForeground(NOTIFICATION_ID, createForegroundNotification());

        if (!isRunning) {
            isRunning = true;
            // Bắt đầu kiểm tra ngay lập tức và lên lịch kiểm tra định kỳ
            handler.post(checkTasksRunnable);
        }

        // Dịch vụ sẽ tự động khởi động lại nếu bị hệ thống dừng
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service destroyed");

        // Hủy lịch kiểm tra
        isRunning = false;
        handler.removeCallbacks(checkTasksRunnable);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Tạo notification channel cho Android 8.0+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task Manager Service",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Dịch vụ theo dõi nhiệm vụ");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Tạo thông báo foreground service
     */
    private Notification createForegroundNotification() {
        // Tạo intent khi người dùng nhấp vào thông báo
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Tạo builder notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Đảm bảo có icon này
                .setContentTitle("Task Manager đang chạy")
                .setContentText("Đang theo dõi nhiệm vụ của bạn")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        return builder.build();
    }

    // Kiểm tra các nhiệm vụ sắp đến hạn và quá hạn
    private void checkTasks() {
        if (!sessionManager.isLoggedIn()) {
            // Không có người dùng đăng nhập, không cần kiểm tra
            return;
        }

        long userId = sessionManager.getUserId();
        Log.d(TAG, "Checking tasks for user ID: " + userId);

        TaskDAO localTaskDAO = null;
        NotificationDAO localNotificationDAO = null;

        try {
            localTaskDAO = new TaskDAO(this);
            localTaskDAO.open();

            localNotificationDAO = new NotificationDAO(this);
            localNotificationDAO.open();

            // Kiểm tra nhiệm vụ sắp đến hạn
            List<Task> upcomingTasks = localTaskDAO.getUpcomingTasks();
            Log.d(TAG, "Found " + upcomingTasks.size() + " upcoming tasks");

            for (Task task : upcomingTasks) {
                // Chỉ gửi thông báo cho người được giao nhiệm vụ
                if (task.getAssignedTo() != null && task.getAssignedTo() == userId) {
                    // Kiểm tra xem thông báo đã được gửi cho nhiệm vụ này chưa
                    if (!hasNotificationForTask(localNotificationDAO, userId, task.getId(), com.example.taskmanager.models.Notification.TYPE_TASK_REMINDER)) {
                        // Tạo thông báo trong cơ sở dữ liệu
                        com.example.taskmanager.models.Notification notification = new com.example.taskmanager.models.Notification(
                                userId,
                                "Nhiệm vụ sắp đến hạn",
                                "Nhiệm vụ \"" + task.getTitle() + "\" sẽ đến hạn trong " + task.getDaysRemaining() + " ngày nữa.",
                                task.getId(),
                                com.example.taskmanager.models.Notification.TYPE_TASK_REMINDER
                        );
                        localNotificationDAO.createNotification(notification);

                        // Hiển thị thông báo
                        notificationHelper.showTaskReminderNotification(task);
                    }
                }
            }

            // Kiểm tra nhiệm vụ quá hạn
            List<Task> overdueTasks = localTaskDAO.getOverdueTasks();
            Log.d(TAG, "Found " + overdueTasks.size() + " overdue tasks");

            for (Task task : overdueTasks) {
                // Chỉ gửi thông báo cho người được giao nhiệm vụ
                if (task.getAssignedTo() != null && task.getAssignedTo() == userId) {
                    // Kiểm tra xem thông báo đã được gửi cho nhiệm vụ này chưa
                    if (!hasNotificationForTask(localNotificationDAO, userId, task.getId(), com.example.taskmanager.models.Notification.TYPE_TASK_OVERDUE)) {
                        // Tạo thông báo trong cơ sở dữ liệu
                        com.example.taskmanager.models.Notification notification = new com.example.taskmanager.models.Notification(
                                userId,
                                "Nhiệm vụ quá hạn",
                                "Nhiệm vụ \"" + task.getTitle() + "\" đã quá hạn.",
                                task.getId(),
                                com.example.taskmanager.models.Notification.TYPE_TASK_OVERDUE
                        );
                        localNotificationDAO.createNotification(notification);

                        // Hiển thị thông báo
                        notificationHelper.showTaskOverdueNotification(task);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking tasks", e);
        } finally {
            // Đóng các kết nối database
            if (localTaskDAO != null) {
                localTaskDAO.close();
            }
            if (localNotificationDAO != null) {
                localNotificationDAO.close();
            }
        }
    }

    // Kiểm tra xem thông báo đã được gửi cho nhiệm vụ cụ thể chưa
    private boolean hasNotificationForTask(NotificationDAO dao, long userId, long taskId, String type) {
        List<com.example.taskmanager.models.Notification> notifications = dao.getNotificationsByUser(userId);

        for (com.example.taskmanager.models.Notification notification : notifications) {
            if (notification.getRelatedId() != null &&
                    notification.getRelatedId() == taskId &&
                    notification.getType().equals(type)) {
                return true;
            }
        }

        return false;
    }
}