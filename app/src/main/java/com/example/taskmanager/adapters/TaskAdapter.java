package com.example.taskmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.database.UserDAO;
import com.example.taskmanager.models.Task;
import com.example.taskmanager.models.User;
import com.example.taskmanager.utils.DateTimeUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context context;
    private List<Task> taskList;
    private OnTaskClickListener listener;
    private UserDAO userDAO;

    // Interface cho sự kiện click vào task
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskAdapter(Context context, List<Task> taskList, OnTaskClickListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
        this.userDAO = new UserDAO(context);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        private View viewPriorityIndicator;
        private TextView tvTaskTitle, tvTaskDescription, tvTaskStatus, tvTaskDueDate, tvDaysRemaining, tvAssigneeName;
        private CircleImageView imgAssigneeAvatar;
        private Button btnViewComments, btnEditTask;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPriorityIndicator = itemView.findViewById(R.id.view_priority_indicator);
            tvTaskTitle = itemView.findViewById(R.id.tv_task_title);
            tvTaskDescription = itemView.findViewById(R.id.tv_task_description);
            tvTaskStatus = itemView.findViewById(R.id.tv_task_status);
            tvTaskDueDate = itemView.findViewById(R.id.tv_task_due_date);
            tvDaysRemaining = itemView.findViewById(R.id.tv_days_remaining);
            tvAssigneeName = itemView.findViewById(R.id.tv_assignee_name);
            imgAssigneeAvatar = itemView.findViewById(R.id.img_assignee_avatar);
            btnViewComments = itemView.findViewById(R.id.btn_view_comments);
            btnEditTask = itemView.findViewById(R.id.btn_edit_task);

            // Thiết lập sự kiện click cho item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTaskClick(taskList.get(position));
                }
            });
        }

        public void bind(Task task) {
            // Hiển thị thông tin cơ bản của task
            tvTaskTitle.setText(task.getTitle());

            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                tvTaskDescription.setVisibility(View.VISIBLE);
                tvTaskDescription.setText(task.getDescription());
            } else {
                tvTaskDescription.setVisibility(View.GONE);
            }

            // Hiển thị trạng thái với màu sắc tương ứng
            tvTaskStatus.setText(task.getStatus());
            int statusBackgroundId;
            switch (task.getStatus()) {
                case "Hoàn thành":
                    statusBackgroundId = R.drawable.bg_status_completed;
                    break;
                case "Đang thực hiện":
                    statusBackgroundId = R.drawable.bg_status_in_progress;
                    break;
                case "Tạm hoãn":
                    statusBackgroundId = R.drawable.bg_status_delayed;
                    break;
                default: // Chưa bắt đầu
                    statusBackgroundId = R.drawable.bg_status_not_started;
                    break;
            }
            tvTaskStatus.setBackgroundResource(statusBackgroundId);

            // Hiển thị priority indicator với màu tương ứng
            int priorityColorId;
            switch (task.getPriority()) {
                case "Cao":
                    priorityColorId = R.color.colorError;
                    break;
                case "Trung bình":
                    priorityColorId = R.color.colorWarning;
                    break;
                case "Thấp":
                    priorityColorId = R.color.colorSuccess;
                    break;
                default:
                    priorityColorId = R.color.colorNeutral;
                    break;
            }
            viewPriorityIndicator.setBackgroundColor(ContextCompat.getColor(context, priorityColorId));

            // Hiển thị ngày đến hạn
            String formattedDueDate = DateTimeUtils.formatDisplayDate(task.getDueDate());
            tvTaskDueDate.setText(formattedDueDate);

            // Hiển thị số ngày còn lại
            int daysRemaining = task.getDaysRemaining();
            String daysText;
            if (daysRemaining < 0) {
                // Quá hạn
                daysText = "(" + context.getString(R.string.overdue) + " " + Math.abs(daysRemaining) + " " + context.getString(R.string.days) + ")";
                tvDaysRemaining.setTextColor(ContextCompat.getColor(context, R.color.colorError));
            } else if (daysRemaining == 0) {
                // Đến hạn hôm nay
                daysText = "(" + context.getString(R.string.due_today) + ")";
                tvDaysRemaining.setTextColor(ContextCompat.getColor(context, R.color.colorWarning));
            } else {
                // Còn thời gian
                daysText = "(" + context.getString(R.string.remaining) + " " + daysRemaining + " " + context.getString(R.string.days) + ")";
                tvDaysRemaining.setTextColor(ContextCompat.getColor(context, R.color.colorTextSecondary));
            }
            tvDaysRemaining.setText(daysText);

            // Hiển thị thông tin người được giao
            Long assignedToId = task.getAssignedTo();
            if (assignedToId != null) {
                // Lấy thông tin người được giao từ database
                userDAO.open();
                User assignee = userDAO.getUserById(assignedToId);
                userDAO.close();

                if (assignee != null) {
                    tvAssigneeName.setText(assignee.getFullName());
                    // Đặt ảnh đại diện nếu có
                    // imgAssigneeAvatar.setImageResource(R.drawable.avatar_placeholder);
                }
            } else {
                tvAssigneeName.setText(R.string.unassigned);
            }

            // Thiết lập sự kiện cho các nút
            btnViewComments.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });

            btnEditTask.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });
        }
    }
}