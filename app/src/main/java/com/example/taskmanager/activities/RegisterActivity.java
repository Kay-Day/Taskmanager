package com.example.taskmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.taskmanager.R;
import com.example.taskmanager.database.UserDAO;
import com.example.taskmanager.models.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout tilFullName, tilEmail, tilUsername, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etEmail, etUsername, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;

    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo các thành phần giao diện
        initViews();

        // Thiết lập toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Khởi tạo database
        userDAO = new UserDAO(this);

        // Thiết lập sự kiện click
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilFullName = findViewById(R.id.til_full_name);
        tilEmail = findViewById(R.id.til_email);
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        // Sự kiện khi nhấn nút Đăng ký
        btnRegister.setOnClickListener(v -> attemptRegister());

        // Sự kiện khi nhấn vào "Đăng nhập ngay"
        tvLogin.setOnClickListener(v -> finish());

        // Sự kiện khi nhấn nút Back trên toolbar
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        // Reset lỗi
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilUsername.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        // Lấy giá trị từ trường nhập liệu
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Kiểm tra trường trống và hợp lệ
        boolean cancel = false;
        View focusView = null;

        // Kiểm tra xác nhận mật khẩu
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError(getString(R.string.fill_all_fields));
            focusView = etConfirmPassword;
            cancel = true;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.passwords_not_match));
            focusView = etConfirmPassword;
            cancel = true;
        }

        // Kiểm tra mật khẩu
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.fill_all_fields));
            focusView = etPassword;
            cancel = true;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.password_helper_text));
            focusView = etPassword;
            cancel = true;
        }

        // Kiểm tra tên đăng nhập
        if (username.isEmpty()) {
            tilUsername.setError(getString(R.string.fill_all_fields));
            focusView = etUsername;
            cancel = true;
        }

        // Kiểm tra email
        if (email.isEmpty()) {
            tilEmail.setError(getString(R.string.fill_all_fields));
            focusView = etEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            tilEmail.setError(getString(R.string.error));
            focusView = etEmail;
            cancel = true;
        }

        // Kiểm tra họ tên
        if (fullName.isEmpty()) {
            tilFullName.setError(getString(R.string.fill_all_fields));
            focusView = etFullName;
            cancel = true;
        }

        if (cancel) {
            // Có lỗi, tập trung vào trường lỗi đầu tiên
            focusView.requestFocus();
        } else {
            // Hiển thị progress bar và thực hiện đăng ký
            showProgress(true);

            // Tạo thread mới để xử lý thao tác database
            new Thread(() -> {
                // Mở kết nối đến database
                userDAO.open();

                // Kiểm tra xem tên đăng nhập đã tồn tại chưa
                final boolean usernameExists = userDAO.isUsernameExists(username);
                final boolean emailExists = userDAO.isEmailExists(email);

                // Nếu tên đăng nhập và email chưa tồn tại, tiến hành đăng ký
                long userId = -1;
                if (!usernameExists && !emailExists) {
                    User newUser = new User(username, password, fullName, email);
                    userId = userDAO.createUser(newUser);
                }

                // Đóng kết nối database
                userDAO.close();

                // Lưu kết quả để sử dụng trong UI thread
                final long finalUserId = userId;

                // Chuyển về main thread để cập nhật UI
                runOnUiThread(() -> {
                    showProgress(false);

                    if (usernameExists) {
                        // Tên đăng nhập đã tồn tại
                        tilUsername.setError(getString(R.string.username_exists));
                        etUsername.requestFocus();
                    } else if (emailExists) {
                        // Email đã tồn tại
                        tilEmail.setError(getString(R.string.email_exists));
                        etEmail.requestFocus();
                    } else if (finalUserId != -1) {
                        // Đăng ký thành công
                        registerSuccess();
                    } else {
                        // Đăng ký thất bại
                        Toast.makeText(RegisterActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        }
    }

    private void registerSuccess() {
        // Hiển thị thông báo
        Toast.makeText(RegisterActivity.this, R.string.registration_successful, Toast.LENGTH_SHORT).show();

        // Chuyển đến màn hình đăng nhập
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
        tvLogin.setEnabled(!show);
        tilFullName.setEnabled(!show);
        tilEmail.setEnabled(!show);
        tilUsername.setEnabled(!show);
        tilPassword.setEnabled(!show);
        tilConfirmPassword.setEnabled(!show);
    }
}