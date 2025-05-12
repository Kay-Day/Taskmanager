package com.example.taskmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taskmanager.R;
import com.example.taskmanager.database.UserDAO;
import com.example.taskmanager.models.User;
import com.example.taskmanager.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;

    private UserDAO userDAO;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo các thành phần giao diện
        initViews();

        // Khởi tạo database và session
        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);

        // Thiết lập sự kiện click
        setupClickListeners();
    }

    private void initViews() {
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        // Sự kiện khi nhấn nút Đăng nhập
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Sự kiện khi nhấn vào "Đăng ký ngay"
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        // Reset lỗi
        tilUsername.setError(null);
        tilPassword.setError(null);

        // Lấy giá trị từ trường nhập liệu
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Kiểm tra trường trống
        boolean cancel = false;
        View focusView = null;

        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.fill_all_fields));
            focusView = etPassword;
            cancel = true;
        }

        if (username.isEmpty()) {
            tilUsername.setError(getString(R.string.fill_all_fields));
            focusView = etUsername;
            cancel = true;
        }

        if (cancel) {
            // Có lỗi, tập trung vào trường lỗi đầu tiên
            focusView.requestFocus();
        } else {
            // Hiển thị progress bar và thực hiện đăng nhập
            showProgress(true);

            // Tạo thread mới để xử lý thao tác database
            new Thread(() -> {
                // Mở kết nối đến database
                userDAO.open();

                // Kiểm tra thông tin đăng nhập
                final User user = userDAO.checkLogin(username, password);

                // Đóng kết nối database
                userDAO.close();

                // Chuyển về main thread để cập nhật UI
                runOnUiThread(() -> {
                    showProgress(false);

                    if (user != null) {
                        if (user.isLocked()) {
                            // Tài khoản bị khóa
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            String lockedUntil = sdf.format(new Date(Long.parseLong(user.getLockedUntil())));

                            tilUsername.setError(getString(R.string.account_locked, lockedUntil));
                            etUsername.requestFocus();
                        } else {
                            // Đăng nhập thành công
                            loginSuccess(user);
                        }
                    } else {
                        // Đăng nhập thất bại
                        tilUsername.setError(getString(R.string.invalid_credentials));
                        tilPassword.setError(getString(R.string.invalid_credentials));
                        etUsername.requestFocus();
                    }
                });
            }).start();
        }
    }

    private void loginSuccess(User user) {
        // Lưu thông tin đăng nhập
        sessionManager.createLoginSession(user);

        // Hiển thị thông báo
        Toast.makeText(LoginActivity.this, R.string.login_successful, Toast.LENGTH_SHORT).show();

        // Chuyển đến màn hình chính
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        tvRegister.setEnabled(!show);
        tilUsername.setEnabled(!show);
        tilPassword.setEnabled(!show);
    }
}