//package com.example.taskmanager.fragments;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.GridView;
//import android.widget.ImageButton;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.taskmanager.R;
//import com.example.taskmanager.activities.CalendarEventActivity;
//import com.example.taskmanager.adapters.CalendarAdapter;
//import com.example.taskmanager.adapters.CalendarEventAdapter;
//import com.example.taskmanager.database.CalendarEventDAO;
//import com.example.taskmanager.models.CalendarEvent;
//import com.example.taskmanager.utils.DateTimeUtils;
//import com.example.taskmanager.utils.SessionManager;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//import java.util.Locale;
//
//public class CalendarFragment extends Fragment implements CalendarAdapter.OnDateClickListener, CalendarEventAdapter.OnEventClickListener {
//
//    private TextView tvCurrentMonth, tvSelectedDate;
//    private GridView gridCalendar;
//    private RecyclerView recyclerViewEvents;
//    private TextView tvEmptyEvents;
//    private ImageButton btnPrevMonth, btnNextMonth;
//
//    private Calendar currentDate;
//    private CalendarAdapter calendarAdapter;
//    private CalendarEventAdapter eventAdapter;
//    private CalendarEventDAO eventDAO;
//    private SessionManager sessionManager;
//
//    private List<CalendarEvent> eventList;
//    private String selectedDate;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
//
//        // Khởi tạo các thành phần giao diện
//        initViews(view);
//
//        // Khởi tạo database và session
//        eventDAO = new CalendarEventDAO(getContext());
//        sessionManager = new SessionManager(getContext());
//
//        // Khởi tạo calendar
//        currentDate = Calendar.getInstance();
//        selectedDate = DateTimeUtils.formatDate(currentDate.getTime());
//
//        // Thiết lập recycler view cho danh sách sự kiện
//        setupRecyclerView();
//
//        // Thiết lập calendar grid
//        setupCalendarGrid();
//
//        // Thiết lập các sự kiện click
//        setupClickListeners();
//
//        // Hiển thị tháng hiện tại
//        updateCalendarUI();
//
//        return view;
//    }
//
//    private void initViews(View view) {
//        tvCurrentMonth = view.findViewById(R.id.tv_current_month);
//        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
//        gridCalendar = view.findViewById(R.id.grid_calendar);
//        recyclerViewEvents = view.findViewById(R.id.recycler_view_events);
//        tvEmptyEvents = view.findViewById(R.id.tv_empty_events);
//        btnPrevMonth = view.findViewById(R.id.btn_prev_month);
//        btnNextMonth = view.findViewById(R.id.btn_next_month);
//    }
//
//    private void setupRecyclerView() {
//        eventList = new ArrayList<>();
//        eventAdapter = new CalendarEventAdapter(getContext(), eventList, this);
//        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerViewEvents.setAdapter(eventAdapter);
//    }
//
//    private void setupCalendarGrid() {
//        List<String> dates = getCalendarDates();
//        calendarAdapter = new CalendarAdapter(getContext(), dates, selectedDate, this);
//        gridCalendar.setAdapter(calendarAdapter);
//
//        // Load các sự kiện của tháng hiện tại
//        loadMonthEvents();
//    }
//
//    private void setupClickListeners() {
//        btnPrevMonth.setOnClickListener(v -> {
//            // Chuyển sang tháng trước
//            currentDate.add(Calendar.MONTH, -1);
//            updateCalendarUI();
//        });
//
//        btnNextMonth.setOnClickListener(v -> {
//            // Chuyển sang tháng sau
//            currentDate.add(Calendar.MONTH, 1);
//            updateCalendarUI();
//        });
//
//        gridCalendar.setOnItemClickListener((parent, view, position, id) -> {
//            // Lấy ngày được chọn
//            String date = calendarAdapter.getItem(position);
//            if (!date.isEmpty()) {
//                // Cập nhật ngày được chọn
//                selectedDate = getFullDateString(date);
//                calendarAdapter.setSelectedDate(selectedDate);
//                calendarAdapter.notifyDataSetChanged();
//
//                // Cập nhật tiêu đề và tải sự kiện
//                updateSelectedDateUI();
//                loadSelectedDateEvents();
//            }
//        });
//    }
//
//    private void updateCalendarUI() {
//        // Cập nhật tiêu đề tháng
//        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
//        tvCurrentMonth.setText(monthFormat.format(currentDate.getTime()));
//
//        // Cập nhật lưới lịch
//        calendarAdapter.setDates(getCalendarDates());
//        calendarAdapter.notifyDataSetChanged();
//
//        // Cập nhật UI cho ngày đã chọn
//        updateSelectedDateUI();
//
//        // Tải lại sự kiện của tháng
//        loadMonthEvents();
//    }
//
//    private void updateSelectedDateUI() {
//        // Cập nhật tiêu đề ngày đã chọn
//        String displayDate = DateTimeUtils.getRelativeDateDisplay(selectedDate);
//        tvSelectedDate.setText(getString(R.string.events) + " " + displayDate);
//    }
//
//    private List<String> getCalendarDates() {
//        List<String> dates = new ArrayList<>();
//
//        // Lưu lại ngày đầu tiên của tháng
//        Calendar calendar = (Calendar) currentDate.clone();
//        calendar.set(Calendar.DAY_OF_MONTH, 1);
//
//        // Xác định ngày đầu tiên của tuần đầu tiên
//        int firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK) - 1;
//        calendar.add(Calendar.DAY_OF_MONTH, -firstDayOfMonth);
//
//        // Tạo danh sách 42 ngày (6 tuần)
//        for (int i = 0; i < 42; i++) {
//            int day = calendar.get(Calendar.DAY_OF_MONTH);
//            int month = calendar.get(Calendar.MONTH);
//
//            // Nếu tháng khác với tháng hiện tại, hiển thị ngày là rỗng
//            if (month != currentDate.get(Calendar.MONTH)) {
//                dates.add("");
//            } else {
//                dates.add(String.valueOf(day));
//            }
//
//            calendar.add(Calendar.DAY_OF_MONTH, 1);
//        }
//
//        return dates;
//    }
//
//    private String getFullDateString(String day) {
//        if (day.isEmpty()) {
//            return selectedDate;
//        }
//
//        Calendar calendar = (Calendar) currentDate.clone();
//        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
//        return DateTimeUtils.formatDate(calendar.getTime());
//    }
//
//    private void loadMonthEvents() {
//        // Lấy ngày đầu và cuối của tháng
//        Calendar calendar = (Calendar) currentDate.clone();
//        calendar.set(Calendar.DAY_OF_MONTH, 1);
//        String startDate = DateTimeUtils.formatDate(calendar.getTime());
//
//        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
//        String endDate = DateTimeUtils.formatDate(calendar.getTime());
//
//        // Tải sự kiện từ database
//        new Thread(() -> {
//            eventDAO.open();
//            // Tải tất cả sự kiện của người dùng
//            List<CalendarEvent> events = eventDAO.getEventsByUser(sessionManager.getUserId());
//            eventDAO.close();
//
//            // Lọc sự kiện trong tháng
//            List<CalendarEvent> monthEvents = new ArrayList<>();
//            for (CalendarEvent event : events) {
//                // Kiểm tra xem sự kiện có nằm trong tháng hiện tại không
//                if (event.getEventDate().compareTo(startDate) >= 0 && event.getEventDate().compareTo(endDate) <= 0) {
//                    monthEvents.add(event);
//                }
//            }
//
//            // Cập nhật UI trên main thread
//            if (getActivity() != null) {
//                getActivity().runOnUiThread(() -> {
//                    // Cập nhật hiển thị dấu chấm cho ngày có sự kiện
//                    calendarAdapter.setEvents(monthEvents);
//                    calendarAdapter.notifyDataSetChanged();
//
//                    // Tải sự kiện của ngày đã chọn
//                    loadSelectedDateEvents();
//                });
//            }
//        }).start();
//    }
//
//    private void loadSelectedDateEvents() {
//        new Thread(() -> {
//            eventDAO.open();
//            List<CalendarEvent> events = eventDAO.getEventsByDate(sessionManager.getUserId(), selectedDate);
//            eventDAO.close();
//
//            // Cập nhật UI trên main thread
//            if (getActivity() != null) {
//                getActivity().runOnUiThread(() -> {
//                    eventList.clear();
//                    eventList.addAll(events);
//                    eventAdapter.notifyDataSetChanged();
//
//                    // Hiển thị thông báo nếu không có sự kiện nào
//                    if (eventList.isEmpty()) {
//                        tvEmptyEvents.setVisibility(View.VISIBLE);
//                        recyclerViewEvents.setVisibility(View.GONE);
//                    } else {
//                        tvEmptyEvents.setVisibility(View.GONE);
//                        recyclerViewEvents.setVisibility(View.VISIBLE);
//                    }
//                });
//            }
//        }).start();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        // Tải lại sự kiện khi fragment được resume
//        loadMonthEvents();
//    }
//
//    @Override
//    public void onDateClick(String date) {
//        // Đã xử lý trong OnItemClickListener của gridCalendar
//    }
//
//    @Override
//    public void onEventClick(CalendarEvent event) {
//        // Mở màn hình chỉnh sửa sự kiện
//        Intent intent = new Intent(getActivity(), CalendarEventActivity.class);
//        intent.putExtra("EVENT_ID", event.getId());
//        startActivity(intent);
//    }
//}