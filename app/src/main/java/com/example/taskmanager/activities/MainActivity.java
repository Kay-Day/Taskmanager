package com.example.taskmanager.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.taskmanager.R;
import com.example.taskmanager.fragments.CalendarFragment;
import com.example.taskmanager.fragments.NotificationsFragment;
import com.example.taskmanager.fragments.ProjectsFragment;
import com.example.taskmanager.fragments.StatisticsFragment;
import com.example.taskmanager.services.NotificationService;
import com.example.taskmanager.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements CalendarFragment.CalendarActionsListener {

    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAdd;

    private ProjectsFragment projectsFragment;
    private CalendarFragment calendarFragment;
    private NotificationsFragment notificationsFragment;
    private StatisticsFragment statisticsFragment;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo các thành phần giao diện
        initViews();

        // Thiết lập toolbar
        setSupportActionBar(toolbar);

        // Khởi tạo session
        sessionManager = new SessionManager(this);

        // Khởi tạo các fragment
        initFragments();

        startNotificationService();

        // Thiết lập sự kiện cho bottom navigation
        setupBottomNavigation();

        // Thiết lập sự kiện cho FAB
        setupFab();

        // Mặc định hiển thị fragment Projects
        loadFragment(projectsFragment);

        // Khởi động dịch vụ thông báo
        startNotificationService();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fabAdd = findViewById(R.id.fab_add);
    }

    private void initFragments() {
        projectsFragment = new ProjectsFragment();
        calendarFragment = new CalendarFragment();
        notificationsFragment = new NotificationsFragment();
        statisticsFragment = new StatisticsFragment();
    }

    private void startNotificationService() {
        if (sessionManager.isLoggedIn()) {
            Intent serviceIntent = new Intent(this, NotificationService.class);

            // Khởi động dịch vụ
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }

            Log.d("MainActivity", "Notification service started");
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_projects) {
                fragment = projectsFragment;
                toolbar.setTitle(R.string.projects);
                fabAdd.setVisibility(View.VISIBLE);
            } else if (itemId == R.id.nav_calendar) {
                fragment = calendarFragment;
                toolbar.setTitle(R.string.calendar);
                fabAdd.setVisibility(View.VISIBLE);
            } else if (itemId == R.id.nav_notifications) {
                fragment = notificationsFragment;
                toolbar.setTitle(R.string.notifications);
                fabAdd.setVisibility(View.GONE);
            } else if (itemId == R.id.nav_statistics) {
                fragment = statisticsFragment;
                toolbar.setTitle(R.string.statistics);
                fabAdd.setVisibility(View.GONE);
            }

            return loadFragment(fragment);
        });
    }

    private void setupFab() {
        fabAdd.setOnClickListener(v -> {
            // Xác định đang ở fragment nào để mở activity tương ứng
            int currentItemId = bottomNavigationView.getSelectedItemId();

            if (currentItemId == R.id.nav_projects) {
                // Tạo dự án mới
                Intent intent = new Intent(MainActivity.this, ProjectActivity.class);
                startActivity(intent);
            } else if (currentItemId == R.id.nav_calendar) {
                // Lấy fragment hiện tại
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof CalendarFragment) {
                    // Gọi phương thức trực tiếp trên fragment
                    ((CalendarFragment) currentFragment).onAddEventClicked();
                }
            }
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

//    private void startNotificationService() {
//        Intent serviceIntent = new Intent(this, NotificationService.class);
//        startService(serviceIntent);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            showLogoutConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.confirm_logout)
                .setPositiveButton(R.string.yes, (dialog, which) -> logout())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void logout() {
        // Đăng xuất
        sessionManager.logout();

        // Dừng dịch vụ thông báo
        stopService(new Intent(this, NotificationService.class));

        // Chuyển đến màn hình đăng nhập
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Được gọi từ interface CalendarActionsListener
    @Override
    public void onAddEvent(String selectedDate) {
        // Chuyển hướng sang màn hình thêm sự kiện
        Intent intent = new Intent(this, CalendarEventActivity.class);
        intent.putExtra("SELECTED_DATE", selectedDate);
        startActivity(intent);
    }
}