package com.example.appcore.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OpenAIHelper {
    private static final String TAG = "OpenAIHelper";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-3.5-turbo";
    
    private ExecutorService executorService;
    private String apiKey;

    public interface ChatResponseCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public OpenAIHelper() {
        executorService = Executors.newSingleThreadExecutor();
        // API key sẽ được lấy từ environment variable
        apiKey = System.getenv("OPENAI_API_KEY");
        
        if (apiKey == null || apiKey.isEmpty()) {
            Log.w(TAG, "OPENAI_API_KEY not found in environment variables");
        }
    }

    /**
     * Gửi tin nhắn đến OpenAI và nhận phản hồi
     * @param userMessage Tin nhắn từ người dùng
     * @param callback Callback để trả về kết quả
     */
    public void getChatResponse(String userMessage, ChatResponseCallback callback) {
        if (apiKey == null || apiKey.isEmpty()) {
            callback.onError("API key không được cấu hình");
            return;
        }

        executorService.execute(() -> {
            try {
                String response = sendChatRequest(userMessage);
                callback.onSuccess(response);
            } catch (Exception e) {
                Log.e(TAG, "Error getting chat response", e);
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Gửi request đến OpenAI API
     * @param userMessage Tin nhắn từ người dùng
     * @return Phản hồi từ AI
     * @throws IOException, JSONException
     */
    private String sendChatRequest(String userMessage) throws IOException, JSONException {
        URL url = new URL(OPENAI_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            // Cấu hình connection
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000); // 30 seconds
            connection.setReadTimeout(30000); // 30 seconds

            // Tạo request body
            JSONObject requestBody = createRequestBody(userMessage);
            
            // Gửi request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Đọc response
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "Response code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return parseSuccessResponse(connection);
            } else {
                return parseErrorResponse(connection, responseCode);
            }

        } finally {
            connection.disconnect();
        }
    }

    /**
     * Tạo request body cho OpenAI API
     * @param userMessage Tin nhắn từ người dùng
     * @return JSONObject request body
     * @throws JSONException
     */
    private JSONObject createRequestBody(String userMessage) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);

        // Tạo system message để định hướng AI
        JSONArray messages = new JSONArray();
        
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", 
            "Bạn là trợ lý AI thông minh của ứng dụng đặt vé xem phim. " +
            "Hãy trả lời một cách thân thiện, hữu ích và chuyên nghiệp. " +
            "Tập trung vào các chủ đề liên quan đến phim, đặt vé, rạp chiếu, giá vé. " +
            "Nếu được hỏi về những chủ đề không liên quan, hãy lịch sự chuyển hướng về dịch vụ phim. " +
            "Trả lời bằng tiếng Việt."
        );
        messages.put(systemMessage);

        JSONObject userMessageObj = new JSONObject();
        userMessageObj.put("role", "user");
        userMessageObj.put("content", userMessage);
        messages.put(userMessageObj);

        requestBody.put("messages", messages);
        
        return requestBody;
    }

    /**
     * Parse response thành công từ OpenAI
     * @param connection HttpURLConnection
     * @return Nội dung phản hồi
     * @throws IOException, JSONException
     */
    private String parseSuccessResponse(HttpURLConnection connection) throws IOException, JSONException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray choices = jsonResponse.getJSONArray("choices");
        
        if (choices.length() > 0) {
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            return message.getString("content").trim();
        } else {
            throw new JSONException("No choices in response");
        }
    }

    /**
     * Parse error response từ OpenAI
     * @param connection HttpURLConnection
     * @param responseCode Response code
     * @return Error message
     * @throws IOException
     */
    private String parseErrorResponse(HttpURLConnection connection, int responseCode) throws IOException {
        StringBuilder errorResponse = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                errorResponse.append(responseLine.trim());
            }
        }

        Log.e(TAG, "API Error " + responseCode + ": " + errorResponse.toString());
        
        // Trả về thông báo lỗi thân thiện
        switch (responseCode) {
            case 401:
                return "Lỗi xác thực API. Vui lòng liên hệ quản trị viên.";
            case 429:
                return "Quá nhiều yêu cầu. Vui lòng thử lại sau ít phút.";
            case 500:
            case 502:
            case 503:
                return "Dịch vụ AI tạm thời không khả dụng. Vui lòng thử lại sau.";
            default:
                return "Có lỗi xảy ra khi kết nối với dịch vụ AI. Vui lòng thử lại.";
        }
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}

