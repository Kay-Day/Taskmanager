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
import com.example.taskmanager.fragments.ProjectDetailsFragment;
import com.example.taskmanager.fragments.ProjectTasksFragment;
import com.example.taskmanager.models.Project;
import com.example.taskmanager.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class ProjectDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Button btnAddTask, btnEditProject;
    private ProgressBar progressBar;
    private TextView tvEmptyProject;

    private SessionManager sessionManager;

    private Project project;
    private long projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        // Khởi tạo các thành phần giao diện
        initViews();

        // Thiết lập toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Khởi tạo session
        sessionManager = new SessionManager(this);

        // Kiểm tra có ID dự án được truyền vào không
        if (getIntent().hasExtra("PROJECT_ID")) {
            projectId = getIntent().getLongExtra("PROJECT_ID", -1);
            loadProject(projectId);
        } else {
            // Không có ID dự án, kết thúc activity
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
        btnAddTask = findViewById(R.id.btn_add_task);
        btnEditProject = findViewById(R.id.btn_edit_project);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyProject = findViewById(R.id.tv_empty_project);
    }

    private void setupClickListeners() {
        // Sự kiện khi nhấn nút thêm nhiệm vụ
        btnAddTask.setOnClickListener(v -> {
            if (project != null) {
                Intent intent = new Intent(ProjectDetailActivity.this, TaskActivity.class);
                intent.putExtra("PROJECT_ID", project.getId());
                startActivity(intent);
            }
        });

        // Sự kiện khi nhấn nút chỉnh sửa dự án
        btnEditProject.setOnClickListener(v -> {
            if (project != null) {
                Intent intent = new Intent(ProjectDetailActivity.this, ProjectActivity.class);
                intent.putExtra("PROJECT_ID", project.getId());
                startActivity(intent);
            }
        });
    }

    private void loadProject(long projectId) {
        showProgress(true);

        new Thread(() -> {
            DatabaseManager dbManager = null;
            SQLiteDatabase database = null;

            try {
                dbManager = DatabaseManager.getInstance(this);
                database = dbManager.openDatabase();

                // Tạo ProjectDAO và thiết lập database cho nó
                ProjectDAO projectDAO = new ProjectDAO(this);
                projectDAO.setDatabase(database);

                // Lấy thông tin dự án
                final Project loadedProject = projectDAO.getProjectById(projectId);

                // Lưu kết quả vào biến thành viên
                project = loadedProject;

                // Chuyển về UI thread để cập nhật giao diện
                runOnUiThread(() -> {
                    showProgress(false);

                    if (project != null) {
                        // Hiển thị thông tin dự án
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(project.getName());
                        }

                        // Kiểm tra quyền chỉnh sửa dự án (chỉ người tạo mới có quyền)
                        boolean canEdit = project.getCreatedBy() == sessionManager.getUserId();
                        btnEditProject.setVisibility(canEdit ? View.VISIBLE : View.GONE);

                        // Thiết lập ViewPager và TabLayout
                        setupViewPager(viewPager);
                        tabLayout.setupWithViewPager(viewPager);

                        // Ẩn thông báo trống
                        tvEmptyProject.setVisibility(View.GONE);
                    } else {
                        // Hiển thị thông báo trống
                        tvEmptyProject.setVisibility(View.VISIBLE);
                        btnAddTask.setVisibility(View.GONE);
                        btnEditProject.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Cập nhật UI khi có lỗi
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(ProjectDetailActivity.this, R.string.error_loading_project, Toast.LENGTH_SHORT).show();
                    // Kết thúc activity nếu bị lỗi khi tải dự án
                    finish();
                });
            } finally {
                // Đóng kết nối database
                if (database != null && dbManager != null) {
                    dbManager.closeDatabase();
                }
            }
        }).start();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Thêm fragment thông tin chi tiết dự án
        ProjectDetailsFragment detailsFragment = new ProjectDetailsFragment();
        Bundle detailsBundle = new Bundle();
        detailsBundle.putLong("PROJECT_ID", project.getId());
        detailsFragment.setArguments(detailsBundle);
        adapter.addFragment(detailsFragment, getString(R.string.details));

        // Thêm fragment danh sách nhiệm vụ
        ProjectTasksFragment tasksFragment = new ProjectTasksFragment();
        Bundle tasksBundle = new Bundle();
        tasksBundle.putLong("PROJECT_ID", project.getId());
        tasksFragment.setArguments(tasksBundle);
        adapter.addFragment(tasksFragment, getString(R.string.tasks));

        viewPager.setAdapter(adapter);
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
        // Tải lại dự án khi quay lại màn hình
        if (projectId > 0) {
            loadProject(projectId);
        }
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