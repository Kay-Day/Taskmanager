package com.example.taskmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.models.Notification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList;
    private OnNotificationClickListener listener;

    // Interface cho sự kiện click vào thông báo
    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(Context context, List<Notification> notificationList, OnNotificationClickListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgNotificationIcon;
        private TextView tvNotificationTitle, tvNotificationMessage, tvNotificationTime;
        private View viewUnreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imgNotificationIcon = itemView.findViewById(R.id.img_notification_icon);
            tvNotificationTitle = itemView.findViewById(R.id.tv_notification_title);
            tvNotificationMessage = itemView.findViewById(R.id.tv_notification_message);
            tvNotificationTime = itemView.findViewById(R.id.tv_notification_time);
            viewUnreadIndicator = itemView.findViewById(R.id.view_unread_indicator);

            // Thiết lập sự kiện click cho item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNotificationClick(notificationList.get(position));
                }
            });
        }

        public void bind(Notification notification) {
            // Hiển thị tiêu đề và nội dung thông báo
            tvNotificationTitle.setText(notification.getTitle());
            tvNotificationMessage.setText(notification.getMessage());

            // Hiển thị thời gian thông báo theo định dạng tương đối
            tvNotificationTime.setText(getRelativeTimeSpan(notification.getCreatedAt()));

            // Hiển thị icon dựa trên loại thông báo
            int iconResId;
            switch (notification.getType()) {
                case Notification.TYPE_NEW_TASK:
                    iconResId = R.drawable.ic_notification_new_task;
                    break;
                case Notification.TYPE_TASK_UPDATED:
                    iconResId = R.drawable.ic_notification_update;
                    break;
                case Notification.TYPE_TASK_REMINDER:
                    iconResId = R.drawable.ic_notification_reminder;
                    break;
                case Notification.TYPE_TASK_OVERDUE:
                    iconResId = R.drawable.ic_notification_overdue;
                    break;
                case Notification.TYPE_TASK_COMPLETED:
                    iconResId = R.drawable.ic_notification_completed;
                    break;
                default:
                    iconResId = R.drawable.ic_notification;
                    break;
            }
            imgNotificationIcon.setImageResource(iconResId);

            // Hiển thị chỉ báo chưa đọc nếu thông báo chưa được đọc
            viewUnreadIndicator.setVisibility(notification.isRead() ? View.INVISIBLE : View.VISIBLE);
        }

        private String getRelativeTimeSpan(String createdAtStr) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date createdAt = sdf.parse(createdAtStr);
                Date now = new Date();

                long diffInMillies = now.getTime() - createdAt.getTime();
                long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillies);
                long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillies);
                long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillies);

                if (diffInMinutes < 1) {
                    return context.getString(R.string.just_now);
                } else if (diffInMinutes < 60) {
                    return diffInMinutes + " " + context.getString(R.string.minutes_ago);
                } else if (diffInHours < 24) {
                    return diffInHours + " " + context.getString(R.string.hours_ago);
                } else if (diffInDays < 7) {
                    return diffInDays + " " + context.getString(R.string.days_ago);
                } else {
                    // Cho các thông báo cũ hơn 7 ngày, hiển thị ngày tháng
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    return displayFormat.format(createdAt);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return createdAtStr;
            }
        }
    }
}