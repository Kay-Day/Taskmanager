package com.example.taskmanager.fragments;

import android.content.Intent;
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
import com.example.taskmanager.activities.TaskDetailActivity;
import com.example.taskmanager.adapters.TaskAdapter;
import com.example.taskmanager.database.DatabaseManager;
import com.example.taskmanager.database.TaskDAO;
import com.example.taskmanager.models.Task;
import com.example.taskmanager.utils.DatabaseUtils;
import com.example.taskmanager.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProjectTasksFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerViewTasks;
    private TextView tvNoTasks;
    private ProgressBar progressBar;

    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private SessionManager sessionManager;

    private long projectId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_tasks, container, false);

        // Khởi tạo các thành phần giao diện
        initViews(view);

        // Khởi tạo session manager
        sessionManager = new SessionManager(getContext());

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
        recyclerViewTasks = view.findViewById(R.id.recycler_view_tasks);
        tvNoTasks = view.findViewById(R.id.tv_no_tasks);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupRecyclerView() {
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(getContext(), taskList, this);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewTasks.setAdapter(taskAdapter);
    }

//    private void loadData() {
//        if (!isAdded() || getContext() == null) {
//            return;
//        }
//
//        showProgress(true);
//
//        new Thread(() -> {
//            DatabaseManager dbManager = null;
//            SQLiteDatabase database = null;
//
//            try {
//                dbManager = DatabaseManager.getInstance(getContext());
//                database = dbManager.openDatabase();
//
//                // Sử dụng TaskDAO và thiết lập database cho nó
//                TaskDAO taskDAO = new TaskDAO(getContext());
//                taskDAO.setDatabase(database);
//
//                // Lấy danh sách nhiệm vụ
//                final List<Task> tasks = taskDAO.getTasksByProject(projectId);
//
//                // Sắp xếp nhiệm vụ (ví dụ: theo trạng thái, rồi đến hạn)
//                Collections.sort(tasks, new Comparator<Task>() {
//                    @Override
//                    public int compare(Task task1, Task task2) {
//                        // Sắp xếp theo trạng thái (ưu tiên trạng thái "Trễ hạn" và "Đang thực hiện")
//                        int statusOrder1 = getStatusOrder(task1.getStatus());
//                        int statusOrder2 = getStatusOrder(task2.getStatus());
//
//                        if (statusOrder1 != statusOrder2) {
//                            return statusOrder1 - statusOrder2;
//                        }
//
//                        // Sau đó sắp xếp theo ngày đến hạn (tăng dần)
//                        return task1.getDueDate().compareTo(task2.getDueDate());
//                    }
//
//                    private int getStatusOrder(String status) {
//                        switch (status) {
//                            case "Trễ hạn": return 0;
//                            case "Đang thực hiện": return 1;
//                            case "Chưa bắt đầu": return 2;
//                            case "Hoàn thành": return 3;
//                            default: return 4;
//                        }
//                    }
//                });
//
//                // Cập nhật UI
//                if (getActivity() != null && isAdded()) {
//                    getActivity().runOnUiThread(() -> {
//                        if (isAdded()) {
//                            showProgress(false);
//
//                            taskList.clear();
//                            taskList.addAll(tasks);
//                            taskAdapter.notifyDataSetChanged();
//
//                            // Hiển thị thông báo không có nhiệm vụ nếu danh sách trống
//                            updateEmptyView();
//                        }
//                    });
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                if (getActivity() != null && isAdded()) {
//                    getActivity().runOnUiThread(() -> {
//                        if (isAdded()) {
//                            showProgress(false);
//                            Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//            } finally {
//                // Đóng kết nối database
//                if (database != null && dbManager != null) {
//                    dbManager.closeDatabase();
//                }
//            }
//        }).start();
//    }
private void loadData() {
    if (!isAdded() || getContext() == null) {
        return;
    }

    showProgress(true);

    new Thread(() -> {
        try {
            // Sử dụng DatabaseUtils để thực hiện thao tác cơ sở dữ liệu an toàn
            final List<Task> tasks = DatabaseUtils.executeWithTransaction(getContext(),
                    (database, projectDAO, taskDAO, userDAO, historyDAO, commentDAO, eventDAO, notificationDAO) -> {
                        // Lấy danh sách nhiệm vụ
                        List<Task> taskList = taskDAO.getTasksByProject(projectId);

                        // Sắp xếp nhiệm vụ
                        Collections.sort(taskList, new Comparator<Task>() {
                            @Override
                            public int compare(Task task1, Task task2) {
                                // Sắp xếp theo trạng thái
                                int statusOrder1 = getStatusOrder(task1.getStatus());
                                int statusOrder2 = getStatusOrder(task2.getStatus());

                                if (statusOrder1 != statusOrder2) {
                                    return statusOrder1 - statusOrder2;
                                }

                                // Sắp xếp theo ngày đến hạn
                                return task1.getDueDate().compareTo(task2.getDueDate());
                            }

                            private int getStatusOrder(String status) {
                                switch (status) {
                                    case "Trễ hạn": return 0;
                                    case "Đang thực hiện": return 1;
                                    case "Chưa bắt đầu": return 2;
                                    case "Hoàn thành": return 3;
                                    default: return 4;
                                }
                            }
                        });

                        return taskList;
                    }
            );

            // Cập nhật UI trên main thread
            if (getActivity() != null && isAdded()) {
                getActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        showProgress(false);

                        taskList.clear();
                        taskList.addAll(tasks);
                        taskAdapter.notifyDataSetChanged();

                        // Hiển thị thông báo không có nhiệm vụ nếu danh sách trống
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
        }
    }).start();
}

    private void updateEmptyView() {
        if (taskList.isEmpty()) {
            tvNoTasks.setVisibility(View.VISIBLE);
            recyclerViewTasks.setVisibility(View.GONE);
        } else {
            tvNoTasks.setVisibility(View.GONE);
            recyclerViewTasks.setVisibility(View.VISIBLE);
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

    @Override
    public void onTaskClick(Task task) {
        // Mở màn hình chi tiết nhiệm vụ khi click vào một nhiệm vụ
        Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
        intent.putExtra("TASK_ID", task.getId());
        startActivity(intent);
    }

    /**
     * Cập nhật trạng thái của nhiệm vụ
     */
    public void updateTaskStatus(long taskId, String newStatus) {
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

                TaskDAO taskDAO = new TaskDAO(getContext());
                taskDAO.setDatabase(database);

                // Lấy task hiện tại
                Task task = taskDAO.getTaskById(taskId);
                if (task != null) {
                    // Lưu trạng thái cũ
                    Task oldTask = new Task(); // Tạo object mới
                    oldTask.setId(task.getId());
                    oldTask.setProjectId(task.getProjectId());
                    oldTask.setTitle(task.getTitle());
                    oldTask.setDescription(task.getDescription());
                    oldTask.setAssignedTo(task.getAssignedTo());
                    oldTask.setCreatedBy(task.getCreatedBy());
                    oldTask.setPriority(task.getPriority());
                    oldTask.setStatus(task.getStatus()); // Lưu trạng thái cũ
                    oldTask.setResourceLink(task.getResourceLink());
                    oldTask.setStartDate(task.getStartDate());
                    oldTask.setDueDate(task.getDueDate());
                    oldTask.setCreatedAt(task.getCreatedAt());

                    // Cập nhật trạng thái mới
                    task.setStatus(newStatus);
                    int updateCount = taskDAO.updateTask(task, oldTask, sessionManager.getUserId());

                    final boolean success = updateCount > 0;

                    // Cập nhật UI
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            showProgress(false);
                            if (success) {
                                // Tải lại dữ liệu để cập nhật danh sách
                                loadData();
                                Toast.makeText(getContext(), R.string.task_updated, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), R.string.error_updating_task, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            showProgress(false);
                            Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
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
}