package com.example.taskmanager.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.taskmanager.R;
import com.example.taskmanager.database.DatabaseManager;
import com.example.taskmanager.database.ProjectDAO;
import com.example.taskmanager.database.TaskDAO;
import com.example.taskmanager.database.UserDAO;
import com.example.taskmanager.fragments.TaskCommentsFragment;
import com.example.taskmanager.fragments.TaskDetailsFragment;
import com.example.taskmanager.models.Project;
import com.example.taskmanager.models.Task;
import com.example.taskmanager.models.User;
import com.example.taskmanager.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class TaskDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Button btnEditTask, btnCompleteTask;
    private ProgressBar progressBar;
    private TextView tvEmptyTask;

    private SessionManager sessionManager;

    private Task task;
    private Project project;
    private User assignedTo;
    private User createdBy;
    private long taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Khởi tạo các thành phần giao diện
        initViews();

        // Thiết lập toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Khởi tạo session
        sessionManager = new SessionManager(this);

        // Kiểm tra có ID nhiệm vụ được truyền vào không
        if (getIntent().hasExtra("TASK_ID")) {
            taskId = getIntent().getLongExtra("TASK_ID", -1);
            loadTask(taskId);
        } else {
            // Không có ID nhiệm vụ, kết thúc activity
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Thiết lập sự kiện click
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        btnEditTask = findViewById(R.id.btn_edit_task);
        btnCompleteTask = findViewById(R.id.btn_complete_task);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyTask = findViewById(R.id.tv_empty_task);
    }

    private void setupClickListeners() {
        // Sự kiện khi nhấn nút chỉnh sửa nhiệm vụ
        btnEditTask.setOnClickListener(v -> {
            if (task != null) {
                Intent intent = new Intent(TaskDetailActivity.this, TaskActivity.class);
                intent.putExtra("TASK_ID", task.getId());
                startActivity(intent);
            }
        });

        // Sự kiện khi nhấn nút hoàn thành nhiệm vụ
        btnCompleteTask.setOnClickListener(v -> {
            if (task != null && !task.getStatus().equals("Hoàn thành")) {
                showCompleteTaskConfirmation();
            }
        });
    }

    private void loadTask(long taskId) {
        showProgress(true);

        new Thread(() -> {
            DatabaseManager dbManager = null;
            SQLiteDatabase database = null;

            try {
                dbManager = DatabaseManager.getInstance(this);
                database = dbManager.openDatabase();

                // Tạo các DAO và thiết lập database cho chúng
                TaskDAO taskDAO = new TaskDAO(this);
                ProjectDAO projectDAO = new ProjectDAO(this);
                UserDAO userDAO = new UserDAO(this);

                taskDAO.setDatabase(database);
                projectDAO.setDatabase(database);
                userDAO.setDatabase(database);

                // Lấy thông tin nhiệm vụ
                Task loadedTask = taskDAO.getTaskById(taskId);
                Project loadedProject = null;
                User loadedAssignedTo = null;
                User loadedCreatedBy = null;

                if (loadedTask != null) {
                    // Lấy thông tin dự án
                    loadedProject = projectDAO.getProjectById(loadedTask.getProjectId());

                    // Lấy thông tin người được giao và người tạo
                    if (loadedTask.getAssignedTo() > 0) {
                        loadedAssignedTo = userDAO.getUserById(loadedTask.getAssignedTo());
                    }
                    if (loadedTask.getCreatedBy() > 0) {
                        loadedCreatedBy = userDAO.getUserById(loadedTask.getCreatedBy());
                    }
                }

                // Lưu trữ biến tạm thời để truy cập trong UI thread
                final Task finalTask = loadedTask;
                final Project finalProject = loadedProject;
                final User finalAssignedTo = loadedAssignedTo;
                final User finalCreatedBy = loadedCreatedBy;

                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        showProgress(false);

                        // Lưu vào biến thành viên
                        task = finalTask;
                        project = finalProject;
                        assignedTo = finalAssignedTo;
                        createdBy = finalCreatedBy;

                        if (task != null) {
                            // Hiển thị thông tin nhiệm vụ
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setTitle(task.getTitle());
                            }

                            // Cập nhật trạng thái nút hoàn thành
                            updateCompleteButton();

                            // Kiểm tra quyền chỉnh sửa nhiệm vụ (người được giao hoặc người tạo)
                            boolean canEdit = (task.getAssignedTo() > 0 && task.getAssignedTo() == sessionManager.getUserId())
                                    || task.getCreatedBy() == sessionManager.getUserId();
                            btnEditTask.setVisibility(canEdit ? View.VISIBLE : View.GONE);

                            // Thiết lập ViewPager và TabLayout
                            setupViewPager(viewPager);
                            tabLayout.setupWithViewPager(viewPager);

                            // Ẩn thông báo trống
                            tvEmptyTask.setVisibility(View.GONE);
                        } else {
                            // Hiển thị thông báo trống
                            tvEmptyTask.setVisibility(View.VISIBLE);
                            btnEditTask.setVisibility(View.GONE);
                            btnCompleteTask.setVisibility(View.GONE);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        showProgress(false);
                        Toast.makeText(TaskDetailActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                });
            } finally {
                if (database != null && dbManager != null) {
                    dbManager.closeDatabase();
                }
            }
        }).start();
    }

    private void updateCompleteButton() {
        if (task.getStatus().equals("Hoàn thành")) {
            btnCompleteTask.setText(R.string.task_completed);
            btnCompleteTask.setEnabled(false);
        } else {
            btnCompleteTask.setText(R.string.complete_task);
            btnCompleteTask.setEnabled(true);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Thêm fragment thông tin chi tiết nhiệm vụ
        TaskDetailsFragment detailsFragment = new TaskDetailsFragment();
        Bundle detailsBundle = new Bundle();
        detailsBundle.putLong("TASK_ID", task.getId());
        detailsFragment.setArguments(detailsBundle);
        adapter.addFragment(detailsFragment, getString(R.string.details));

        // Thêm fragment bình luận
        TaskCommentsFragment commentsFragment = new TaskCommentsFragment();
        Bundle commentsBundle = new Bundle();
        commentsBundle.putLong("TASK_ID", task.getId());
        commentsFragment.setArguments(commentsBundle);
        adapter.addFragment(commentsFragment, getString(R.string.comments));

        viewPager.setAdapter(adapter);
    }

    private void showCompleteTaskConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.complete_task)
                .setMessage(R.string.confirm_complete_task)
                .setPositiveButton(R.string.yes, (dialog, which) -> completeTask())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void completeTask() {
        showProgress(true);

        new Thread(() -> {
            DatabaseManager dbManager = null;
            SQLiteDatabase database = null;

            try {
                dbManager = DatabaseManager.getInstance(this);
                database = dbManager.openDatabase();

                TaskDAO taskDAO = new TaskDAO(this);
                taskDAO.setDatabase(database);

                // Cập nhật trạng thái nhiệm vụ thành hoàn thành
                String oldStatus = task.getStatus();
                task.setStatus("Hoàn thành");

                // Tạo bản sao của nhiệm vụ cũ để ghi lịch sử thay đổi
                Task oldTask = new Task();
                oldTask.setId(task.getId());
                oldTask.setProjectId(task.getProjectId());
                oldTask.setTitle(task.getTitle());
                oldTask.setDescription(task.getDescription());
                oldTask.setAssignedTo(task.getAssignedTo());
                oldTask.setCreatedBy(task.getCreatedBy());
                oldTask.setPriority(task.getPriority());
                oldTask.setStatus(oldStatus); // Lưu trạng thái cũ
                oldTask.setResourceLink(task.getResourceLink());
                oldTask.setStartDate(task.getStartDate());
                oldTask.setDueDate(task.getDueDate());
                oldTask.setCreatedAt(task.getCreatedAt());

                int result = taskDAO.updateTask(task, oldTask, sessionManager.getUserId());

                final boolean success = result > 0;

                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        showProgress(false);

                        if (success) {
                            // Cập nhật UI
                            updateCompleteButton();
                            Toast.makeText(TaskDetailActivity.this, R.string.task_completed_message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TaskDetailActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        showProgress(false);
                        Toast.makeText(TaskDetailActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                });
            } finally {
                if (database != null && dbManager != null) {
                    dbManager.closeDatabase();
                }
            }
        }).start();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại nhiệm vụ khi quay lại màn hình
        if (taskId > 0) {
            loadTask(taskId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Reset DatabaseManager để đảm bảo tất cả các kết nối được đóng đúng cách
        DatabaseManager.getInstance(this).reset();
    }

    // Adapter cho ViewPager
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }
}