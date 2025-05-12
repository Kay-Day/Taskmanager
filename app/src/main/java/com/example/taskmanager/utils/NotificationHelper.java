package com.example.taskmanager.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.taskmanager.R;
import com.example.taskmanager.activities.MainActivity;
import com.example.taskmanager.activities.TaskDetailActivity;
import com.example.taskmanager.models.Task;

public class NotificationHelper {

    private static final String CHANNEL_ID = "task_manager_channel";
    private static final String CHANNEL_NAME = "Task Manager";
    private static final String CHANNEL_DESCRIPTION = "Thông báo từ Task Manager";

    private Context context;
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo notification channel (yêu cầu từ Android 8.0 trở lên)
        createNotificationChannel();
    }

    // Tạo notification channel
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Hiển thị thông báo nhiệm vụ mới
    public void showNewTaskNotification(Task task) {
        // Intent khi click vào thông báo
        Intent intent = new Intent(context, TaskDetailActivity.class);
        intent.putExtra("TASK_ID", task.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Âm thanh thông báo mặc định
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Xây dựng thông báo
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Nhiệm vụ mới")
                .setContentText("Bạn đã được giao nhiệm vụ: " + task.getTitle())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        // Hiển thị thông báo
        notificationManager.notify((int) task.getId(), notificationBuilder.build());
    }

    // Hiển thị thông báo nhiệm vụ được cập nhật
    public void showTaskUpdatedNotification(Task task) {
        // Intent khi click vào thông báo
        Intent intent = new Intent(context, TaskDetailActivity.class);
        intent.putExtra("TASK_ID", task.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Âm thanh thông báo mặc định
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Xây dựng thông báo
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Nhiệm vụ đã cập nhật")
                .setContentText("Nhiệm vụ đã được cập nhật: " + task.getTitle())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        // Hiển thị thông báo
        int notificationId = (int) task.getId() + 100000; // Để tránh trùng ID với thông báo khác
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    // Hiển thị thông báo nhắc nhở nhiệm vụ sắp đến hạn
    public void showTaskReminderNotification(Task task) {
        // Intent khi click vào thông báo
        Intent intent = new Intent(context, TaskDetailActivity.class);
        intent.putExtra("TASK_ID", task.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Âm thanh thông báo mặc định
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Xây dựng thông báo
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Nhiệm vụ sắp đến hạn")
                .setContentText("Nhiệm vụ sắp đến hạn: " + task.getTitle())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        // Hiển thị thông báo
        int notificationId = (int) task.getId() + 200000; // Để tránh trùng ID với thông báo khác
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    // Hiển thị thông báo nhiệm vụ quá hạn
    public void showTaskOverdueNotification(Task task) {
        // Intent khi click vào thông báo
        Intent intent = new Intent(context, TaskDetailActivity.class);
        intent.putExtra("TASK_ID", task.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Âm thanh thông báo mặc định
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Xây dựng thông báo
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Nhiệm vụ quá hạn")
                .setContentText("Nhiệm vụ đã quá hạn: " + task.getTitle())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        // Hiển thị thông báo
        int notificationId = (int) task.getId() + 300000; // Để tránh trùng ID với thông báo khác
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    // Hiển thị thông báo chung
    public void showGeneralNotification(String title, String message) {
        // Intent khi click vào thông báo
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Âm thanh thông báo mặc định
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Xây dựng thông báo
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        // Hiển thị thông báo
        int notificationId = (int) System.currentTimeMillis() / 1000;
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}