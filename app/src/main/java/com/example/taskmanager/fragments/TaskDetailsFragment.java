package com.example.taskmanager.fragments;

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
import com.example.taskmanager.adapters.TaskHistoryAdapter;
import com.example.taskmanager.database.ProjectDAO;
import com.example.taskmanager.database.TaskDAO;
import com.example.taskmanager.database.TaskHistoryDAO;
import com.example.taskmanager.database.UserDAO;
import com.example.taskmanager.models.Project;
import com.example.taskmanager.models.Task;
import com.example.taskmanager.models.TaskHistory;
import com.example.taskmanager.models.User;
import com.example.taskmanager.utils.DatabaseUtils;
import com.example.taskmanager.utils.DateTimeUtils;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TaskDetailsFragment extends Fragment {
    private static final String TAG = "TaskDetailsFragment";

    private TextView tvTaskTitle, tvTaskDescription, tvTaskPriority, tvTaskStatus;
    private TextView tvTaskStartDate, tvTaskDueDate, tvTaskProject;
    private TextView tvTaskAssignee, tvTaskCreatedBy, tvTaskDaysRemaining;
    private TextView tvTaskResourceLink;
    private RecyclerView recyclerViewHistory;
    private TextView tvNoHistory;
    private ProgressBar progressBar;

    private TaskHistoryAdapter historyAdapter;
    private List<TaskHistory> historyList;

    private TaskDAO taskDAO;
    private ProjectDAO projectDAO;
    private UserDAO userDAO;
    private TaskHistoryDAO historyDAO;

    private long taskId;
    private Task task;
    private Project project;
    private User assignee;
    private User createdBy;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_details, container, false);

        // Khởi tạo các thành phần giao diện
        initViews(view);

        // Khởi tạo database
        taskDAO = new TaskDAO(getContext());
        projectDAO = new ProjectDAO(getContext());
        userDAO = new UserDAO(getContext());
        historyDAO = new TaskHistoryDAO(getContext());

        // Lấy ID nhiệm vụ từ arguments
        Bundle args = getArguments();
        if (args != null) {
            taskId = args.getLong("TASK_ID", -1);
        }

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Tải dữ liệu
        loadData();

        return view;
    }

    private void initViews(View view) {
        tvTaskTitle = view.findViewById(R.id.tv_task_title);
        tvTaskDescription = view.findViewById(R.id.tv_task_description);
        tvTaskPriority = view.findViewById(R.id.tv_task_priority);
        tvTaskStatus = view.findViewById(R.id.tv_task_status);
        tvTaskStartDate = view.findViewById(R.id.tv_task_start_date);
        tvTaskDueDate = view.findViewById(R.id.tv_task_due_date);
        tvTaskProject = view.findViewById(R.id.tv_task_project);
        tvTaskAssignee = view.findViewById(R.id.tv_task_assignee);
        tvTaskCreatedBy = view.findViewById(R.id.tv_task_created_by);
        tvTaskDaysRemaining = view.findViewById(R.id.tv_task_days_remaining);
        tvTaskResourceLink = view.findViewById(R.id.tv_task_resource_link);
        recyclerViewHistory = view.findViewById(R.id.recycler_view_history);
        tvNoHistory = view.findViewById(R.id.tv_no_history);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupRecyclerView() {
        historyList = new ArrayList<>();
        historyAdapter = new TaskHistoryAdapter(getContext(), historyList);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewHistory.setAdapter(historyAdapter);
    }

//    private void loadData() {
//        showProgress(true);
//
//        new Thread(() -> {
//            TaskDAO localTaskDAO = null;
//            ProjectDAO localProjectDAO = null;
//            UserDAO localUserDAO = null;
//            TaskHistoryDAO localHistoryDAO = null;
//
//            try {
//                // Khởi tạo các DAO cho thread này
//                localTaskDAO = new TaskDAO(getContext());
//                localProjectDAO = new ProjectDAO(getContext());
//                localUserDAO = new UserDAO(getContext());
//                localHistoryDAO = new TaskHistoryDAO(getContext());
//
//                // Mở kết nối cho TaskDAO (đây sẽ là connection chính)
//                localTaskDAO.open();
//
//                // Sử dụng cùng một connection cho các DAO khác
//                SQLiteDatabase db = localTaskDAO.getDatabase();
//
//                localProjectDAO.setDatabase(db);
//                localUserDAO.setDatabase(db);
//                localHistoryDAO.setDatabase(db);
//
//                Log.d(TAG, "All DAOs initialized with shared connection in thread: " + Thread.currentThread().getId());
//
//                try {
//                    // Lấy thông tin nhiệm vụ
//                    final Task loadedTask = localTaskDAO.getTaskById(taskId);
//
//                    if (loadedTask != null) {
//                        // Lấy thông tin dự án
//                        final Project loadedProject = localProjectDAO.getProjectById(loadedTask.getProjectId());
//
//                        // Lấy thông tin người được giao
//                        User loadedAssignee = null;
//                        if (loadedTask.getAssignedTo() != null) {
//                            loadedAssignee = localUserDAO.getUserById(loadedTask.getAssignedTo());
//                        }
//                        final User finalAssignee = loadedAssignee;
//
//                        // Lấy thông tin người tạo
//                        final User loadedCreatedBy = localUserDAO.getUserById(loadedTask.getCreatedBy());
//
//                        // Lấy lịch sử nhiệm vụ
//                        List<TaskHistory> loadedHistory = localHistoryDAO.getTaskHistoryByTaskId(taskId);
//                        final List<TaskHistory> finalHistory = loadedHistory != null ?
//                                new ArrayList<>(loadedHistory) : new ArrayList<>();
//
//                        Log.d(TAG, "All data loaded successfully in thread: " + Thread.currentThread().getId());
//
//                        // Cập nhật UI trên main thread
//                        if (getActivity() != null) {
//                            getActivity().runOnUiThread(() -> {
//                                showProgress(false);
//
//                                // Gán dữ liệu cho biến thành viên
//                                task = loadedTask;
//                                project = loadedProject;
//                                assignee = finalAssignee;
//                                createdBy = loadedCreatedBy;
//
//                                if (finalHistory != null) {
//                                    historyList.clear();
//                                    historyList.addAll(finalHistory);
//                                }
//
//                                if (task != null) {
//                                    // Hiển thị thông tin nhiệm vụ
//                                    updateTaskUI();
//                                }
//
//                                // Cập nhật adapter lịch sử
//                                historyAdapter.notifyDataSetChanged();
//
//                                // Hiển thị thông báo không có lịch sử nếu danh sách trống
//                                updateEmptyView();
//                            });
//                        }
//                    } else {
//                        Log.e(TAG, "No task found with ID: " + taskId);
//                        if (getActivity() != null) {
//                            getActivity().runOnUiThread(() -> {
//                                showProgress(false);
//                                Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
//                            });
//                        }
//                    }
//                } finally {
//                    // Chỉ cần đóng TaskDAO vì nó sở hữu connection
//                    // Các DAO khác sử dụng chung connection nên không cần đóng
//                    Log.d(TAG, "Closing main DAO connection in thread: " + Thread.currentThread().getId());
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Error loading task data: " + e.getMessage(), e);
//                e.printStackTrace();
//                if (getActivity() != null) {
//                    getActivity().runOnUiThread(() -> {
//                        showProgress(false);
//                        Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
//                    });
//                }
//            } finally {
//                // Chỉ cần đóng TaskDAO vì nó sở hữu connection
//                if (localTaskDAO != null) {
//                    localTaskDAO.close();
//                    Log.d(TAG, "Main DAO connection closed in thread: " + Thread.currentThread().getId());
//                }
//            }
//        }).start();
//    }
private void loadData() {
    showProgress(true);

    new Thread(() -> {
        try {
            // Sử dụng DatabaseUtils để thực hiện thao tác cơ sở dữ liệu an toàn
            DatabaseUtils.executeWithTransaction(getContext(),
                    (database, projectDAO, taskDAO, userDAO, historyDAO, commentDAO, eventDAO, notificationDAO) -> {
                        // Lấy thông tin nhiệm vụ
                        Task loadedTask = taskDAO.getTaskById(taskId);

                        if (loadedTask != null) {
                            // Lưu vào biến thành viên của fragment
                            task = loadedTask;

                            // Lấy thông tin dự án
                            project = projectDAO.getProjectById(loadedTask.getProjectId());

                            // Lấy thông tin người được giao
                            if (loadedTask.getAssignedTo() != null) {
                                assignee = userDAO.getUserById(loadedTask.getAssignedTo());
                            }

                            // Lấy thông tin người tạo
                            createdBy = userDAO.getUserById(loadedTask.getCreatedBy());

                            // Lấy lịch sử nhiệm vụ
                            List<TaskHistory> loadedHistory = historyDAO.getTaskHistoryByTaskId(taskId);
                            historyList.clear();
                            if (loadedHistory != null) {
                                historyList.addAll(loadedHistory);
                            }
                        }

                        return null; // Không cần giá trị trả về
                    }
            );

            // Cập nhật UI trên main thread
            if (getActivity() != null && isAdded()) {
                getActivity().runOnUiThread(() -> {
                    showProgress(false);

                    if (task != null) {
                        // Hiển thị thông tin nhiệm vụ
                        updateTaskUI();
                    }

                    // Cập nhật adapter lịch sử
                    historyAdapter.notifyDataSetChanged();

                    // Hiển thị thông báo không có lịch sử nếu danh sách trống
                    updateEmptyView();
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading task data: " + e.getMessage(), e);
            e.printStackTrace();
            if (getActivity() != null && isAdded()) {
                getActivity().runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                });
            }
        }
    }).start();
}

    private void updateTaskUI() {
        // Cập nhật các thông tin nhiệm vụ
        tvTaskTitle.setText(task.getTitle());

        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            tvTaskDescription.setText(task.getDescription());
            tvTaskDescription.setVisibility(View.VISIBLE);
        } else {
            tvTaskDescription.setVisibility(View.GONE);
        }

        tvTaskPriority.setText(task.getPriority());
        tvTaskStatus.setText(task.getStatus());

        // Định dạng và hiển thị ngày
        String startDate = DateTimeUtils.formatDisplayDate(task.getStartDate());
        String dueDate = DateTimeUtils.formatDisplayDate(task.getDueDate());
        tvTaskStartDate.setText(startDate);
        tvTaskDueDate.setText(dueDate);

        // Hiển thị dự án
        if (project != null) {
            tvTaskProject.setText(project.getName());
        }

        // Hiển thị người được giao
        if (assignee != null) {
            tvTaskAssignee.setText(assignee.getFullName());
        } else {
            tvTaskAssignee.setText(R.string.unassigned);
        }

        // Hiển thị người tạo
        if (createdBy != null) {
            tvTaskCreatedBy.setText(createdBy.getFullName());
        }

        // Hiển thị số ngày còn lại
        int daysRemaining = task.getDaysRemaining();
        if (daysRemaining < 0) {
            // Quá hạn
            tvTaskDaysRemaining.setText(getString(R.string.overdue) + " " + Math.abs(daysRemaining) + " " + getString(R.string.days));
            tvTaskDaysRemaining.setTextColor(getResources().getColor(R.color.colorError));
        } else if (daysRemaining == 0) {
            // Đến hạn hôm nay
            tvTaskDaysRemaining.setText(getString(R.string.due_today));
            tvTaskDaysRemaining.setTextColor(getResources().getColor(R.color.colorWarning));
        } else {
            // Còn thời gian
            tvTaskDaysRemaining.setText(getString(R.string.remaining) + " " + daysRemaining + " " + getString(R.string.days));
            tvTaskDaysRemaining.setTextColor(getResources().getColor(R.color.colorTextSecondary));
        }

        // Hiển thị đường dẫn tài nguyên
        if (task.getResourceLink() != null && !task.getResourceLink().isEmpty()) {
            tvTaskResourceLink.setText(task.getResourceLink());
            tvTaskResourceLink.setVisibility(View.VISIBLE);
        } else {
            tvTaskResourceLink.setVisibility(View.GONE);
        }
    }

    private void updateEmptyView() {
        if (historyList.isEmpty()) {
            tvNoHistory.setVisibility(View.VISIBLE);
            recyclerViewHistory.setVisibility(View.GONE);
        } else {
            tvNoHistory.setVisibility(View.GONE);
            recyclerViewHistory.setVisibility(View.VISIBLE);
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
        // Tải lại dữ liệu khi quay lại fragment
        if (taskId > 0) {
            loadData();
        }
    }
}