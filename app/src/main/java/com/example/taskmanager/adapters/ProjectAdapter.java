package com.example.taskmanager.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.models.Project;
import com.example.taskmanager.models.User;
import com.example.taskmanager.utils.DateTimeUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private Context context;
    private List<Project> projectList;
    private OnProjectClickListener listener;

    // Interface cho sự kiện click vào dự án
    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    public ProjectAdapter(Context context, List<Project> projectList, OnProjectClickListener listener) {
        this.context = context;
        this.projectList = projectList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.bind(project);
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public class ProjectViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProjectName, tvProjectDescription, tvProgress, tvDueDate, tvMembersCount, tvPriority;
        private ProgressBar progressBar;
        private ChipGroup chipGroupMembers;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectName = itemView.findViewById(R.id.tv_project_name);
            tvProjectDescription = itemView.findViewById(R.id.tv_project_description);
            tvProgress = itemView.findViewById(R.id.tv_progress);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvMembersCount = itemView.findViewById(R.id.tv_members_count);
            tvPriority = itemView.findViewById(R.id.tv_priority);
            progressBar = itemView.findViewById(R.id.progress_bar);
            chipGroupMembers = itemView.findViewById(R.id.chip_group_members);

            // Thiết lập sự kiện click cho item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProjectClick(projectList.get(position));
                }
            });
        }

        public void bind(Project project) {
            // Hiển thị thông tin cơ bản của dự án
            tvProjectName.setText(project.getName());
            tvProjectDescription.setText(project.getDescription());

            // Tính và hiển thị tiến độ
            int completionPercentage = project.getCompletionPercentage();
            tvProgress.setText(completionPercentage + "%");
            progressBar.setProgress(completionPercentage);

            // Hiển thị ngày kết thúc
            String formattedDueDate = DateTimeUtils.formatDisplayDate(project.getEndDate());
            tvDueDate.setText(formattedDueDate);

            // Hiển thị số lượng thành viên
            List<User> members = project.getMembers();
            int membersCount = members != null ? members.size() : 0;
            tvMembersCount.setText(String.valueOf(membersCount));

            // Hiển thị độ ưu tiên với màu sắc tương ứng
            tvPriority.setText(project.getPriority());
            int backgroundColor;
            switch (project.getPriority()) {
                case "Cao":
                    backgroundColor = R.color.colorError;
                    break;
                case "Trung bình":
                    backgroundColor = R.color.colorWarning;
                    break;
                case "Thấp":
                    backgroundColor = R.color.colorSuccess;
                    break;
                default:
                    backgroundColor = R.color.colorNeutral;
                    break;
            }
            GradientDrawable drawable = (GradientDrawable) tvPriority.getBackground();
            drawable.setColor(ContextCompat.getColor(context, backgroundColor));

            // Hiển thị chips cho các thành viên
            chipGroupMembers.removeAllViews();
            if (members != null && !members.isEmpty()) {
                // Hiển thị tối đa 3 thành viên dưới dạng chip
                int maxMembers = Math.min(members.size(), 3);
                for (int i = 0; i < maxMembers; i++) {
                    User member = members.get(i);
                    Chip chip = new Chip(context);
                    chip.setText(member.getFullName());
                    chip.setChipBackgroundColorResource(R.color.colorPrimaryLight);
                    chip.setTextColor(ContextCompat.getColor(context, R.color.colorTextPrimary));
                    chipGroupMembers.addView(chip);
                }

                // Nếu có nhiều hơn 3 thành viên, hiển thị thêm một chip "+n"
                if (members.size() > 3) {
                    Chip moreChip = new Chip(context);
                    moreChip.setText("+" + (members.size() - 3));
                    moreChip.setChipBackgroundColorResource(R.color.colorNeutral);
                    moreChip.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                    chipGroupMembers.addView(moreChip);
                }
            }
        }
    }
}