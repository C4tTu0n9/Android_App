package com.example.appcore.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class QRCodeGenerator {
    private static final String TAG = "QRCodeGenerator";
    private static final int QR_CODE_SIZE = 512;

    /**
     * Tạo QR code cho vé xem phim
     * @param movieName Tên phim
     * @param showDate Ngày chiếu
     * @param showTime Giờ chiếu
     * @param roomName Tên phòng chiếu
     * @param selectedSeats Danh sách ghế đã chọn
     * @param invoiceId ID hóa đơn
     * @param userId ID người dùng
     * @return Bitmap của QR code
     */
    public static Bitmap generateMovieTicketQR(String movieName, String showDate, 
                                               String showTime, String roomName, 
                                               List<String> selectedSeats, String invoiceId, 
                                               String userId) {
        try {
            // Tạo JSON object chứa thông tin vé
            JSONObject ticketInfo = new JSONObject();
            ticketInfo.put("type", "MOVIE_TICKET");
            ticketInfo.put("movieName", movieName);
            ticketInfo.put("showDate", showDate);
            ticketInfo.put("showTime", showTime);
            ticketInfo.put("roomName", roomName);
            ticketInfo.put("seats", selectedSeats.toString());
            ticketInfo.put("invoiceId", invoiceId);
            ticketInfo.put("userId", userId);
            ticketInfo.put("timestamp", System.currentTimeMillis());
            
            // Tạo checksum để bảo mật
            String checksum = generateChecksum(ticketInfo.toString());
            ticketInfo.put("checksum", checksum);

            String qrContent = ticketInfo.toString();
            Log.d(TAG, "Generating QR code with content: " + qrContent);

            return generateQRCodeBitmap(qrContent);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating QR code JSON", e);
            return null;
        }
    }

    /**
     * Tạo QR code đơn giản với text
     * @param content Nội dung QR code
     * @return Bitmap của QR code
     */
    public static Bitmap generateSimpleQR(String content) {
        return generateQRCodeBitmap(content);
    }

    /**
     * Tạo bitmap QR code từ string content
     * @param content Nội dung QR code
     * @return Bitmap của QR code
     */
    private static Bitmap generateQRCodeBitmap(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            
            // Cấu hình QR code
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 
                                              QR_CODE_SIZE, QR_CODE_SIZE, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            // Tạo bitmap từ bit matrix
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;

        } catch (WriterException e) {
            Log.e(TAG, "Error generating QR code bitmap", e);
            return null;
        }
    }

    /**
     * Tạo checksum đơn giản để bảo mật QR code
     * @param content Nội dung cần tạo checksum
     * @return Checksum string
     */
    private static String generateChecksum(String content) {
        try {
            int hash = content.hashCode();
            return String.valueOf(Math.abs(hash));
        } catch (Exception e) {
            Log.e(TAG, "Error generating checksum", e);
            return "0";
        }
    }

    /**
     * Xác thực QR code vé xem phim
     * @param qrContent Nội dung QR code
     * @return true nếu QR code hợp lệ
     */
    public static boolean validateMovieTicketQR(String qrContent) {
        try {
            JSONObject ticketInfo = new JSONObject(qrContent);
            
            // Kiểm tra type
            if (!"MOVIE_TICKET".equals(ticketInfo.optString("type"))) {
                return false;
            }

            // Kiểm tra các field bắt buộc
            String[] requiredFields = {"movieName", "showDate", "showTime", 
                                     "roomName", "seats", "invoiceId", "userId", "checksum"};
            
            for (String field : requiredFields) {
                if (!ticketInfo.has(field) || ticketInfo.optString(field).isEmpty()) {
                    Log.w(TAG, "Missing required field: " + field);
                    return false;
                }
            }

            // Xác thực checksum
            String originalChecksum = ticketInfo.optString("checksum");
            ticketInfo.remove("checksum");
            String calculatedChecksum = generateChecksum(ticketInfo.toString());
            
            if (!originalChecksum.equals(calculatedChecksum)) {
                Log.w(TAG, "Checksum validation failed");
                return false;
            }

            // Kiểm tra thời gian (QR code có hiệu lực trong 24 giờ)
            long timestamp = ticketInfo.optLong("timestamp", 0);
            long currentTime = System.currentTimeMillis();
            long validityPeriod = 24 * 60 * 60 * 1000; // 24 giờ

            if (currentTime - timestamp > validityPeriod) {
                Log.w(TAG, "QR code expired");
                return false;
            }

            return true;

        } catch (JSONException e) {
            Log.e(TAG, "Error validating QR code", e);
            return false;
        }
    }

    /**
     * Lấy thông tin từ QR code vé xem phim
     * @param qrContent Nội dung QR code
     * @return JSONObject chứa thông tin vé, null nếu không hợp lệ
     */
    public static JSONObject getTicketInfoFromQR(String qrContent) {
        try {
            if (validateMovieTicketQR(qrContent)) {
                return new JSONObject(qrContent);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing QR code content", e);
        }
        return null;
    }

    /**
     * Format thông tin vé để hiển thị
     * @param ticketInfo JSONObject chứa thông tin vé
     * @return String formatted ticket info
     */
    public static String formatTicketInfo(JSONObject ticketInfo) {
        if (ticketInfo == null) return "Thông tin vé không hợp lệ";

        StringBuilder sb = new StringBuilder();
        sb.append("🎬 ").append(ticketInfo.optString("movieName", "N/A")).append("\n");
        sb.append("📅 ").append(ticketInfo.optString("showDate", "N/A")).append("\n");
        sb.append("🕐 ").append(ticketInfo.optString("showTime", "N/A")).append("\n");
        sb.append("🏢 ").append(ticketInfo.optString("roomName", "N/A")).append("\n");
        sb.append("💺 ").append(ticketInfo.optString("seats", "N/A")).append("\n");
        sb.append("🎫 ").append(ticketInfo.optString("invoiceId", "N/A"));

        return sb.toString();
    }
}

