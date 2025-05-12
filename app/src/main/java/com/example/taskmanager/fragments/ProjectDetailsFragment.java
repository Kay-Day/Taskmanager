package com.example.taskmanager.fragments;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.adapters.MemberAdapter;
import com.example.taskmanager.database.DatabaseManager;
import com.example.taskmanager.database.ProjectDAO;
import com.example.taskmanager.database.TaskDAO;
import com.example.taskmanager.database.UserDAO;
import com.example.taskmanager.models.Project;
import com.example.taskmanager.models.Task;
import com.example.taskmanager.models.User;
import com.example.taskmanager.utils.DateTimeUtils;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class ProjectDetailsFragment extends Fragment {

    private TextView tvProjectName, tvProjectDescription, tvProjectPriority;
    private TextView tvProjectStartDate, tvProjectEndDate, tvProjectCreatedBy;
    private TextView tvCompletedCount, tvInProgressCount, tvNotStartedCount, tvDelayedCount;
    private LinearProgressIndicator progressIndicator;
    private RecyclerView recyclerViewMembers;
    private TextView tvNoMembers;
    private ProgressBar progressBar;

    private MemberAdapter memberAdapter;
    private List<User> memberList;

    private long projectId;
    private Project project;
    private User createdBy;
    private List<Task> taskList;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_details, container, false);

        // Khởi tạo các thành phần giao diện
        initViews(view);

        // Lấy ID dự án từ arguments
        Bundle args = getArguments();
        if (args != null) {
            projectId = args.getLong("PROJECT_ID", -1);
        }

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Tải dữ liệu
        loadData();

        return view;
    }

    private void initViews(View view) {
        tvProjectName = view.findViewById(R.id.tv_project_name);
        tvProjectDescription = view.findViewById(R.id.tv_project_description);
        tvProjectPriority = view.findViewById(R.id.tv_project_priority);
        tvProjectStartDate = view.findViewById(R.id.tv_project_start_date);
        tvProjectEndDate = view.findViewById(R.id.tv_project_end_date);
        tvProjectCreatedBy = view.findViewById(R.id.tv_project_created_by);
        tvCompletedCount = view.findViewById(R.id.tv_completed_count);
        tvInProgressCount = view.findViewById(R.id.tv_in_progress_count);
        tvNotStartedCount = view.findViewById(R.id.tv_not_started_count);
        tvDelayedCount = view.findViewById(R.id.tv_delayed_count);
        progressIndicator = view.findViewById(R.id.progress_indicator);
        recyclerViewMembers = view.findViewById(R.id.recycler_view_members);
        tvNoMembers = view.findViewById(R.id.tv_no_members);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupRecyclerView() {
        memberList = new ArrayList<>();
        memberAdapter = new MemberAdapter(getContext(), memberList, null); // Không cần xử lý xóa thành viên
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMembers.setAdapter(memberAdapter);
    }

    private void loadData() {
        if (!isAdded() || getContext() == null) {
            return;
        }

        showProgress(true);

        new Thread(() -> {
            DatabaseManager dbManager = null;
            SQLiteDatabase database = null;

            try {
                dbManager = DatabaseManager.getInstance(getContext());
                database = dbManager.openDatabase();

                // Tạo các DAO và thiết lập database cho chúng
                ProjectDAO projectDAO = new ProjectDAO(getContext());
                TaskDAO taskDAO = new TaskDAO(getContext());
                UserDAO userDAO = new UserDAO(getContext());

                projectDAO.setDatabase(database);
                taskDAO.setDatabase(database);
                userDAO.setDatabase(database);

                // Lấy thông tin dự án, thành viên và nhiệm vụ
                final Project project = projectDAO.getProjectById(projectId);
                final List<User> members = new ArrayList<>();
                final List<Task> tasks = new ArrayList<>();
                final User creator;

                if (project != null) {
                    members.addAll(projectDAO.getProjectMembers(projectId));
                    tasks.addAll(taskDAO.getTasksByProject(projectId));
                    creator = userDAO.getUserById(project.getCreatedBy());
                } else {
                    creator = null;
                }

                // Cập nhật UI trên main thread
                if (getActivity() != null && isAdded()) {
                    // Lưu các kết quả vào biến thành viên để sử dụng trong UI thread
                    final Project finalProject = project;
                    final User finalCreator = creator;
                    final List<User> finalMembers = members;
                    final List<Task> finalTasks = tasks;

                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            showProgress(false);

                            // Lưu các kết quả vào biến thành viên
                            ProjectDetailsFragment.this.project = finalProject;
                            createdBy = finalCreator;
                            memberList.clear();
                            memberList.addAll(finalMembers);
                            taskList = finalTasks;

                            // Hiển thị thông tin
                            updateProjectUI();

                            // Cập nhật adapter
                            memberAdapter.notifyDataSetChanged();

                            // Hiển thị thông báo không có thành viên nếu danh sách trống
                            updateEmptyView();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            showProgress(false);
                            Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } finally {
                // Đóng kết nối database
                if (database != null && dbManager != null) {
                    dbManager.closeDatabase();
                }
            }
        }).start();
    }

    private void updateProjectUI() {
        if (project == null) {
            return;
        }

        // Hiển thị thông tin dự án
        tvProjectName.setText(project.getName());
        tvProjectDescription.setText(project.getDescription());
        tvProjectPriority.setText(getString(R.string.priority) + ": " + project.getPriority());
        tvProjectStartDate.setText(getString(R.string.start_date) + ": " + DateTimeUtils.formatDisplayDate(project.getStartDate()));
        tvProjectEndDate.setText(getString(R.string.end_date) + ": " + DateTimeUtils.formatDisplayDate(project.getEndDate()));

        // Hiển thị người tạo dự án
        if (createdBy != null) {
            tvProjectCreatedBy.setText(getString(R.string.created_by) + ": " + createdBy.getFullName());
        } else {
            tvProjectCreatedBy.setText(getString(R.string.created_by) + ": N/A");
        }

        // Tính toán số lượng nhiệm vụ theo trạng thái
        int completedCount = 0;
        int inProgressCount = 0;
        int notStartedCount = 0;
        int delayedCount = 0;

        if (taskList != null) {
            for (Task task : taskList) {
                switch (task.getStatus()) {
                    case "Hoàn thành":
                        completedCount++;
                        break;
                    case "Đang thực hiện":
                        inProgressCount++;
                        break;
                    case "Chưa bắt đầu":
                        notStartedCount++;
                        break;
                    case "Trễ hạn":
                        delayedCount++;
                        break;
                }
            }
        }

        // Hiển thị số lượng nhiệm vụ
        tvCompletedCount.setText(String.valueOf(completedCount));
        tvInProgressCount.setText(String.valueOf(inProgressCount));
        tvNotStartedCount.setText(String.valueOf(notStartedCount));
        tvDelayedCount.setText(String.valueOf(delayedCount));

        // Tính toán tiến độ
        int totalTasks = completedCount + inProgressCount + notStartedCount + delayedCount;
        int progress = totalTasks > 0 ? (completedCount * 100) / totalTasks : 0;

        // Hiển thị tiến độ
        progressIndicator.setProgress(progress);
    }

    private void updateEmptyView() {
        if (memberList.isEmpty()) {
            tvNoMembers.setVisibility(View.VISIBLE);
            recyclerViewMembers.setVisibility(View.GONE);
        } else {
            tvNoMembers.setVisibility(View.GONE);
            recyclerViewMembers.setVisibility(View.VISIBLE);
        }
    }

    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi fragment được hiển thị lại
        if (isAdded() && projectId > 0) {
            loadData();
        }
    }
}

