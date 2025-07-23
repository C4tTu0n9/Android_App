package com.example.appcore.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    /**
     * Chuyển đổi timestamp (long) sang chuỗi ngày tháng theo định dạng mong muốn.
     * @param timestamp The timestamp in milliseconds.
     * @param formatPattern The desired date format (e.g., "dd/MM/yyyy").
     * @return A formatted date string.
     */
    public static String formatTimestamp(long timestamp, String formatPattern) {
        if (timestamp <= 0) {
            return ""; // Hoặc trả về "N/A", "Chưa xác định", ...
        }
        try {
            Date date = new Date(timestamp);
            SimpleDateFormat formatter = new SimpleDateFormat(formatPattern, Locale.getDefault());
            return formatter.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return ""; // Trả về chuỗi rỗng nếu có lỗi
        }
    }

    /**
     * Hàm tiện lợi với định dạng mặc định là dd/MM/yyyy.
     * @param timestamp The timestamp in milliseconds.
     * @return A formatted date string as "dd/MM/yyyy".
     */
    public static String formatTimestamp(long timestamp) {
        return formatTimestamp(timestamp, "dd/MM/yyyy");
    }
}
