package com.example.taskmanager.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.activities.CalendarEventActivity;
import com.example.taskmanager.adapters.CalendarAdapter;
import com.example.taskmanager.adapters.CalendarEventAdapter;
import com.example.taskmanager.database.CalendarEventDAO;
import com.example.taskmanager.models.CalendarEvent;
import com.example.taskmanager.utils.DateTimeUtils;
import com.example.taskmanager.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.Intent;

public class CalendarFragment extends Fragment implements CalendarAdapter.OnDateClickListener, CalendarEventAdapter.OnEventClickListener {

    // Interface để giao tiếp với Activity
    public interface CalendarActionsListener {
        void onAddEvent(String selectedDate);
    }

    private TextView tvCurrentMonth, tvSelectedDate, tvEmptyEvents;
    private GridView gridCalendar;
    private RecyclerView recyclerViewEvents;
    private ImageButton btnPrevMonth, btnNextMonth;

    private CalendarAdapter calendarAdapter;
    private CalendarEventAdapter eventAdapter;
    private List<String> datesList;
    private List<CalendarEvent> eventList;

    private Calendar currentCalendar;
    private String selectedDate; // Format: yyyy-MM-dd

    private SessionManager sessionManager;
    private CalendarActionsListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Kiểm tra xem Activity có triển khai interface không
        if (context instanceof CalendarActionsListener) {
            listener = (CalendarActionsListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Khởi tạo các thành phần giao diện
        initViews(view);

        // Khởi tạo session
        sessionManager = new SessionManager(getContext());

        // Khởi tạo calendar và ngày hiện tại
        setupCalendar();

        // Thiết lập adapter cho lịch
        setupCalendarAdapter();

        // Thiết lập adapter cho danh sách sự kiện
        setupEventAdapter();

        // Thiết lập sự kiện click
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        tvCurrentMonth = view.findViewById(R.id.tv_current_month);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        tvEmptyEvents = view.findViewById(R.id.tv_empty_events);
        gridCalendar = view.findViewById(R.id.grid_calendar);
        recyclerViewEvents = view.findViewById(R.id.recycler_view_events);
        btnPrevMonth = view.findViewById(R.id.btn_prev_month);
        btnNextMonth = view.findViewById(R.id.btn_next_month);
    }

    private void setupCalendar() {
        currentCalendar = Calendar.getInstance();
        datesList = new ArrayList<>();

        // Thiết lập ngày được chọn là ngày hiện tại
        selectedDate = DateTimeUtils.getCurrentDate();

        // Tạo danh sách ngày trong tháng hiện tại
        generateDatesForMonth(currentCalendar);

        // Cập nhật tiêu đề tháng năm
        updateMonthYearTitle();

        // Cập nhật hiển thị ngày đã chọn
        updateSelectedDateView();
    }

    private void setupCalendarAdapter() {
        calendarAdapter = new CalendarAdapter(getContext(), datesList, selectedDate, this);
        gridCalendar.setAdapter(calendarAdapter);
    }

    private void setupEventAdapter() {
        eventList = new ArrayList<>();
        eventAdapter = new CalendarEventAdapter(getContext(), eventList, this);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewEvents.setAdapter(eventAdapter);

        // Tải sự kiện của ngày được chọn
        loadSelectedDateEvents();
    }

    private void setupClickListeners() {
        // Sự kiện khi nhấn nút Tháng trước
        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendarView();
        });

        // Sự kiện khi nhấn nút Tháng sau
        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendarView();
        });

        // Sự kiện khi nhấn vào ô ngày trong lịch
        gridCalendar.setOnItemClickListener((parent, view, position, id) -> {
            String date = calendarAdapter.getItem(position);
            if (!date.isEmpty()) {
                // Lấy ngày đầy đủ dựa trên vị trí được chọn
                Calendar cal = (Calendar) currentCalendar.clone();
                cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date));
                selectedDate = DateTimeUtils.formatDate(cal.getTime());

                // Cập nhật adapter với ngày mới được chọn
                calendarAdapter.setSelectedDate(selectedDate);
                calendarAdapter.notifyDataSetChanged();

                // Cập nhật hiển thị ngày đã chọn
                updateSelectedDateView();

                // Tải sự kiện của ngày được chọn
                loadSelectedDateEvents();
            }
        });
    }

    public void onAddEventClicked() {
        // Phương thức này được gọi từ Activity khi FAB được nhấn
        if (getActivity() != null && isAdded()) {
            Intent intent = new Intent(getActivity(), CalendarEventActivity.class);
            intent.putExtra("SELECTED_DATE", selectedDate);
            startActivity(intent);
        }
    }

    private void updateCalendarView() {
        // Tạo lại danh sách ngày cho tháng mới
        generateDatesForMonth(currentCalendar);

        // Cập nhật tiêu đề tháng năm
        updateMonthYearTitle();

        // Thông báo adapter về dữ liệu mới
        calendarAdapter.setDates(datesList);
        calendarAdapter.notifyDataSetChanged();
    }

    private void generateDatesForMonth(Calendar calendar) {
        datesList.clear();

        // Lấy ngày đầu tiên của tháng
        Calendar firstDayOfMonth = (Calendar) calendar.clone();
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);

        // Lấy vị trí của ngày đầu tiên trong tuần (0 = Chủ nhật, 1 = Thứ 2, ...)
        int firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1;

        // Thêm các ngày trống vào đầu danh sách
        for (int i = 0; i < firstDayOfWeek; i++) {
            datesList.add("");
        }

        // Lấy số ngày trong tháng
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Thêm các ngày trong tháng vào danh sách
        for (int i = 1; i <= daysInMonth; i++) {
            datesList.add(String.valueOf(i));
        }

        // Tải sự kiện của tháng
        loadMonthEvents();
    }

    private void updateMonthYearTitle() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvCurrentMonth.setText(sdf.format(currentCalendar.getTime()));
    }

    private void updateSelectedDateView() {
        String displayDate = DateTimeUtils.formatDisplayDate(selectedDate);
        String dayOfWeek = DateTimeUtils.getDayOfWeekText(selectedDate);
        tvSelectedDate.setText(dayOfWeek + ", " + displayDate);
    }

    private void loadMonthEvents() {
        if (!isAdded() || getContext() == null) {
            return; // Không làm gì nếu Fragment không còn gắn kết
        }

        // Lấy ngày đầu tiên của tháng
        String firstDay = DateTimeUtils.getFirstDayOfMonth(DateTimeUtils.formatDate(currentCalendar.getTime()));
        // Lấy ngày cuối cùng của tháng
        String lastDay = DateTimeUtils.getLastDayOfMonth(DateTimeUtils.formatDate(currentCalendar.getTime()));

        CalendarEventDAO eventDAO = null;
        try {
            eventDAO = new CalendarEventDAO(getContext());
            eventDAO.open();

            // Lấy tất cả sự kiện trong tháng
            final List<CalendarEvent> monthEvents = eventDAO.getEventsByDateRange(sessionManager.getUserId(), firstDay, lastDay);

            // Cập nhật adapter với sự kiện mới
            if (getActivity() != null && isAdded()) {
                getActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        calendarAdapter.setEvents(monthEvents);
                        calendarAdapter.notifyDataSetChanged();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (eventDAO != null) {
                eventDAO.close();
            }
        }
    }

    private void loadSelectedDateEvents() {
        if (!isAdded() || getContext() == null) {
            return; // Không làm gì nếu Fragment không còn gắn kết
        }

        CalendarEventDAO eventDAO = null;
        try {
            eventDAO = new CalendarEventDAO(getContext());
            eventDAO.open();

            final List<CalendarEvent> events = eventDAO.getEventsByDate(
                    sessionManager.getUserId(), selectedDate);

            if (getActivity() != null && isAdded()) {
                getActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        eventList.clear();
                        eventList.addAll(events);
                        eventAdapter.notifyDataSetChanged();

                        // Cập nhật UI khi không có sự kiện
                        if (eventList.isEmpty()) {
                            tvEmptyEvents.setVisibility(View.VISIBLE);
                            recyclerViewEvents.setVisibility(View.GONE);
                        } else {
                            tvEmptyEvents.setVisibility(View.GONE);
                            recyclerViewEvents.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (getActivity() != null && isAdded()) {
                getActivity().runOnUiThread(() -> {
                    if (getContext() != null && isAdded()) {
                        Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } finally {
            // Đảm bảo luôn đóng kết nối, ngay cả khi có lỗi
            if (eventDAO != null) {
                eventDAO.close();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại sự kiện khi quay lại fragment
        if (isAdded() && getContext() != null) {
            loadSelectedDateEvents();
            // Tải lại sự kiện của tháng
            loadMonthEvents();
        }
    }

    @Override
    public void onDateClick(String date) {
        // Sự kiện khi click vào ngày trong lịch
        // Đã được xử lý trong setupClickListeners()
    }

    @Override
    public void onEventClick(CalendarEvent event) {
        // Kiểm tra Context không null trước khi tạo Intent
        if (getActivity() != null && isAdded()) {
            // Mở màn hình chi tiết sự kiện
            Intent intent = new Intent(getActivity(), CalendarEventActivity.class);
            intent.putExtra("EVENT_ID", event.getId());
            startActivity(intent);
        }
    }

    public String getSelectedDate() {
        return selectedDate;
    }
}