package com.example.taskmanager.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    // Định dạng ngày tháng
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DISPLAY_DATE_FORMAT = "dd/MM/yyyy";
    private static final String DISPLAY_TIME_FORMAT = "HH:mm";
    private static final String DISPLAY_DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";

    // Chuyển đổi chuỗi ngày dạng yyyy-MM-dd sang Date
    public static Date parseDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Chuyển đổi chuỗi thời gian dạng HH:mm sang Date
    public static Date parseTime(String timeString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
            return sdf.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Chuyển đổi chuỗi ngày giờ dạng yyyy-MM-dd HH:mm:ss sang Date
    public static Date parseDateTime(String dateTimeString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
            return sdf.parse(dateTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Chuyển đổi Date sang chuỗi ngày dạng yyyy-MM-dd
    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }

    // Chuyển đổi Date sang chuỗi thời gian dạng HH:mm
    public static String formatTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }

    // Chuyển đổi Date sang chuỗi ngày giờ dạng yyyy-MM-dd HH:mm:ss
    public static String formatDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }

    // Chuyển đổi chuỗi ngày dạng yyyy-MM-dd sang chuỗi ngày dạng dd/MM/yyyy
    public static String formatDisplayDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString;
        }
    }

    // Chuyển đổi chuỗi ngày dạng dd/MM/yyyy sang chuỗi ngày dạng yyyy-MM-dd
    public static String parseDisplayDate(String displayDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            Date date = inputFormat.parse(displayDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return displayDate;
        }
    }

    // Lấy ngày hiện tại dạng yyyy-MM-dd
    public static String getCurrentDate() {
        return formatDate(new Date());
    }

    // Lấy thời gian hiện tại dạng HH:mm
    public static String getCurrentTime() {
        return formatTime(new Date());
    }

    // Lấy ngày giờ hiện tại dạng yyyy-MM-dd HH:mm:ss
    public static String getCurrentDateTime() {
        return formatDateTime(new Date());
    }

    // Tính số ngày giữa hai ngày
    public static int daysBetween(String startDateStr, String endDateStr) {
        Date startDate = parseDate(startDateStr);
        Date endDate = parseDate(endDateStr);

        if (startDate == null || endDate == null) {
            return 0;
        }

        long difference = endDate.getTime() - startDate.getTime();
        return (int) (difference / (24 * 60 * 60 * 1000));
    }

    // Tính xem một ngày là thứ mấy trong tuần (1 = Chủ nhật, 2 = Thứ 2, ...)
    public static int getDayOfWeek(String dateStr) {
        Date date = parseDate(dateStr);

        if (date == null) {
            return 0;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    // Định dạng thứ trong tuần
    public static String getDayOfWeekText(String dateStr) {
        int dayOfWeek = getDayOfWeek(dateStr);

        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return "Chủ nhật";
            case Calendar.MONDAY:
                return "Thứ hai";
            case Calendar.TUESDAY:
                return "Thứ ba";
            case Calendar.WEDNESDAY:
                return "Thứ tư";
            case Calendar.THURSDAY:
                return "Thứ năm";
            case Calendar.FRIDAY:
                return "Thứ sáu";
            case Calendar.SATURDAY:
                return "Thứ bảy";
            default:
                return "";
        }
    }

    // Kiểm tra xem một ngày có phải là ngày hiện tại không
    public static boolean isToday(String dateStr) {
        return dateStr.equals(getCurrentDate());
    }

    // Lấy ngày trước đó n ngày
    public static String getDateBefore(String dateStr, int days) {
        Date date = parseDate(dateStr);

        if (date == null) {
            return dateStr;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -days);

        return formatDate(calendar.getTime());
    }

    // Lấy ngày sau đó n ngày
    public static String getDateAfter(String dateStr, int days) {
        Date date = parseDate(dateStr);

        if (date == null) {
            return dateStr;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);

        return formatDate(calendar.getTime());
    }

    // Lấy ngày đầu tiên của tháng
    public static String getFirstDayOfMonth(String dateStr) {
        Date date = parseDate(dateStr);

        if (date == null) {
            return dateStr;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        return formatDate(calendar.getTime());
    }

    // Lấy ngày cuối cùng của tháng
    public static String getLastDayOfMonth(String dateStr) {
        Date date = parseDate(dateStr);

        if (date == null) {
            return dateStr;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        return formatDate(calendar.getTime());
    }

    // Chuyển đổi thành định dạng để hiển thị tương đối (hôm nay, hôm qua, ngày mai)
    public static String getRelativeDateDisplay(String dateStr) {
        if (isToday(dateStr)) {
            return "Hôm nay";
        }

        String yesterday = getDateBefore(getCurrentDate(), 1);
        if (dateStr.equals(yesterday)) {
            return "Hôm qua";
        }

        String tomorrow = getDateAfter(getCurrentDate(), 1);
        if (dateStr.equals(tomorrow)) {
            return "Ngày mai";
        }

        return formatDisplayDate(dateStr);
    }
}