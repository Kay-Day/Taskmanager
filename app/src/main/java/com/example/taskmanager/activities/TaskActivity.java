package com.example.taskmanager.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.taskmanager.R;
import com.example.taskmanager.database.NotificationDAO;
import com.example.taskmanager.database.ProjectDAO;
import com.example.taskmanager.database.TaskDAO;
import com.example.taskmanager.database.UserDAO;
import com.example.taskmanager.models.Notification;
import com.example.taskmanager.models.Project;
import com.example.taskmanager.models.Task;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout tilTaskTitle, tilTaskDescription, tilResourceLink, tilStartDate, tilDueDate;
    private TextInputEditText etTaskTitle, etTaskDescription, etResourceLink, etStartDate, etDueDate;
    private RadioGroup radioGroupPriority;
    private RadioButton radioPriorityHigh, radioPriorityMedium, radioPriorityLow;
    private Spinner spinnerAssignee, spinnerStatus;
    private Button btnSaveTask;
    private ProgressBar progressBar;

    private TaskDAO taskDAO;
    private ProjectDAO projectDAO;
    private UserDAO userDAO;
    private SessionManager sessionManager;

    private List<User> memberList;
    private Map<Integer, Long> memberIdMap; // Map vị trí trong spinner với ID thành viên

    private long projectId; // ID của dự án khi tạo nhiệm vụ mới
    private Task existingTask; // Null nếu đang tạo nhiệm vụ mới
    private boolean isEditing = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);



        // Khởi tạo các thành phần giao diện
        initViews();



        // Thiết lập toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Khởi tạo database và session
        taskDAO = new TaskDAO(this);
        projectDAO = new ProjectDAO(this);
        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);

        // Khởi tạo danh sách thành viên
        memberList = new ArrayList<>();
        memberIdMap = new HashMap<>();

        // Kiểm tra xem đang tạo mới hay chỉnh sửa nhiệm vụ
        if (getIntent().hasExtra("TASK_ID")) {
            // Chỉnh sửa nhiệm vụ hiện có
            isEditing = true;
            long taskId = getIntent().getLongExtra("TASK_ID", -1);
            loadExistingTask(taskId);
            getSupportActionBar().setTitle(R.string.edit_task);
            btnSaveTask.setText(R.string.save);
        } else if (getIntent().hasExtra("PROJECT_ID")) {
            // Tạo nhiệm vụ mới cho dự án cụ thể
            projectId = getIntent().getLongExtra("PROJECT_ID", -1);
            loadProjectMembers(projectId);
            getSupportActionBar().setTitle(R.string.new_task);
        } else {
            // Không có thông tin dự án, kết thúc activity
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Thiết lập spinner trạng thái
        setupStatusSpinner();

        // Thiết lập sự kiện cho các view
        setupViewListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilTaskTitle = findViewById(R.id.til_task_title);
        tilTaskDescription = findViewById(R.id.til_task_description);
        tilResourceLink = findViewById(R.id.til_resource_link);
        tilStartDate = findViewById(R.id.til_start_date);
        tilDueDate = findViewById(R.id.til_due_date);

        etTaskTitle = findViewById(R.id.et_task_title);
        etTaskDescription = findViewById(R.id.et_task_description);
        etResourceLink = findViewById(R.id.et_resource_link);
        etStartDate = findViewById(R.id.et_start_date);
        etDueDate = findViewById(R.id.et_due_date);

        radioGroupPriority = findViewById(R.id.radio_group_priority);
        radioPriorityHigh = findViewById(R.id.radio_priority_high);
        radioPriorityMedium = findViewById(R.id.radio_priority_medium);
        radioPriorityLow = findViewById(R.id.radio_priority_low);

        spinnerAssignee = findViewById(R.id.spinner_assignee);
        spinnerStatus = findViewById(R.id.spinner_status);

        btnSaveTask = findViewById(R.id.btn_save_task);

        progressBar = findViewById(R.id.progress_bar);
    }

    private void createTaskAssignmentNotification(Task task) {
        // Kiểm tra xem task có được giao cho người dùng khác không
        if (task.getAssignedTo() != null && task.getAssignedTo() != sessionManager.getUserId()) {
            // Tạo thông báo cho người được giao nhiệm vụ
            Notification notification = new Notification(
                    task.getAssignedTo(), // ID người nhận thông báo
                    "Nhiệm vụ mới",
                    "Bạn đã được giao nhiệm vụ mới: " + task.getTitle(),
                    task.getId(),
                    Notification.TYPE_NEW_TASK
            );

            // Lưu thông báo vào cơ sở dữ liệu
            NotificationDAO notificationDAO = new NotificationDAO(this);
            notificationDAO.open();
            notificationDAO.createNotification(notification);
            notificationDAO.close();
        }
    }

    private void setupStatusSpinner() {
        // Tạo mảng trạng thái
        String[] statuses = new String[]{
                getString(R.string.not_started),
                getString(R.string.in_progress),
                getString(R.string.completed),
                getString(R.string.delayed)
        };

        // Tạo adapter cho spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);
    }

    private void setupViewListeners() {
        // Sự kiện cho ngày bắt đầu
        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));

        // Sự kiện cho ngày đến hạn
        etDueDate.setOnClickListener(v -> showDatePickerDialog(etDueDate));

        // Sự kiện cho nút lưu nhiệm vụ
        btnSaveTask.setOnClickListener(v -> saveTask());
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

        // Phần còn lại không thay đổi
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

    private void loadProjectMembers(long projectId) {
        showProgress(true);

        new Thread(() -> {
            projectDAO.open();
            Project project = projectDAO.getProjectById(projectId);
            List<User> members = projectDAO.getProjectMembers(projectId);
            projectDAO.close();

            if (project == null || members == null) {
                // Không tìm thấy dự án hoặc danh sách thành viên
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(TaskActivity.this, R.string.error_loading_members, Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            // Lưu trữ danh sách thành viên
            memberList.addAll(members);

            // Cập nhật UI trên main thread
            runOnUiThread(() -> {
                showProgress(false);

                // Thiết lập spinner người được giao
                setupAssigneeSpinner();

                // Thiết lập ngày bắt đầu mặc định
                etStartDate.setText(DateTimeUtils.formatDisplayDate(DateTimeUtils.getCurrentDate()));
            });
        }).start();
    }

    private void loadExistingTask(long taskId) {
        showProgress(true);

        new Thread(() -> {
            taskDAO.open();
            existingTask = taskDAO.getTaskById(taskId);
            taskDAO.close();

            if (existingTask == null) {
                // Không tìm thấy nhiệm vụ
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(TaskActivity.this, R.string.error_loading_task, Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            // Lấy ID của dự án
            projectId = existingTask.getProjectId();

            // Lấy danh sách thành viên của dự án
            projectDAO.open();
            List<User> members = projectDAO.getProjectMembers(projectId);
            projectDAO.close();

            if (members == null) {
                // Không tìm thấy danh sách thành viên
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(TaskActivity.this, R.string.error_loading_members, Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            // Lưu trữ danh sách thành viên
            memberList.addAll(members);

            // Cập nhật UI trên main thread
            runOnUiThread(() -> {
                showProgress(false);

                // Thiết lập spinner người được giao
                setupAssigneeSpinner();

                // Điền thông tin nhiệm vụ vào form
                etTaskTitle.setText(existingTask.getTitle());
                etTaskDescription.setText(existingTask.getDescription());
                etResourceLink.setText(existingTask.getResourceLink());
                etStartDate.setText(DateTimeUtils.formatDisplayDate(existingTask.getStartDate()));
                etDueDate.setText(DateTimeUtils.formatDisplayDate(existingTask.getDueDate()));

                // Thiết lập độ ưu tiên
                switch (existingTask.getPriority()) {
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

                // Thiết lập trạng thái
                String status = existingTask.getStatus();
                int statusPosition = 0;
                if (status.equals(getString(R.string.in_progress))) {
                    statusPosition = 1;
                } else if (status.equals(getString(R.string.completed))) {
                    statusPosition = 2;
                } else if (status.equals(getString(R.string.delayed))) {
                    statusPosition = 3;
                }
                spinnerStatus.setSelection(statusPosition);

                // Thiết lập người được giao
                Long assignedToId = existingTask.getAssignedTo();
                if (assignedToId != null) {
                    for (int i = 0; i < memberList.size(); i++) {
                        if (memberList.get(i).getId() == assignedToId) {
                            spinnerAssignee.setSelection(i + 1); // +1 vì có mục "Chọn người thực hiện"
                            break;
                        }
                    }
                }
            });
        }).start();
    }

    private void setupAssigneeSpinner() {
        // Tạo danh sách tên thành viên
        List<String> memberNames = new ArrayList<>();
        memberNames.add(getString(R.string.select_assignee)); // Mục đầu tiên

        // Thêm tên thành viên vào danh sách
        for (int i = 0; i < memberList.size(); i++) {
            User member = memberList.get(i);
            memberNames.add(member.getFullName());
            memberIdMap.put(i + 1, member.getId()); // +1 vì có mục "Chọn người thực hiện"
        }

        // Tạo adapter cho spinner
        ArrayAdapter<String> assigneeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, memberNames);
        assigneeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAssignee.setAdapter(assigneeAdapter);

        // Thiết lập sự kiện cho spinner
        spinnerAssignee.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Không cần xử lý gì ở đây
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không cần xử lý gì ở đây
            }
        });
    }
    private void createTaskReassignmentNotification(Task task, Long oldAssignedTo) {
        // Kiểm tra xem nhiệm vụ có được giao cho người khác không
        if (task.getAssignedTo() != null &&
                !task.getAssignedTo().equals(oldAssignedTo) &&
                task.getAssignedTo() != sessionManager.getUserId()) {

            // Tạo thông báo cho người được giao nhiệm vụ mới
            Notification notification = new Notification(
                    task.getAssignedTo(),
                    "Nhiệm vụ mới",
                    "Bạn đã được giao nhiệm vụ: " + task.getTitle(),
                    task.getId(),
                    Notification.TYPE_NEW_TASK
            );

            // Lưu thông báo vào cơ sở dữ liệu
            NotificationDAO notificationDAO = new NotificationDAO(this);
            notificationDAO.open();
            notificationDAO.createNotification(notification);
            notificationDAO.close();
        }
    }

//    private void saveTask() {
//        // Kiểm tra và lấy dữ liệu từ form
//        if (!validateForm()) {
//            return;
//        }
//
//        // Lấy dữ liệu từ form
//        final String title = etTaskTitle.getText().toString().trim();
//        final String description = etTaskDescription.getText().toString().trim();
//        final String resourceLink = etResourceLink.getText().toString().trim();
//        final String startDateDisplay = etStartDate.getText().toString().trim();
//        final String dueDateDisplay = etDueDate.getText().toString().trim();
//
//        // Chuyển đổi định dạng ngày
//        final String startDate = DateTimeUtils.parseDisplayDate(startDateDisplay);
//        final String dueDate = DateTimeUtils.parseDisplayDate(dueDateDisplay);
//
//        // Lấy độ ưu tiên
//        final int selectedPriorityId = radioGroupPriority.getCheckedRadioButtonId();
//        final String priority;
//        if (selectedPriorityId == R.id.radio_priority_high) {
//            priority = "Cao";
//        } else if (selectedPriorityId == R.id.radio_priority_low) {
//            priority = "Thấp";
//        } else {
//            priority = "Trung bình";
//        }
//
//        // Lấy trạng thái
//        final String status = spinnerStatus.getSelectedItem().toString();
//
//        // Lấy ID người được giao
//        final Long assignedToId;
//        int assigneePosition = spinnerAssignee.getSelectedItemPosition();
//        if (assigneePosition > 0) { // > 0 vì mục đầu tiên là "Chọn người thực hiện"
//            assignedToId = memberIdMap.get(assigneePosition);
//        } else {
//            assignedToId = null;
//        }
//
//        // Hiển thị progress
//        showProgress(true);
//
//        // Lưu nhiệm vụ vào database
//        new Thread(() -> {
//            // Mở kết nối đến database
//            taskDAO.open();
//
//            long result;
//            if (isEditing && existingTask != null) {
//                // Tạo bản sao của nhiệm vụ hiện tại để ghi lại lịch sử thay đổi
//                Task oldTask = new Task(
//                        existingTask.getId(),
//                        existingTask.getProjectId(),
//                        existingTask.getTitle(),
//                        existingTask.getDescription(),
//                        existingTask.getAssignedTo(),
//                        existingTask.getCreatedBy(),
//                        existingTask.getPriority(),
//                        existingTask.getStatus(),
//                        existingTask.getResourceLink(),
//                        existingTask.getStartDate(),
//                        existingTask.getDueDate(),
//                        existingTask.getCreatedAt()
//                );
//
//                // Cập nhật nhiệm vụ hiện có
//                existingTask.setTitle(title);
//                existingTask.setDescription(description);
//                existingTask.setAssignedTo(assignedToId);
//                existingTask.setPriority(priority);
//                existingTask.setStatus(status);
//                existingTask.setResourceLink(resourceLink);
//                existingTask.setStartDate(startDate);
//                existingTask.setDueDate(dueDate);
//
//                // Cập nhật nhiệm vụ và lưu lịch sử thay đổi
//                int updateCount = taskDAO.updateTask(existingTask, oldTask, sessionManager.getUserId());
//                result = updateCount > 0 ? existingTask.getId() : -1;
//            } else {
//                // Tạo nhiệm vụ mới
//                Task newTask = new Task(
//                        projectId,
//                        title,
//                        description,
//                        assignedToId,
//                        sessionManager.getUserId(),
//                        priority,
//                        status,
//                        resourceLink,
//                        startDate,
//                        dueDate
//                );
//
//                // Lưu nhiệm vụ
//                result = taskDAO.createTask(newTask);
//            }
//
//            // Đóng kết nối database
//            taskDAO.close();
//
//            // Kết quả cuối cùng
//            final boolean success = result != -1;
//
//            // Cập nhật UI trên main thread
//            runOnUiThread(() -> {
//                showProgress(false);
//
//                if (success) {
//                    // Thông báo thành công
//                    Toast.makeText(TaskActivity.this,
//                            isEditing ? R.string.task_updated : R.string.task_created,
//                            Toast.LENGTH_SHORT).show();
//
//                    // Đóng màn hình
//                    finish();
//                } else {
//                    // Thông báo lỗi
//                    Toast.makeText(TaskActivity.this,
//                            isEditing ? R.string.error_updating_task : R.string.error_creating_task,
//                            Toast.LENGTH_SHORT).show();
//                }
//            });
//        }).start();
//    }

    private void saveTask() {
        // Kiểm tra và lấy dữ liệu từ form
        if (!validateForm()) {
            return;
        }

        // Lấy dữ liệu từ form
        final String title = etTaskTitle.getText().toString().trim();
        final String description = etTaskDescription.getText().toString().trim();
        final String resourceLink = etResourceLink.getText().toString().trim();
        final String startDateDisplay = etStartDate.getText().toString().trim();
        final String dueDateDisplay = etDueDate.getText().toString().trim();

        // Chuyển đổi định dạng ngày
        final String startDate = DateTimeUtils.parseDisplayDate(startDateDisplay);
        final String dueDate = DateTimeUtils.parseDisplayDate(dueDateDisplay);

        // Lấy độ ưu tiên
        final int selectedPriorityId = radioGroupPriority.getCheckedRadioButtonId();
        final String priority;
        if (selectedPriorityId == R.id.radio_priority_high) {
            priority = "Cao";
        } else if (selectedPriorityId == R.id.radio_priority_low) {
            priority = "Thấp";
        } else {
            priority = "Trung bình";
        }

        // Lấy trạng thái
        final String status = spinnerStatus.getSelectedItem().toString();

        // Lấy ID người được giao
        final Long assignedToId;
        int assigneePosition = spinnerAssignee.getSelectedItemPosition();
        if (assigneePosition > 0) { // > 0 vì mục đầu tiên là "Chọn người thực hiện"
            assignedToId = memberIdMap.get(assigneePosition);
        } else {
            assignedToId = null;
        }

        // Lưu lại người được giao việc trước đó (nếu đang chỉnh sửa)
        final Long oldAssignedTo = isEditing && existingTask != null ? existingTask.getAssignedTo() : null;

        // Hiển thị progress
        showProgress(true);

        // Lưu nhiệm vụ vào database
        new Thread(() -> {
            // Mở kết nối đến database
            taskDAO.open();

            long result;
            Task resultTask = null; // Biến để lưu nhiệm vụ kết quả

            if (isEditing && existingTask != null) {
                // Tạo bản sao của nhiệm vụ hiện tại để ghi lại lịch sử thay đổi
                Task oldTask = new Task(
                        existingTask.getId(),
                        existingTask.getProjectId(),
                        existingTask.getTitle(),
                        existingTask.getDescription(),
                        existingTask.getAssignedTo(),
                        existingTask.getCreatedBy(),
                        existingTask.getPriority(),
                        existingTask.getStatus(),
                        existingTask.getResourceLink(),
                        existingTask.getStartDate(),
                        existingTask.getDueDate(),
                        existingTask.getCreatedAt()
                );

                // Cập nhật nhiệm vụ hiện có
                existingTask.setTitle(title);
                existingTask.setDescription(description);
                existingTask.setAssignedTo(assignedToId);
                existingTask.setPriority(priority);
                existingTask.setStatus(status);
                existingTask.setResourceLink(resourceLink);
                existingTask.setStartDate(startDate);
                existingTask.setDueDate(dueDate);

                // Cập nhật nhiệm vụ và lưu lịch sử thay đổi
                int updateCount = taskDAO.updateTask(existingTask, oldTask, sessionManager.getUserId());
                result = updateCount > 0 ? existingTask.getId() : -1;

                if (result > 0) {
                    resultTask = existingTask;
                }
            } else {
                // Tạo nhiệm vụ mới
                Task newTask = new Task(
                        projectId,
                        title,
                        description,
                        assignedToId,
                        sessionManager.getUserId(),
                        priority,
                        status,
                        resourceLink,
                        startDate,
                        dueDate
                );

                // Lưu nhiệm vụ
                result = taskDAO.createTask(newTask);

                if (result > 0) {
                    // Cập nhật ID cho nhiệm vụ mới
                    newTask.setId(result);
                    resultTask = newTask;
                }
            }

            // Đóng kết nối database
            taskDAO.close();

            // Kết quả cuối cùng
            final boolean success = result != -1;
            final Task finalResultTask = resultTask;
            final Long finalOldAssignedTo = oldAssignedTo;

            // Cập nhật UI trên main thread
            runOnUiThread(() -> {
                showProgress(false);

                if (success) {
                    // TẠI ĐÂY: Thêm mã tạo thông báo
                    if (!isEditing) {
                        // Trường hợp tạo nhiệm vụ mới
                        if (finalResultTask != null) {
                            createTaskAssignmentNotification(finalResultTask);
                        }
                    } else {
                        // Trường hợp cập nhật nhiệm vụ, kiểm tra thay đổi người được giao
                        if (finalResultTask != null && (
                                (finalOldAssignedTo == null && finalResultTask.getAssignedTo() != null) ||
                                        (finalOldAssignedTo != null && finalResultTask.getAssignedTo() != null &&
                                                !finalOldAssignedTo.equals(finalResultTask.getAssignedTo())))) {
                            createTaskReassignmentNotification(finalResultTask, finalOldAssignedTo);
                        }
                    }

                    // Thông báo thành công
                    Toast.makeText(TaskActivity.this,
                            isEditing ? R.string.task_updated : R.string.task_created,
                            Toast.LENGTH_SHORT).show();

                    // Đóng màn hình
                    finish();
                } else {
                    // Thông báo lỗi
                    Toast.makeText(TaskActivity.this,
                            isEditing ? R.string.error_updating_task : R.string.error_creating_task,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private boolean validateForm() {
        boolean valid = true;

        // Kiểm tra tiêu đề nhiệm vụ
        if (etTaskTitle.getText().toString().trim().isEmpty()) {
            tilTaskTitle.setError(getString(R.string.fill_all_fields));
            valid = false;
        } else {
            tilTaskTitle.setError(null);
        }

        // Kiểm tra ngày bắt đầu
        if (etStartDate.getText().toString().trim().isEmpty()) {
            tilStartDate.setError(getString(R.string.fill_all_fields));
            valid = false;
        } else {
            tilStartDate.setError(null);
        }

        // Kiểm tra ngày đến hạn
        if (etDueDate.getText().toString().trim().isEmpty()) {
            tilDueDate.setError(getString(R.string.fill_all_fields));
            valid = false;
        } else {
            // Kiểm tra ngày đến hạn phải sau ngày bắt đầu
            String startDateStr = etStartDate.getText().toString().trim();
            String dueDateStr = etDueDate.getText().toString().trim();

            if (!startDateStr.isEmpty() && !dueDateStr.isEmpty()) {
                String startDate = DateTimeUtils.parseDisplayDate(startDateStr);
                String dueDate = DateTimeUtils.parseDisplayDate(dueDateStr);

                if (startDate.compareTo(dueDate) > 0) {
                    tilDueDate.setError(getString(R.string.due_date_must_after_start_date));
                    valid = false;
                } else {
                    tilDueDate.setError(null);
                }
            }
        }

        return valid;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        etTaskTitle.setEnabled(!show);
        etTaskDescription.setEnabled(!show);
        etResourceLink.setEnabled(!show);
        etStartDate.setEnabled(!show);
        etDueDate.setEnabled(!show);
        btnSaveTask.setEnabled(!show);
        radioGroupPriority.setEnabled(!show);
        radioPriorityHigh.setEnabled(!show);
        radioPriorityMedium.setEnabled(!show);
        radioPriorityLow.setEnabled(!show);
        spinnerAssignee.setEnabled(!show);
        spinnerStatus.setEnabled(!show);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}