package com.example.taskmanager.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.activities.TaskDetailActivity;
import com.example.taskmanager.adapters.TaskAdapter;
import com.example.taskmanager.database.ProjectDAO;
import com.example.taskmanager.database.TaskDAO;
import com.example.taskmanager.models.Project;
import com.example.taskmanager.models.Task;
import com.example.taskmanager.utils.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private TextView tvTotalProjects, tvTotalTasks;
    private TextView tvCompletedCount, tvInProgressCount, tvNotStartedCount, tvDelayedCount;
    private ProgressBar progressCompleted, progressInProgress, progressNotStarted, progressDelayed;
    private FrameLayout chartContainer;
    private RecyclerView recyclerViewDeadlines;
    private TextView tvNoDeadlines;

    private BarChart barChart;

    private TaskAdapter taskAdapter;
    private ProjectDAO projectDAO;
    private TaskDAO taskDAO;
    private SessionManager sessionManager;

    private List<Task> upcomingTasks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Khởi tạo các thành phần giao diện
        initViews(view);

        // Khởi tạo database và session
        projectDAO = new ProjectDAO(getContext());
        taskDAO = new TaskDAO(getContext());
        sessionManager = new SessionManager(getContext());

        // Thiết lập RecyclerView cho danh sách nhiệm vụ sắp đến hạn
        setupRecyclerView();

        // Khởi tạo biểu đồ
        setupChart();

        return view;
    }

    private void initViews(View view) {
        tvTotalProjects = view.findViewById(R.id.tv_total_projects);
        tvTotalTasks = view.findViewById(R.id.tv_total_tasks);

        tvCompletedCount = view.findViewById(R.id.tv_completed_count);
        tvInProgressCount = view.findViewById(R.id.tv_in_progress_count);
        tvNotStartedCount = view.findViewById(R.id.tv_not_started_count);
        tvDelayedCount = view.findViewById(R.id.tv_delayed_count);

        progressCompleted = view.findViewById(R.id.progress_completed);
        progressInProgress = view.findViewById(R.id.progress_in_progress);
        progressNotStarted = view.findViewById(R.id.progress_not_started);
        progressDelayed = view.findViewById(R.id.progress_delayed);

        chartContainer = view.findViewById(R.id.chart_container);
        recyclerViewDeadlines = view.findViewById(R.id.recycler_view_deadlines);
        tvNoDeadlines = view.findViewById(R.id.tv_no_deadlines);
    }

    private void setupRecyclerView() {
        upcomingTasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(getContext(), upcomingTasks, this);
        recyclerViewDeadlines.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDeadlines.setAdapter(taskAdapter);
    }

    private void setupChart() {
        // Tạo biểu đồ cột
        barChart = new BarChart(getContext());
        chartContainer.addView(barChart);

        // Cấu hình biểu đồ
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);
        barChart.setMaxVisibleValueCount(7);
        barChart.setNoDataText(getString(R.string.loading));

        // Cấu hình trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);

        // Cấu hình trục Y bên trái
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setSpaceTop(35f);
        leftAxis.setAxisMinimum(0f);

        // Cấu hình trục Y bên phải (tắt)
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Tắt chú thích
        barChart.getLegend().setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải dữ liệu thống kê khi fragment được resume
        loadStatistics();
    }

    private void loadStatistics() {
        new Thread(() -> {
            // Mở kết nối đến database
            projectDAO.open();
            taskDAO.open();

            // Lấy danh sách dự án và nhiệm vụ
            List<Project> projects = projectDAO.getProjectsByUser(sessionManager.getUserId());
            List<Task> tasks = taskDAO.getTasksByUser(sessionManager.getUserId());

            // Lấy danh sách nhiệm vụ sắp đến hạn
            List<Task> upcomingDeadlines = taskDAO.getUpcomingTasks();

            // Đếm số lượng nhiệm vụ theo trạng thái
            int completedCount = 0;
            int inProgressCount = 0;
            int notStartedCount = 0;
            int delayedCount = 0;

            for (Task task : tasks) {
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
                    case "Tạm hoãn":
                        delayedCount++;
                        break;
                }
            }

            // Chuẩn bị dữ liệu cho biểu đồ tiến độ theo tuần
            List<BarEntry> entries = prepareWeeklyChartData(tasks);
            List<String> labels = getWeekDayLabels();

            // Đóng kết nối database
            taskDAO.close();
            projectDAO.close();

            // Cập nhật UI trên main thread
            if (getActivity() != null) {
                int finalCompletedCount = completedCount;
                int finalInProgressCount = inProgressCount;
                int finalNotStartedCount = notStartedCount;
                int finalDelayedCount = delayedCount;
                getActivity().runOnUiThread(() -> {
                    // Cập nhật số lượng dự án và nhiệm vụ
                    tvTotalProjects.setText(String.valueOf(projects.size()));
                    tvTotalTasks.setText(String.valueOf(tasks.size()));

                    // Tính phần trăm cho các trạng thái nhiệm vụ
                    int totalTasks = tasks.size();
                    if (totalTasks > 0) {
                        int completedPercent = (finalCompletedCount * 100) / totalTasks;
                        int inProgressPercent = (finalInProgressCount * 100) / totalTasks;
                        int notStartedPercent = (finalNotStartedCount * 100) / totalTasks;
                        int delayedPercent = (finalDelayedCount * 100) / totalTasks;

                        // Cập nhật các progress bar và text
                        progressCompleted.setProgress(completedPercent);
                        progressInProgress.setProgress(inProgressPercent);
                        progressNotStarted.setProgress(notStartedPercent);
                        progressDelayed.setProgress(delayedPercent);

                        tvCompletedCount.setText(completedPercent + "%");
                        tvInProgressCount.setText(inProgressPercent + "%");
                        tvNotStartedCount.setText(notStartedPercent + "%");
                        tvDelayedCount.setText(delayedPercent + "%");
                    }

                    // Cập nhật biểu đồ
                    updateChart(entries, labels);

                    // Cập nhật danh sách nhiệm vụ sắp đến hạn
                    upcomingTasks.clear();

                    // Sắp xếp nhiệm vụ theo thời gian đến hạn
                    Collections.sort(upcomingDeadlines, new Comparator<Task>() {
                        @Override
                        public int compare(Task t1, Task t2) {
                            return t1.getDueDate().compareTo(t2.getDueDate());
                        }
                    });

                    // Giới hạn hiển thị tối đa 5 nhiệm vụ
                    int maxTasks = Math.min(upcomingDeadlines.size(), 5);
                    for (int i = 0; i < maxTasks; i++) {
                        upcomingTasks.add(upcomingDeadlines.get(i));
                    }

                    taskAdapter.notifyDataSetChanged();

                    // Hiển thị thông báo nếu không có nhiệm vụ nào sắp đến hạn
                    if (upcomingTasks.isEmpty()) {
                        tvNoDeadlines.setVisibility(View.VISIBLE);
                        recyclerViewDeadlines.setVisibility(View.GONE);
                    } else {
                        tvNoDeadlines.setVisibility(View.GONE);
                        recyclerViewDeadlines.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
    }

    private List<BarEntry> prepareWeeklyChartData(List<Task> tasks) {
        List<BarEntry> entries = new ArrayList<>();

        // Lấy ngày đầu tiên của tuần (Chủ nhật)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        // Tạo mảng đếm số nhiệm vụ hoàn thành theo từng ngày trong tuần
        int[] completedTasksCount = new int[7];

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (Task task : tasks) {
            if (task.getStatus().equals("Hoàn thành")) {
                try {
                    // Parse the string date into a Date object
                    Date createdDate = sdf.parse(task.getCreatedAt());

                    // Set the parsed date
                    Calendar taskDate = Calendar.getInstance();
                    taskDate.setTime(createdDate);

                    int dayOfWeek = taskDate.get(Calendar.DAY_OF_WEEK) - 1; // Chuyển về index 0-6
                    if (dayOfWeek >= 0 && dayOfWeek < 7) {
                        completedTasksCount[dayOfWeek]++;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        // ... existing code ...
        // Tạo các entry cho biểu đồ
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, completedTasksCount[i]));
        }

        return entries;
    }

    private List<String> getWeekDayLabels() {
        List<String> labels = new ArrayList<>();
        labels.add("CN");
        labels.add("T2");
        labels.add("T3");
        labels.add("T4");
        labels.add("T5");
        labels.add("T6");
        labels.add("T7");
        return labels;
    }

    private void updateChart(List<BarEntry> entries, List<String> labels) {
        // Tạo dataset
        BarDataSet dataSet = new BarDataSet(entries, "Nhiệm vụ hoàn thành");
        dataSet.setColor(getResources().getColor(R.color.colorPrimary));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        // Tạo dữ liệu cho biểu đồ
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        // Thiết lập labels cho trục X
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        // Cập nhật biểu đồ
        barChart.setData(data);
        barChart.invalidate();
    }

    @Override
    public void onTaskClick(Task task) {
        // Mở màn hình chi tiết nhiệm vụ khi click vào một nhiệm vụ
        Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
        intent.putExtra("TASK_ID", task.getId());
        startActivity(intent);
    }
}