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
import com.example.taskmanager.activities.ProjectDetailActivity;
import com.example.taskmanager.adapters.ProjectAdapter;
import com.example.taskmanager.database.DatabaseManager;
import com.example.taskmanager.database.ProjectDAO;
import com.example.taskmanager.models.Project;
import com.example.taskmanager.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class ProjectsFragment extends Fragment implements ProjectAdapter.OnProjectClickListener {

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    private ProjectAdapter projectAdapter;
    private ProjectDAO projectDAO;
    private SessionManager sessionManager;

    private List<Project> projectList;

    private static final int TAB_ALL_PROJECTS = 0;
    private static final int TAB_MY_PROJECTS = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_projects, container, false);

        // Khởi tạo các thành phần giao diện
        initViews(view);

        // Khởi tạo database và session
        projectDAO = new ProjectDAO(getContext());
        sessionManager = new SessionManager(getContext());

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập TabLayout
        setupTabLayout();

        return view;
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        recyclerView = view.findViewById(R.id.recycler_view);
        tvEmpty = view.findViewById(R.id.tv_empty);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupRecyclerView() {
        projectList = new ArrayList<>();
        projectAdapter = new ProjectAdapter(getContext(), projectList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(projectAdapter);
    }

    private void setupTabLayout() {
        // Thêm các tab
        tabLayout.addTab(tabLayout.newTab().setText(R.string.all_projects));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.my_projects));

        // Thiết lập sự kiện cho tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Tải dự án dựa trên tab được chọn
                loadProjects(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Không làm gì
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Tải lại dự án
                loadProjects(tab.getPosition());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải dự án khi fragment được resume
        loadProjects(tabLayout.getSelectedTabPosition());
    }

    private void loadProjects(int tabPosition) {
        showProgress(true);

        new Thread(() -> {
            DatabaseManager dbManager = null;
            SQLiteDatabase database = null;

            try {
                dbManager = DatabaseManager.getInstance(getContext());
                database = dbManager.openDatabase();

                ProjectDAO localProjectDAO = new ProjectDAO(getContext());
                localProjectDAO.setDatabase(database);

                // Lấy danh sách dự án dựa trên tab được chọn
                final List<Project> projects;
                if (tabPosition == TAB_ALL_PROJECTS) {
                    projects = localProjectDAO.getAllProjects();
                } else { // TAB_MY_PROJECTS
                    projects = localProjectDAO.getProjectsByUser(sessionManager.getUserId());
                }

                // Cập nhật UI trên main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showProgress(false);

                        projectList.clear();
                        projectList.addAll(projects);
                        projectAdapter.notifyDataSetChanged();

                        // Hiển thị thông báo nếu không có dự án nào
                        if (projectList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                    });
                }
            } finally {
                if (database != null && dbManager != null) {
                    dbManager.closeDatabase();
                }
            }
        }).start();
    }

    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onProjectClick(Project project) {
        // Mở màn hình chi tiết dự án khi click vào một dự án
        Intent intent = new Intent(getActivity(), ProjectDetailActivity.class);
        intent.putExtra("PROJECT_ID", project.getId());
        startActivity(intent);
    }
}