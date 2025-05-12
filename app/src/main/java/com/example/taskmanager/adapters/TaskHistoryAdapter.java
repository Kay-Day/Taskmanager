package com.example.taskmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.models.TaskHistory;
import com.example.taskmanager.utils.DateTimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskHistoryAdapter extends RecyclerView.Adapter<TaskHistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<TaskHistory> historyList;

    public TaskHistoryAdapter(Context context, List<TaskHistory> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        TaskHistory history = historyList.get(position);
        holder.bind(history);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUsername, tvChangeDescription, tvChangeTime;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvChangeDescription = itemView.findViewById(R.id.tv_change_description);
            tvChangeTime = itemView.findViewById(R.id.tv_change_time);
        }

        public void bind(TaskHistory history) {
            // Hiển thị người thay đổi
            if (history.getUser() != null) {
                tvUsername.setText(history.getUser().getFullName());
            } else {
                tvUsername.setText(R.string.unknown_user);
            }

            // Hiển thị mô tả thay đổi
            String fieldName = getFieldDisplayName(history.getFieldChanged());
            String changeText = fieldName + " " +
                    context.getString(R.string.changed_from) + " '" +
                    history.getOldValue() + "' " +
                    context.getString(R.string.to) + " '" +
                    history.getNewValue() + "'";
            tvChangeDescription.setText(changeText);

            // Định dạng và hiển thị thời gian thay đổi
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date changeDate = inputFormat.parse(history.getChangedAt());

                // Định dạng hiển thị
                String formattedDate = getRelativeTimeSpan(changeDate);
                tvChangeTime.setText(formattedDate);
            } catch (ParseException e) {
                tvChangeTime.setText(history.getChangedAt());
            }
        }

        private String getFieldDisplayName(String fieldName) {
            switch (fieldName) {
                case "title":
                    return context.getString(R.string.task_title);
                case "description":
                    return context.getString(R.string.task_description);
                case "assigned_to":
                    return context.getString(R.string.assignee);
                case "priority":
                    return context.getString(R.string.priority);
                case "status":
                    return context.getString(R.string.status);
                case "resource_link":
                    return context.getString(R.string.resource_link);
                case "start_date":
                    return context.getString(R.string.start_date);
                case "due_date":
                    return context.getString(R.string.due_date);
                default:
                    return fieldName;
            }
        }

        private String getRelativeTimeSpan(Date date) {
            if (date == null) {
                return "";
            }

            long currentTime = System.currentTimeMillis();
            long historyTime = date.getTime();
            long diffTime = currentTime - historyTime;

            // Chuyển đổi sang giây, phút, giờ, ngày
            long seconds = diffTime / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                if (days == 1) {
                    return context.getString(R.string.yesterday);
                } else if (days < 7) {
                    return days + " " + context.getString(R.string.days_ago);
                } else {
                    return DateTimeUtils.formatDisplayDate(DateTimeUtils.formatDate(date));
                }
            } else if (hours > 0) {
                return hours + " " + context.getString(R.string.hours_ago);
            } else if (minutes > 0) {
                return minutes + " " + context.getString(R.string.minutes_ago);
            } else {
                return context.getString(R.string.just_now);
            }
        }
    }
}