package com.example.taskmanager.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.activities.TaskDetailActivity;
import com.example.taskmanager.adapters.NotificationAdapter;
import com.example.taskmanager.database.NotificationDAO;
import com.example.taskmanager.models.Notification;
import com.example.taskmanager.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private Button btnMarkAllRead;

    private NotificationAdapter notificationAdapter;
    private NotificationDAO notificationDAO;
    private SessionManager sessionManager;

    private List<Notification> notificationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        // Khởi tạo các thành phần giao diện
        initViews(view);

        // Khởi tạo database và session
        notificationDAO = new NotificationDAO(getContext());
        sessionManager = new SessionManager(getContext());

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập sự kiện click
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        tvEmpty = view.findViewById(R.id.tv_empty);
        progressBar = view.findViewById(R.id.progress_bar);
        btnMarkAllRead = view.findViewById(R.id.btn_mark_all_read);
    }

    private void setupRecyclerView() {
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(), notificationList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(notificationAdapter);
    }

    private void setupClickListeners() {
        // Sự kiện khi nhấn nút "Đánh dấu tất cả đã đọc"
        btnMarkAllRead.setOnClickListener(v -> markAllNotificationsAsRead());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải thông báo khi fragment được resume
        loadNotifications();
    }

    private void loadNotifications() {
        showProgress(true);

        new Thread(() -> {
            // Mở kết nối đến database
            notificationDAO.open();

            // Lấy danh sách thông báo của người dùng
            final List<Notification> notifications = notificationDAO.getNotificationsByUser(sessionManager.getUserId());

            // Đóng kết nối database
            notificationDAO.close();

            // Cập nhật UI trên main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    showProgress(false);

                    notificationList.clear();
                    notificationList.addAll(notifications);
                    notificationAdapter.notifyDataSetChanged();

                    // Hiển thị nút "Đánh dấu tất cả đã đọc" nếu có thông báo chưa đọc
                    boolean hasUnread = hasUnreadNotifications(notifications);
                    btnMarkAllRead.setEnabled(hasUnread);

                    // Hiển thị thông báo nếu không có thông báo nào
                    if (notificationList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
    }

    private boolean hasUnreadNotifications(List<Notification> notifications) {
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                return true;
            }
        }
        return false;
    }

    private void markAllNotificationsAsRead() {
        new Thread(() -> {
            // Mở kết nối đến database
            notificationDAO.open();

            // Đánh dấu tất cả thông báo đã đọc
            int count = notificationDAO.markAllNotificationsAsRead(sessionManager.getUserId());

            // Đóng kết nối database
            notificationDAO.close();

            // Cập nhật UI trên main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Cập nhật trạng thái thông báo trong danh sách
                    for (Notification notification : notificationList) {
                        notification.setRead(true);
                    }
                    notificationAdapter.notifyDataSetChanged();

                    // Vô hiệu hóa nút "Đánh dấu tất cả đã đọc"
                    btnMarkAllRead.setEnabled(false);

                    // Hiển thị thông báo
                    Toast.makeText(getContext(), count + " " + getString(R.string.notifications) + " " + getString(R.string.mark_all_as_read), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Đánh dấu thông báo đã đọc
        markNotificationAsRead(notification);

        // Mở màn hình liên quan đến thông báo
        if (notification.getRelatedId() != null) {
            String type = notification.getType();

            if (type.equals(Notification.TYPE_NEW_TASK) ||
                    type.equals(Notification.TYPE_TASK_UPDATED) ||
                    type.equals(Notification.TYPE_TASK_REMINDER) ||
                    type.equals(Notification.TYPE_TASK_OVERDUE) ||
                    type.equals(Notification.TYPE_TASK_COMPLETED)) {

                // Mở màn hình chi tiết nhiệm vụ
                Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
                intent.putExtra("TASK_ID", notification.getRelatedId());
                startActivity(intent);
            }
        }
    }

    private void markNotificationAsRead(Notification notification) {
        if (!notification.isRead()) {
            // Cập nhật trạng thái thông báo
            notification.setRead(true);
            notificationAdapter.notifyDataSetChanged();

            // Cập nhật trong database
            new Thread(() -> {
                notificationDAO.open();
                notificationDAO.markNotificationAsRead(notification.getId());
                notificationDAO.close();
            }).start();
        }
    }
}