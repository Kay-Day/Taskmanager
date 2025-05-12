//package com.example.taskmanager.services;
//
//import android.app.Service;
//import android.content.Intent;
//import android.os.Handler;
//import android.os.IBinder;
//import android.util.Log;
//
//import androidx.annotation.Nullable;
//
//import com.example.taskmanager.database.NotificationDAO;
//import com.example.taskmanager.database.TaskDAO;
//import com.example.taskmanager.models.Notification;
//import com.example.taskmanager.models.Task;
//import com.example.taskmanager.utils.NotificationHelper;
//import com.example.taskmanager.utils.SessionManager;
//
//import java.util.List;
//
//public class NotificationService extends Service {
//
//    private static final String TAG = "NotificationService";
//    private static final long CHECK_INTERVAL = 60 * 60 * 1000; // Kiểm tra mỗi 1 giờ
//
//    private Handler handler;
//    private Runnable checkTasksRunnable;
//    private TaskDAO taskDAO;
//    private NotificationDAO notificationDAO;
//    private NotificationHelper notificationHelper;
//    private SessionManager sessionManager;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Log.d(TAG, "Service created");
//
//        handler = new Handler();
//        taskDAO = new TaskDAO(this);
//        notificationDAO = new NotificationDAO(this);
//        notificationHelper = new NotificationHelper(this);
//        sessionManager = new SessionManager(this);
//
//        // Tạo runnable để kiểm tra nhiệm vụ định kỳ
//        checkTasksRunnable = new Runnable() {
//            @Override
//            public void run() {
//                checkTasks();
//                // Lên lịch chạy lại
//                handler.postDelayed(this, CHECK_INTERVAL);
//            }
//        };
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d(TAG, "Service started");
//
//        // Mở kết nối database
//        taskDAO.open();
//        notificationDAO.open();
//
//        // Bắt đầu kiểm tra ngay lập tức và lên lịch kiểm tra định kỳ
//        handler.post(checkTasksRunnable);
//
//        // Dịch vụ sẽ tự động khởi động lại nếu bị hệ thống dừng
//        return START_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        Log.d(TAG, "Service destroyed");
//
//        // Hủy lịch kiểm tra
//        handler.removeCallbacks(checkTasksRunnable);
//
//        // Đóng kết nối database
//        taskDAO.close();
//        notificationDAO.close();
//
//        super.onDestroy();
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    // Kiểm tra các nhiệm vụ sắp đến hạn và quá hạn
//    private void checkTasks() {
//        if (!sessionManager.isLoggedIn()) {
//            // Không có người dùng đăng nhập, không cần kiểm tra
//            return;
//        }
//
//        long userId = sessionManager.getUserId();
//
//        // Kiểm tra nhiệm vụ sắp đến hạn
//        List<Task> upcomingTasks = taskDAO.getUpcomingTasks();
//        for (Task task : upcomingTasks) {
//            // Chỉ gửi thông báo cho người được giao nhiệm vụ
//            if (task.getAssignedTo() != null && task.getAssignedTo() == userId) {
//                // Kiểm tra xem thông báo đã được gửi cho nhiệm vụ này chưa
//                if (!hasNotificationForTask(userId, task.getId(), Notification.TYPE_TASK_REMINDER)) {
//                    // Tạo thông báo trong cơ sở dữ liệu
//                    Notification notification = new Notification(
//                            userId,
//                            "Nhiệm vụ sắp đến hạn",
//                            "Nhiệm vụ \"" + task.getTitle() + "\" sẽ đến hạn trong " + task.getDaysRemaining() + " ngày nữa.",
//                            task.getId(),
//                            Notification.TYPE_TASK_REMINDER
//                    );
//                    notificationDAO.createNotification(notification);
//
//                    // Hiển thị thông báo
//                    notificationHelper.showTaskReminderNotification(task);
//                }
//            }
//        }
//
//        // Kiểm tra nhiệm vụ quá hạn
//        List<Task> overdueTasks = taskDAO.getOverdueTasks();
//        for (Task task : overdueTasks) {
//            // Chỉ gửi thông báo cho người được giao nhiệm vụ
//            if (task.getAssignedTo() != null && task.getAssignedTo() == userId) {
//                // Kiểm tra xem thông báo đã được gửi cho nhiệm vụ này chưa
//                if (!hasNotificationForTask(userId, task.getId(), Notification.TYPE_TASK_OVERDUE)) {
//                    // Tạo thông báo trong cơ sở dữ liệu
//                    Notification notification = new Notification(
//                            userId,
//                            "Nhiệm vụ quá hạn",
//                            "Nhiệm vụ \"" + task.getTitle() + "\" đã quá hạn.",
//                            task.getId(),
//                            Notification.TYPE_TASK_OVERDUE
//                    );
//                    notificationDAO.createNotification(notification);
//
//                    // Hiển thị thông báo
//                    notificationHelper.showTaskOverdueNotification(task);
//                }
//            }
//        }
//    }
//
//    // Kiểm tra xem thông báo đã được gửi cho nhiệm vụ cụ thể chưa
//    private boolean hasNotificationForTask(long userId, long taskId, String type) {
//        List<Notification> notifications = notificationDAO.getNotificationsByUser(userId);
//
//        for (Notification notification : notifications) {
//            if (notification.getRelatedId() != null &&
//                    notification.getRelatedId() == taskId &&
//                    notification.getType().equals(type)) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//}