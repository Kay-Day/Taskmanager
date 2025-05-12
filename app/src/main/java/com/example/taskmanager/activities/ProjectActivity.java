//package com.example.taskmanager.activities;
//
//import android.app.DatePickerDialog;
//import android.database.sqlite.SQLiteDatabase;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ProgressBar;
//import android.widget.RadioButton;
//import android.widget.RadioGroup;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.taskmanager.R;
//import com.example.taskmanager.adapters.MemberAdapter;
//import com.example.taskmanager.database.DatabaseManager;
//import com.example.taskmanager.database.ProjectDAO;
//import com.example.taskmanager.database.UserDAO;
//import com.example.taskmanager.models.Project;
//import com.example.taskmanager.models.User;
//import com.example.taskmanager.utils.DateTimeUtils;
//import com.example.taskmanager.utils.SessionManager;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//public class ProjectActivity extends AppCompatActivity implements MemberAdapter.OnMemberActionListener {
//
//    private Toolbar toolbar;
//    private TextInputLayout tilProjectName, tilProjectDescription, tilStartDate, tilEndDate, tilSearchMember;
//    private TextInputEditText etProjectName, etProjectDescription, etStartDate, etEndDate, etSearchMember;
//    private RadioGroup radioGroupPriority;
//    private RadioButton radioPriorityHigh, radioPriorityMedium, radioPriorityLow;
//    private Button btnAddMember, btnCreateProject;
//    private RecyclerView recyclerViewMembers;
//    private TextView tvNoMembers;
//    private ProgressBar progressBar;
//
//    private ProjectDAO projectDAO;
//    private UserDAO userDAO;
//    private SessionManager sessionManager;
//
//    private MemberAdapter memberAdapter;
//    private List<User> memberList;
//
//    private Project existingProject; // Null nếu đang tạo dự án mới
//    private boolean isEditing = false;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_project);
//
//        // Khởi tạo các thành phần giao diện
//        initViews();
//
//        // Thiết lập toolbar
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//
//        // Khởi tạo database và session
//        projectDAO = new ProjectDAO(this);
//        userDAO = new UserDAO(this);
//        sessionManager = new SessionManager(this);
//
//        // Thiết lập RecyclerView cho danh sách thành viên
//        setupMembersRecyclerView();
//
//        // Kiểm tra xem đang tạo mới hay chỉnh sửa dự án
//        if (getIntent().hasExtra("PROJECT_ID")) {
//            // Chỉnh sửa dự án hiện có
//            isEditing = true;
//            long projectId = getIntent().getLongExtra("PROJECT_ID", -1);
//            loadExistingProject(projectId);
//            getSupportActionBar().setTitle(R.string.edit_project);
//            btnCreateProject.setText(R.string.save);
//        } else {
//            // Tạo dự án mới
//            getSupportActionBar().setTitle(R.string.new_project);
//
//            // Thêm người dùng hiện tại vào danh sách thành viên
//            addCurrentUserAsMember();
//        }
//
//        // Thiết lập sự kiện cho các view
//        setupViewListeners();
//    }
//
//    private void initViews() {
//        toolbar = findViewById(R.id.toolbar);
//        tilProjectName = findViewById(R.id.til_project_name);
//        tilProjectDescription = findViewById(R.id.til_project_description);
//        tilStartDate = findViewById(R.id.til_start_date);
//        tilEndDate = findViewById(R.id.til_end_date);
//        tilSearchMember = findViewById(R.id.til_search_member);
//
//        etProjectName = findViewById(R.id.et_project_name);
//        etProjectDescription = findViewById(R.id.et_project_description);
//        etStartDate = findViewById(R.id.et_start_date);
//        etEndDate = findViewById(R.id.et_end_date);
//        etSearchMember = findViewById(R.id.et_search_member);
//
//        radioGroupPriority = findViewById(R.id.radio_group_priority);
//        radioPriorityHigh = findViewById(R.id.radio_priority_high);
//        radioPriorityMedium = findViewById(R.id.radio_priority_medium);
//        radioPriorityLow = findViewById(R.id.radio_priority_low);
//
//        btnAddMember = findViewById(R.id.btn_add_member);
//        btnCreateProject = findViewById(R.id.btn_create_project);
//
//        recyclerViewMembers = findViewById(R.id.recycler_view_members);
//        tvNoMembers = findViewById(R.id.tv_no_members);
//
//        progressBar = findViewById(R.id.progress_bar);
//    }
//
//    private void setupMembersRecyclerView() {
//        memberList = new ArrayList<>();
//        memberAdapter = new MemberAdapter(this, memberList, this);
//        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(this));
//        recyclerViewMembers.setAdapter(memberAdapter);
//    }
//
//    private void setupViewListeners() {
//        // Sự kiện cho ngày bắt đầu
//        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
//
//        // Sự kiện cho ngày kết thúc
//        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));
//
//        // Sự kiện cho nút thêm thành viên
//        btnAddMember.setOnClickListener(v -> searchAndAddMember());
//
//        // Sự kiện cho nút tạo dự án
//        btnCreateProject.setOnClickListener(v -> saveProject());
//
//        // Sự kiện cho ô tìm kiếm thành viên
//        etSearchMember.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                // Bật nút thêm thành viên nếu có nội dung tìm kiếm
//                btnAddMember.setEnabled(s.length() > 0);
//            }
//        });
//    }
//
//    private void showDatePickerDialog(final TextInputEditText editText) {
//        // Tạo đối tượng Calendar với ngày hiện tại
//        final Calendar calendar = Calendar.getInstance();
//
//        // Nếu đã có ngày trong EditText, sử dụng ngày đó
//        String currentDate = editText.getText().toString();
//        if (!currentDate.isEmpty()) {
//            try {
//                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//                Date date = displayFormat.parse(currentDate);
//                if (date != null) {
//                    calendar.setTime(date);
//                }
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        }
//
//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH);
//        int day = calendar.get(Calendar.DAY_OF_MONTH);
//
//        // Tạo DatePickerDialog
//        DatePickerDialog datePickerDialog = new DatePickerDialog(
//                this,
//                (view, selectedYear, selectedMonth, selectedDay) -> {
//                    // Cập nhật ngày đã chọn vào EditText
//                    calendar.set(selectedYear, selectedMonth, selectedDay);
//                    String formattedDate = DateTimeUtils.formatDisplayDate(DateTimeUtils.formatDate(calendar.getTime()));
//                    editText.setText(formattedDate);
//                },
//                year, month, day);
//
//        // Hiển thị hộp thoại
//        datePickerDialog.show();
//    }
//
//    private void addCurrentUserAsMember() {
//        User currentUser = sessionManager.getUserDetails();
//        if (currentUser != null && !isMemberAlreadyAdded(currentUser.getId())) {
//            memberList.add(currentUser);
//            memberAdapter.notifyDataSetChanged();
//            updateMembersVisibility();
//        }
//    }
//
//    private void searchAndAddMember() {
//        final String username = etSearchMember.getText().toString().trim();
//        if (username.isEmpty()) {
//            tilSearchMember.setError(getString(R.string.fill_all_fields));
//            return;
//        }
//
//        // Xóa lỗi nếu có
//        tilSearchMember.setError(null);
//
//        // Hiển thị progress
//        showProgress(true);
//
//        // Tìm kiếm người dùng trong database
//        new Thread(() -> {
//            DatabaseManager dbManager = null;
//            SQLiteDatabase database = null;
//
//            try {
//                dbManager = DatabaseManager.getInstance(this);
//                database = dbManager.openDatabase();
//
//                UserDAO localUserDAO = new UserDAO(this);
//                localUserDAO.setDatabase(database);
//
//                final User user = localUserDAO.getUserByUsername(username);
//
//                // Cập nhật UI trên main thread
//                runOnUiThread(() -> {
//                    showProgress(false);
//
//                    if (user != null) {
//                        // Kiểm tra xem người dùng đã có trong danh sách chưa
//                        if (isMemberAlreadyAdded(user.getId())) {
//                            Toast.makeText(ProjectActivity.this, getString(R.string.member_already_added), Toast.LENGTH_SHORT).show();
//                        } else {
//                            // Thêm người dùng vào danh sách thành viên
//                            memberList.add(user);
//                            memberAdapter.notifyDataSetChanged();
//                            updateMembersVisibility();
//
//                            // Xóa nội dung tìm kiếm
//                            etSearchMember.setText("");
//                        }
//                    } else {
//                        // Không tìm thấy người dùng
//                        tilSearchMember.setError(getString(R.string.user_not_found));
//                    }
//                });
//            } catch (Exception e) {
//                e.printStackTrace();
//                runOnUiThread(() -> {
//                    showProgress(false);
//                    Toast.makeText(ProjectActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
//                });
//            } finally {
//                if (database != null && dbManager != null) {
//                    dbManager.closeDatabase();
//                }
//            }
//        }).start();
//    }
//
//    private boolean isMemberAlreadyAdded(long userId) {
//        for (User member : memberList) {
//            if (member.getId() == userId) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private void updateMembersVisibility() {
//        if (memberList.isEmpty()) {
//            tvNoMembers.setVisibility(View.VISIBLE);
//            recyclerViewMembers.setVisibility(View.GONE);
//        } else {
//            tvNoMembers.setVisibility(View.GONE);
//            recyclerViewMembers.setVisibility(View.VISIBLE);
//        }
//    }
//
//    private void loadExistingProject(long projectId) {
//        showProgress(true);
//
//        new Thread(() -> {
//            DatabaseManager dbManager = null;
//            SQLiteDatabase database = null;
//
//            try {
//                dbManager = DatabaseManager.getInstance(this);
//                database = dbManager.openDatabase();
//
//                ProjectDAO localProjectDAO = new ProjectDAO(this);
//                localProjectDAO.setDatabase(database);
//
//                existingProject = localProjectDAO.getProjectById(projectId);
//
//                if (existingProject != null) {
//                    // Lấy danh sách thành viên
//                    List<User> members = localProjectDAO.getProjectMembers(projectId);
//                    memberList.clear();
//                    memberList.addAll(members);
//
//                    // Cập nhật UI trên main thread
//                    runOnUiThread(() -> {
//                        showProgress(false);
//
//                        // Điền thông tin dự án vào form
//                        etProjectName.setText(existingProject.getName());
//                        etProjectDescription.setText(existingProject.getDescription());
//                        etStartDate.setText(DateTimeUtils.formatDisplayDate(existingProject.getStartDate()));
//                        etEndDate.setText(DateTimeUtils.formatDisplayDate(existingProject.getEndDate()));
//
//                        // Thiết lập độ ưu tiên
//                        switch (existingProject.getPriority()) {
//                            case "Cao":
//                                radioPriorityHigh.setChecked(true);
//                                break;
//                            case "Trung bình":
//                                radioPriorityMedium.setChecked(true);
//                                break;
//                            case "Thấp":
//                                radioPriorityLow.setChecked(true);
//                                break;
//                        }
//
//                        // Cập nhật RecyclerView
//                        memberAdapter.notifyDataSetChanged();
//                        updateMembersVisibility();
//                    });
//                } else {
//                    // Không tìm thấy dự án
//                    runOnUiThread(() -> {
//                        showProgress(false);
//                        Toast.makeText(ProjectActivity.this, R.string.error_loading_project, Toast.LENGTH_SHORT).show();
//                        finish();
//                    });
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                runOnUiThread(() -> {
//                    showProgress(false);
//                    Toast.makeText(ProjectActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
//                    finish();
//                });
//            } finally {
//                if (database != null && dbManager != null) {
//                    dbManager.closeDatabase();
//                }
//            }
//        }).start();
//    }
//
//    private void saveProject() {
//        // Kiểm tra và lấy dữ liệu từ form
//        if (!validateForm()) {
//            return;
//        }
//
//        // Lấy dữ liệu từ form
//        String name = etProjectName.getText().toString().trim();
//        String description = etProjectDescription.getText().toString().trim();
//        String startDateDisplay = etStartDate.getText().toString().trim();
//        String endDateDisplay = etEndDate.getText().toString().trim();
//
//        // Chuyển đổi định dạng ngày
//        String startDate = DateTimeUtils.parseDisplayDate(startDateDisplay);
//        String endDate = DateTimeUtils.parseDisplayDate(endDateDisplay);
//
//        // Lấy độ ưu tiên
//        int selectedPriorityId = radioGroupPriority.getCheckedRadioButtonId();
//        String priority;
//        if (selectedPriorityId == R.id.radio_priority_high) {
//            priority = "Cao";
//        } else if (selectedPriorityId == R.id.radio_priority_low) {
//            priority = "Thấp";
//        } else {
//            priority = "Trung bình";
//        }
//
//        // Hiển thị progress
//        showProgress(true);
//
//        // Lưu dự án vào database
//        new Thread(() -> {
//            DatabaseManager dbManager = null;
//            SQLiteDatabase database = null;
//
//            try {
//                dbManager = DatabaseManager.getInstance(this);
//                database = dbManager.openDatabase();
//
//                ProjectDAO localProjectDAO = new ProjectDAO(this);
//                localProjectDAO.setDatabase(database);
//
//                long result;
//                if (isEditing && existingProject != null) {
//                    // Cập nhật dự án hiện có
//                    existingProject.setName(name);
//                    existingProject.setDescription(description);
//                    existingProject.setStartDate(startDate);
//                    existingProject.setEndDate(endDate);
//                    existingProject.setPriority(priority);
//
//                    // Bắt đầu giao dịch
//                    database.beginTransaction();
//
//                    try {
//                        // Cập nhật dự án
//                        int updateCount = localProjectDAO.updateProject(existingProject);
//                        result = updateCount > 0 ? existingProject.getId() : -1;
//
//                        // Cập nhật danh sách thành viên
//                        if (result != -1) {
//                            // Xóa tất cả thành viên hiện có (trừ người tạo)
//                            // Thay vì xóa và thêm lại, chúng ta sẽ kiểm tra xem thành viên đã tồn tại chưa
//                            List<User> existingMembers = localProjectDAO.getProjectMembers(existingProject.getId());
//                            List<Long> existingMemberIds = new ArrayList<>();
//
//                            // Lấy danh sách ID của các thành viên hiện tại
//                            for (User member : existingMembers) {
//                                existingMemberIds.add(member.getId());
//                            }
//
//                            // Thêm các thành viên mới
//                            for (User member : memberList) {
//                                // Kiểm tra xem thành viên đã tồn tại chưa
//                                if (!existingMemberIds.contains(member.getId())) {
//                                    // Nếu chưa tồn tại, thêm vào dự án
//                                    localProjectDAO.addMember(existingProject.getId(), member.getId(), "Thành viên");
//                                }
//                            }
//
//                            // Xóa các thành viên không còn trong danh sách (trừ người tạo)
//                            for (User existingMember : existingMembers) {
//                                if (existingMember.getId() != existingProject.getCreatedBy()) {
//                                    boolean shouldKeep = false;
//
//                                    // Kiểm tra xem thành viên có trong danh sách mới không
//                                    for (User member : memberList) {
//                                        if (existingMember.getId() == member.getId()) {
//                                            shouldKeep = true;
//                                            break;
//                                        }
//                                    }
//
//                                    // Nếu không còn trong danh sách mới, xóa khỏi dự án
//                                    if (!shouldKeep) {
//                                        localProjectDAO.removeMemberFromProject(existingProject.getId(), existingMember.getId());
//                                    }
//                                }
//                            }
//                        }
//
//                        // Đánh dấu giao dịch thành công
//                        database.setTransactionSuccessful();
//                    } finally {
//                        // Kết thúc giao dịch
//                        database.endTransaction();
//                    }
//                } else {
//                    // Tạo dự án mới
//                    Project newProject = new Project(
//                            name,
//                            description,
//                            sessionManager.getUserId(),
//                            startDate,
//                            endDate,
//                            priority
//                    );
//
//                    // Bắt đầu giao dịch
//                    database.beginTransaction();
//
//                    try {
//                        // Lưu dự án
//                        result = localProjectDAO.createProject(newProject);
//
//                        // Thêm các thành viên vào dự án
//                        if (result != -1) {
//                            for (User member : memberList) {
//                                if (member.getId() != sessionManager.getUserId()) {
//                                    localProjectDAO.addMember(result, member.getId(), "Thành viên");
//                                }
//                            }
//                        }
//
//                        // Đánh dấu giao dịch thành công
//                        database.setTransactionSuccessful();
//                    } finally {
//                        // Kết thúc giao dịch
//                        database.endTransaction();
//                    }
//                }
//
//                // Kết quả cuối cùng
//                final boolean success = result != -1;
//
//                // Cập nhật UI trên main thread
//                runOnUiThread(() -> {
//                    showProgress(false);
//
//                    if (success) {
//                        // Thông báo thành công
//                        Toast.makeText(ProjectActivity.this,
//                                isEditing ? R.string.project_updated : R.string.project_created,
//                                Toast.LENGTH_SHORT).show();
//
//                        // Đóng màn hình
//                        finish();
//                    } else {
//                        // Thông báo lỗi
//                        Toast.makeText(ProjectActivity.this,
//                                isEditing ? R.string.error_updating_project : R.string.error_creating_project,
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//            } catch (Exception e) {
//                e.printStackTrace();
//                runOnUiThread(() -> {
//                    showProgress(false);
//                    Toast.makeText(ProjectActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
//                });
//            } finally {
//                if (database != null && dbManager != null) {
//                    dbManager.closeDatabase();
//                }
//            }
//        }).start();
//    }
//
//    private boolean validateForm() {
//        boolean valid = true;
//
//        // Kiểm tra tên dự án
//        if (etProjectName.getText().toString().trim().isEmpty()) {
//            tilProjectName.setError(getString(R.string.fill_all_fields));
//            valid = false;
//        } else {
//            tilProjectName.setError(null);
//        }
//
//        // Kiểm tra ngày bắt đầu
//        if (etStartDate.getText().toString().trim().isEmpty()) {
//            tilStartDate.setError(getString(R.string.fill_all_fields));
//            valid = false;
//        } else {
//            tilStartDate.setError(null);
//        }
//
//        // Kiểm tra ngày kết thúc
//        if (etEndDate.getText().toString().trim().isEmpty()) {
//            tilEndDate.setError(getString(R.string.fill_all_fields));
//            valid = false;
//        } else {
//            // Kiểm tra ngày kết thúc phải sau ngày bắt đầu
//            String startDateStr = etStartDate.getText().toString().trim();
//            String endDateStr = etEndDate.getText().toString().trim();
//
//            if (!startDateStr.isEmpty() && !endDateStr.isEmpty()) {
//                String startDate = DateTimeUtils.parseDisplayDate(startDateStr);
//                String endDate = DateTimeUtils.parseDisplayDate(endDateStr);
//
//                if (startDate.compareTo(endDate) > 0) {
//                    tilEndDate.setError(getString(R.string.end_date_must_after_start_date));
//                    valid = false;
//                } else {
//                    tilEndDate.setError(null);
//                }
//            }
//        }
//
//        return valid;
//    }
//
//    private void showProgress(boolean show) {
//        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
//        etProjectName.setEnabled(!show);
//        etProjectDescription.setEnabled(!show);
//        etStartDate.setEnabled(!show);
//        etEndDate.setEnabled(!show);
//        etSearchMember.setEnabled(!show);
//        btnAddMember.setEnabled(!show && etSearchMember.getText().length() > 0);
//        btnCreateProject.setEnabled(!show);
//        radioGroupPriority.setEnabled(!show);
//        radioPriorityHigh.setEnabled(!show);
//        radioPriorityMedium.setEnabled(!show);
//        radioPriorityLow.setEnabled(!show);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            onBackPressed();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public void onRemoveMember(User member) {
//        // Không cho phép xóa người tạo dự án
//        if (isEditing && existingProject != null && member.getId() == existingProject.getCreatedBy()) {
//            Toast.makeText(this, R.string.cannot_remove_creator, Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Xóa thành viên khỏi danh sách
//        memberList.remove(member);
//        memberAdapter.notifyDataSetChanged();
//        updateMembersVisibility();
//    }
//}

package com.example.taskmanager.activities;

import android.app.DatePickerDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.adapters.MemberAdapter;
import com.example.taskmanager.database.DatabaseManager;
import com.example.taskmanager.database.NotificationDAO;
import com.example.taskmanager.database.ProjectDAO;
import com.example.taskmanager.database.UserDAO;
import com.example.taskmanager.models.Notification;
import com.example.taskmanager.models.Project;
import com.example.taskmanager.models.User;
import com.example.taskmanager.utils.DateTimeUtils;
import com.example.taskmanager.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProjectActivity extends AppCompatActivity implements MemberAdapter.OnMemberActionListener {

    private Toolbar toolbar;
    private TextInputLayout tilProjectName, tilProjectDescription, tilStartDate, tilEndDate, tilSearchMember;
    private TextInputEditText etProjectName, etProjectDescription, etStartDate, etEndDate, etSearchMember;
    private RadioGroup radioGroupPriority;
    private RadioButton radioPriorityHigh, radioPriorityMedium, radioPriorityLow;
    private Button btnAddMember, btnCreateProject, btnDeleteProject;
    private RecyclerView recyclerViewMembers;
    private TextView tvNoMembers;
    private ProgressBar progressBar;

    private ProjectDAO projectDAO;
    private UserDAO userDAO;
    private SessionManager sessionManager;

    private MemberAdapter memberAdapter;
    private List<User> memberList;

    private Project existingProject; // Null nếu đang tạo dự án mới
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        // Khởi tạo các thành phần giao diện
        initViews();

        // Thiết lập toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Khởi tạo database và session
        projectDAO = new ProjectDAO(this);
        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);

        // Thiết lập RecyclerView cho danh sách thành viên
        setupMembersRecyclerView();

        // Kiểm tra xem đang tạo mới hay chỉnh sửa dự án
        if (getIntent().hasExtra("PROJECT_ID")) {
            // Chỉnh sửa dự án hiện có
            isEditing = true;
            long projectId = getIntent().getLongExtra("PROJECT_ID", -1);
            loadExistingProject(projectId);
            getSupportActionBar().setTitle(R.string.edit_project);
            btnCreateProject.setText(R.string.save);

            // Hiển thị nút xóa khi đang chỉnh sửa dự án
            btnDeleteProject.setVisibility(View.VISIBLE);
        } else {
            // Tạo dự án mới
            getSupportActionBar().setTitle(R.string.new_project);

            // Ẩn nút xóa khi tạo dự án mới
            btnDeleteProject.setVisibility(View.GONE);

            // Thêm người dùng hiện tại vào danh sách thành viên
            addCurrentUserAsMember();
        }

        // Thiết lập sự kiện cho các view
        setupViewListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilProjectName = findViewById(R.id.til_project_name);
        tilProjectDescription = findViewById(R.id.til_project_description);
        tilStartDate = findViewById(R.id.til_start_date);
        tilEndDate = findViewById(R.id.til_end_date);
        tilSearchMember = findViewById(R.id.til_search_member);

        etProjectName = findViewById(R.id.et_project_name);
        etProjectDescription = findViewById(R.id.et_project_description);
        etStartDate = findViewById(R.id.et_start_date);
        etEndDate = findViewById(R.id.et_end_date);
        etSearchMember = findViewById(R.id.et_search_member);

        radioGroupPriority = findViewById(R.id.radio_group_priority);
        radioPriorityHigh = findViewById(R.id.radio_priority_high);
        radioPriorityMedium = findViewById(R.id.radio_priority_medium);
        radioPriorityLow = findViewById(R.id.radio_priority_low);

        btnAddMember = findViewById(R.id.btn_add_member);
        btnCreateProject = findViewById(R.id.btn_create_project);
        btnDeleteProject = findViewById(R.id.btn_delete_project);

        recyclerViewMembers = findViewById(R.id.recycler_view_members);
        tvNoMembers = findViewById(R.id.tv_no_members);

        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupMembersRecyclerView() {
        memberList = new ArrayList<>();
        memberAdapter = new MemberAdapter(this, memberList, this);
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMembers.setAdapter(memberAdapter);
    }

    private void setupViewListeners() {
        // Sự kiện cho ngày bắt đầu
        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));

        // Sự kiện cho ngày kết thúc
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));

        // Sự kiện cho nút thêm thành viên
        btnAddMember.setOnClickListener(v -> searchAndAddMember());

        // Sự kiện cho nút tạo dự án
        btnCreateProject.setOnClickListener(v -> saveProject());

        // Sự kiện cho nút xóa dự án
        btnDeleteProject.setOnClickListener(v -> {
            if (existingProject != null) {
                showDeleteConfirmationDialog();
            }
        });

        // Sự kiện cho ô tìm kiếm thành viên
        etSearchMember.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Bật nút thêm thành viên nếu có nội dung tìm kiếm
                btnAddMember.setEnabled(s.length() > 0);
            }
        });
    }

    private void showDatePickerDialog(final TextInputEditText editText) {
        // Tạo đối tượng Calendar với ngày hiện tại
        final Calendar calendar = Calendar.getInstance();

        // Nếu đã có ngày trong EditText, sử dụng ngày đó
        String currentDate = editText.getText().toString();
        if (!currentDate.isEmpty()) {
            try {
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = displayFormat.parse(currentDate);
                if (date != null) {
                    calendar.setTime(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Tạo DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Cập nhật ngày đã chọn vào EditText
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    String formattedDate = DateTimeUtils.formatDisplayDate(DateTimeUtils.formatDate(calendar.getTime()));
                    editText.setText(formattedDate);
                },
                year, month, day);

        // Hiển thị hộp thoại
        datePickerDialog.show();
    }

    private void addCurrentUserAsMember() {
        User currentUser = sessionManager.getUserDetails();
        if (currentUser != null && !isMemberAlreadyAdded(currentUser.getId())) {
            memberList.add(currentUser);
            memberAdapter.notifyDataSetChanged();
            updateMembersVisibility();
        }
    }

    private void searchAndAddMember() {
        final String username = etSearchMember.getText().toString().trim();
        if (username.isEmpty()) {
            tilSearchMember.setError(getString(R.string.fill_all_fields));
            return;
        }

        // Xóa lỗi nếu có
        tilSearchMember.setError(null);

        // Hiển thị progress
        showProgress(true);

        // Tìm kiếm người dùng trong database
        new Thread(() -> {
            DatabaseManager dbManager = null;
            SQLiteDatabase database = null;

            try {
                dbManager = DatabaseManager.getInstance(this);
                database = dbManager.openDatabase();

                UserDAO localUserDAO = new UserDAO(this);
                localUserDAO.setDatabase(database);

                final User user = localUserDAO.getUserByUsername(username);

                // Cập nhật UI trên main thread
                runOnUiThread(() -> {
                    showProgress(false);

                    if (user != null) {
                        // Kiểm tra xem người dùng đã có trong danh sách chưa
                        if (isMemberAlreadyAdded(user.getId())) {
                            Toast.makeText(ProjectActivity.this, getString(R.string.member_already_added), Toast.LENGTH_SHORT).show();
                        } else {
                            // Thêm người dùng vào danh sách thành viên
                            memberList.add(user);
                            memberAdapter.notifyDataSetChanged();
                            updateMembersVisibility();

                            // Xóa nội dung tìm kiếm
                            etSearchMember.setText("");
                        }
                    } else {
                        // Không tìm thấy người dùng
                        tilSearchMember.setError(getString(R.string.user_not_found));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(ProjectActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (database != null && dbManager != null) {
                    dbManager.closeDatabase();
                }
            }
        }).start();
    }

    private boolean isMemberAlreadyAdded(long userId) {
        for (User member : memberList) {
            if (member.getId() == userId) {
                return true;
            }
        }
        return false;
    }

    private void updateMembersVisibility() {
        if (memberList.isEmpty()) {
            tvNoMembers.setVisibility(View.VISIBLE);
            recyclerViewMembers.setVisibility(View.GONE);
        } else {
            tvNoMembers.setVisibility(View.GONE);
            recyclerViewMembers.setVisibility(View.VISIBLE);
        }
    }

    private void loadExistingProject(long projectId) {
        showProgress(true);

        new Thread(() -> {
            DatabaseManager dbManager = null;
            SQLiteDatabase database = null;

            try {
                dbManager = DatabaseManager.getInstance(this);
                database = dbManager.openDatabase();

                ProjectDAO localProjectDAO = new ProjectDAO(this);
                localProjectDAO.setDatabase(database);

                existingProject = localProjectDAO.getProjectById(projectId);

                if (existingProject != null) {
                    // Lấy danh sách thành viên
                    List<User> members = localProjectDAO.getProjectMembers(projectId);
                    memberList.clear();
                    memberList.addAll(members);

                    // Cập nhật UI trên main thread
                    runOnUiThread(() -> {
                        showProgress(false);

                        // Điền thông tin dự án vào form
                        etProjectName.setText(existingProject.getName());
                        etProjectDescription.setText(existingProject.getDescription());
                        etStartDate.setText(DateTimeUtils.formatDisplayDate(existingProject.getStartDate()));
                        etEndDate.setText(DateTimeUtils.formatDisplayDate(existingProject.getEndDate()));

                        // Thiết lập độ ưu tiên
                        switch (existingProject.getPriority()) {
                            case "Cao":
                                radioPriorityHigh.setChecked(true);
                                break;
                            case "Trung bình":
                                radioPriorityMedium.setChecked(true);
                                break;
                            case "Thấp":
                                radioPriorityLow.setChecked(true);
                                break;
                        }

                        // Cập nhật RecyclerView
                        memberAdapter.notifyDataSetChanged();
                        updateMembersVisibility();
                    });
                } else {
                    // Không tìm thấy dự án
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(ProjectActivity.this, R.string.error_loading_project, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(ProjectActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            } finally {
                if (database != null && dbManager != null) {
                    dbManager.closeDatabase();
                }
            }
        }).start();
    }

    private void saveProject() {
        // Kiểm tra và lấy dữ liệu từ form
        if (!validateForm()) {
            return;
        }

        // Lấy dữ liệu từ form
        String name = etProjectName.getText().toString().trim();
        String description = etProjectDescription.getText().toString().trim();
        String startDateDisplay = etStartDate.getText().toString().trim();
        String endDateDisplay = etEndDate.getText().toString().trim();

        // Chuyển đổi định dạng ngày
        String startDate = DateTimeUtils.parseDisplayDate(startDateDisplay);
        String endDate = DateTimeUtils.parseDisplayDate(endDateDisplay);

        // Lấy độ ưu tiên
        int selectedPriorityId = radioGroupPriority.getCheckedRadioButtonId();
        String priority;
        if (selectedPriorityId == R.id.radio_priority_high) {
            priority = "Cao";
        } else if (selectedPriorityId == R.id.radio_priority_low) {
            priority = "Thấp";
        } else {
            priority = "Trung bình";
        }

        // Hiển thị progress
        showProgress(true);

        // Lưu dự án vào database
        new Thread(() -> {
            DatabaseManager dbManager = null;
            SQLiteDatabase database = null;

            try {
                dbManager = DatabaseManager.getInstance(this);
                database = dbManager.openDatabase();

                ProjectDAO localProjectDAO = new ProjectDAO(this);
                localProjectDAO.setDatabase(database);

                long result;
                if (isEditing && existingProject != null) {
                    // Cập nhật dự án hiện có
                    existingProject.setName(name);
                    existingProject.setDescription(description);
                    existingProject.setStartDate(startDate);
                    existingProject.setEndDate(endDate);
                    existingProject.setPriority(priority);

                    // Bắt đầu giao dịch
                    database.beginTransaction();

                    try {
                        // Cập nhật dự án
                        int updateCount = localProjectDAO.updateProject(existingProject);
                        result = updateCount > 0 ? existingProject.getId() : -1;

                        // Cập nhật danh sách thành viên
                        if (result != -1) {
                            // Xóa tất cả thành viên hiện có (trừ người tạo)
                            // Thay vì xóa và thêm lại, chúng ta sẽ kiểm tra xem thành viên đã tồn tại chưa
                            List<User> existingMembers = localProjectDAO.getProjectMembers(existingProject.getId());
                            List<Long> existingMemberIds = new ArrayList<>();

                            // Lấy danh sách ID của các thành viên hiện tại
                            for (User member : existingMembers) {
                                existingMemberIds.add(member.getId());
                            }

                            // Thêm các thành viên mới
                            for (User member : memberList) {
                                // Kiểm tra xem thành viên đã tồn tại chưa
                                if (!existingMemberIds.contains(member.getId())) {
                                    // Nếu chưa tồn tại, thêm vào dự án
                                    localProjectDAO.addMember(existingProject.getId(), member.getId(), "Thành viên");
                                }
                            }

                            // Xóa các thành viên không còn trong danh sách (trừ người tạo)
                            for (User existingMember : existingMembers) {
                                if (existingMember.getId() != existingProject.getCreatedBy()) {
                                    boolean shouldKeep = false;

                                    // Kiểm tra xem thành viên có trong danh sách mới không
                                    for (User member : memberList) {
                                        if (existingMember.getId() == member.getId()) {
                                            shouldKeep = true;
                                            break;
                                        }
                                    }

                                    // Nếu không còn trong danh sách mới, xóa khỏi dự án
                                    if (!shouldKeep) {
                                        localProjectDAO.removeMemberFromProject(existingProject.getId(), existingMember.getId());
                                    }
                                }
                            }
                        }

                        // Đánh dấu giao dịch thành công
                        database.setTransactionSuccessful();
                    } finally {
                        // Kết thúc giao dịch
                        database.endTransaction();
                    }
                } else {
                    // Tạo dự án mới
                    Project newProject = new Project(
                            name,
                            description,
                            sessionManager.getUserId(),
                            startDate,
                            endDate,
                            priority
                    );

                    // Bắt đầu giao dịch
                    database.beginTransaction();

                    try {
                        // Lưu dự án
                        result = localProjectDAO.createProject(newProject);

                        // Thêm các thành viên vào dự án
                        if (result != -1) {
                            for (User member : memberList) {
                                if (member.getId() != sessionManager.getUserId()) {
                                    localProjectDAO.addMember(result, member.getId(), "Thành viên");
                                }
                            }
                        }

                        // Đánh dấu giao dịch thành công
                        database.setTransactionSuccessful();
                    } finally {
                        // Kết thúc giao dịch
                        database.endTransaction();
                    }
                }

                // Kết quả cuối cùng
                final boolean success = result != -1;

                // Cập nhật UI trên main thread
                runOnUiThread(() -> {
                    showProgress(false);

                    if (success) {
                        // Thông báo thành công
                        Toast.makeText(ProjectActivity.this,
                                isEditing ? R.string.project_updated : R.string.project_created,
                                Toast.LENGTH_SHORT).show();

                        // Đóng màn hình
                        finish();
                    } else {
                        // Thông báo lỗi
                        Toast.makeText(ProjectActivity.this,
                                isEditing ? R.string.error_updating_project : R.string.error_creating_project,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(ProjectActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (database != null && dbManager != null) {
                    dbManager.closeDatabase();
                }
            }
        }).start();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_project)
                .setMessage(R.string.confirm_delete_project)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteProject())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteProject() {
        if (existingProject == null) {
            return;
        }

        showProgress(true);

        new Thread(() -> {
            DatabaseManager dbManager = null;
            SQLiteDatabase database = null;

            try {
                dbManager = DatabaseManager.getInstance(this);
                database = dbManager.openDatabase();

                ProjectDAO localProjectDAO = new ProjectDAO(this);
                localProjectDAO.setDatabase(database);

                // Kiểm tra xem người dùng hiện tại có phải là người tạo dự án không
                if (existingProject.getCreatedBy() != sessionManager.getUserId()) {
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(this, R.string.cannot_delete_project, Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Thực hiện xóa dự án
                int result = localProjectDAO.deleteProject(existingProject.getId());

                final boolean success = result > 0;
                runOnUiThread(() -> {
                    showProgress(false);
                    if (success) {
                        Toast.makeText(this, R.string.project_deleted, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, R.string.error_deleting_project, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (database != null && dbManager != null) {
                    dbManager.closeDatabase();
                }
            }
        }).start();
    }

    private void createAddProjectMemberNotifications(Project project, List<User> newMembers) {
        NotificationDAO notificationDAO = new NotificationDAO(this);
        notificationDAO.open();

        for (User member : newMembers) {
            // Chỉ tạo thông báo cho người dùng khác với người đang thực hiện thao tác
            if (member.getId() != sessionManager.getUserId()) {
                // Tạo thông báo
                Notification notification = new Notification(
                        member.getId(),
                        "Dự án mới",
                        "Bạn đã được thêm vào dự án: " + project.getName(),
                        project.getId(),
                        "project_added" // Bạn cần thêm hằng số này vào Notification.java
                );

                // Lưu thông báo
                notificationDAO.createNotification(notification);
            }
        }

        notificationDAO.close();
    }

    private boolean validateForm() {
        boolean valid = true;

        // Kiểm tra tên dự án
        if (etProjectName.getText().toString().trim().isEmpty()) {
            tilProjectName.setError(getString(R.string.fill_all_fields));
            valid = false;
        } else {
            tilProjectName.setError(null);
        }

        // Kiểm tra ngày bắt đầu
        if (etStartDate.getText().toString().trim().isEmpty()) {
            tilStartDate.setError(getString(R.string.fill_all_fields));
            valid = false;
        } else {
            tilStartDate.setError(null);
        }

        // Kiểm tra ngày kết thúc
        if (etEndDate.getText().toString().trim().isEmpty()) {
            tilEndDate.setError(getString(R.string.fill_all_fields));
            valid = false;
        } else {
            // Kiểm tra ngày kết thúc phải sau ngày bắt đầu
            String startDateStr = etStartDate.getText().toString().trim();
            String endDateStr = etEndDate.getText().toString().trim();

            if (!startDateStr.isEmpty() && !endDateStr.isEmpty()) {
                String startDate = DateTimeUtils.parseDisplayDate(startDateStr);
                String endDate = DateTimeUtils.parseDisplayDate(endDateStr);

                if (startDate.compareTo(endDate) > 0) {
                    tilEndDate.setError(getString(R.string.end_date_must_after_start_date));
                    valid = false;
                } else {
                    tilEndDate.setError(null);
                }
            }
        }

        return valid;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        etProjectName.setEnabled(!show);
        etProjectDescription.setEnabled(!show);
        etStartDate.setEnabled(!show);
        etEndDate.setEnabled(!show);
        etSearchMember.setEnabled(!show);
        btnAddMember.setEnabled(!show && etSearchMember.getText().length() > 0);
        btnCreateProject.setEnabled(!show);
        btnDeleteProject.setEnabled(!show);
        radioGroupPriority.setEnabled(!show);
        radioPriorityHigh.setEnabled(!show);
        radioPriorityMedium.setEnabled(!show);
        radioPriorityLow.setEnabled(!show);
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
    public void onRemoveMember(User member) {
        // Không cho phép xóa người tạo dự án
        if (isEditing && existingProject != null && member.getId() == existingProject.getCreatedBy()) {
            Toast.makeText(this, R.string.cannot_remove_creator, Toast.LENGTH_SHORT).show();
            return;
        }

        // Xóa thành viên khỏi danh sách
        memberList.remove(member);
        memberAdapter.notifyDataSetChanged();
        updateMembersVisibility();
    }
}