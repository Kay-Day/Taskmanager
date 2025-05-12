package com.example.taskmanager.activities;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.adapters.SelectUserAdapter;
import com.example.taskmanager.database.DatabaseManager;
import com.example.taskmanager.database.ProjectDAO;
import com.example.taskmanager.database.UserDAO;
import com.example.taskmanager.models.User;

import java.util.ArrayList;
import java.util.List;

public class AddMemberActivity extends AppCompatActivity implements SelectUserAdapter.OnUserClickListener {

    private RecyclerView recyclerViewUsers;
    private Button btnAddMember;
    private Spinner spinnerRole;

    private SelectUserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private List<User> selectedUsers = new ArrayList<>();

    private long projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        // Lấy ID dự án từ intent
        projectId = getIntent().getLongExtra("PROJECT_ID", -1);
        if (projectId == -1) {
            Toast.makeText(this, R.string.error_loading_project, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Thiết lập tiêu đề
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.add_members);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Khởi tạo các thành phần giao diện
        initViews();

        // Thiết lập spinner cho các vai trò
        setupRoleSpinner();

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Tải danh sách người dùng
        loadUsers();

        // Thiết lập sự kiện cho nút thêm thành viên
        btnAddMember.setOnClickListener(v -> addSelectedMembers());
    }

    private void initViews() {
        recyclerViewUsers = findViewById(R.id.recycler_view_users);
        btnAddMember = findViewById(R.id.btn_add_member);
        spinnerRole = findViewById(R.id.spinner_role);
    }

    private void setupRoleSpinner() {
        // Thiết lập adapter cho spinner vai trò
        String[] roles = {"Thành viên", "Quản lý", "Khách"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);
    }

    private void setupRecyclerView() {
        userAdapter = new SelectUserAdapter(this, userList, this);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);
    }

    private void loadUsers() {
        // Tải danh sách người dùng từ database
        new Thread(() -> {
            DatabaseManager dbManager = DatabaseManager.getInstance(this);
            SQLiteDatabase database = null;

            try {
                database = dbManager.openDatabase();

                // Tạo UserDAO và ProjectDAO và thiết lập database cho chúng
                UserDAO userDAO = new UserDAO(this);
                ProjectDAO projectDAO = new ProjectDAO(this);

                userDAO.setDatabase(database);
                projectDAO.setDatabase(database);

                // Lấy danh sách tất cả người dùng
                final List<User> allUsers = userDAO.getAllUsers();

                // Lấy danh sách thành viên hiện tại của dự án
                final List<User> projectMembers = projectDAO.getProjectMembers(projectId);

                // Loại bỏ các thành viên đã có trong dự án
                for (User member : projectMembers) {
                    for (int i = 0; i < allUsers.size(); i++) {
                        if (allUsers.get(i).getId() == member.getId()) {
                            allUsers.remove(i);
                            break;
                        }
                    }
                }

                // Cập nhật UI
                runOnUiThread(() -> {
                    userList.clear();
                    userList.addAll(allUsers);
                    userAdapter.notifyDataSetChanged();

                    if (userList.isEmpty()) {
                        Toast.makeText(this, R.string.no_users_to_add, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (database != null) {
                    dbManager.closeDatabase();
                }
            }
        }).start();
    }

    @Override
    public void onUserClick(User user, boolean isSelected) {
        if (isSelected) {
            if (!selectedUsers.contains(user)) {
                selectedUsers.add(user);
            }
        } else {
            selectedUsers.remove(user);
        }

        // Cập nhật trạng thái của nút thêm thành viên
        btnAddMember.setEnabled(!selectedUsers.isEmpty());
    }

    private void addSelectedMembers() {
        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, R.string.select_users, Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy vai trò từ spinner
        String role = spinnerRole.getSelectedItem().toString();

        // Thêm các thành viên đã chọn vào dự án
        new Thread(() -> {
            DatabaseManager dbManager = DatabaseManager.getInstance(this);
            SQLiteDatabase database = null;

            try {
                database = dbManager.openDatabase();

                ProjectDAO projectDAO = new ProjectDAO(this);
                projectDAO.setDatabase(database);

                boolean success = true;

                // Thêm từng thành viên vào dự án
                for (User user : selectedUsers) {
                    boolean result = projectDAO.addMember(projectId, user.getId(), role);
                    if (!result) {
                        success = false;
                        break;
                    }
                }

                final boolean finalSuccess = success;
                runOnUiThread(() -> {
                    if (finalSuccess) {
                        Toast.makeText(this, R.string.members_added, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, R.string.error_adding_members, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (database != null) {
                    dbManager.closeDatabase();
                }
            }
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}