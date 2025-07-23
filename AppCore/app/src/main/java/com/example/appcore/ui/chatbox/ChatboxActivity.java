package com.example.appcore.ui.chatbox;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appcore.R;
import com.example.appcore.ui.chatbox.adapter.ChatMessageAdapter;
import com.example.appcore.ui.chatbox.model.ChatMessage;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class ChatboxActivity extends AppCompatActivity {
    private static final String TAG = "ChatboxActivity";
    private static final String API_KEY = "AIzaSyAV2X3DLAU0gkbXOFc60aZtDnDZW22V80k";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;
    
    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ProgressBar progressBarSending;
    private ChipNavigationBar chipNavigationBar;
    private ChatMessageAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private OkHttpClient httpClient;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox);
        
        initViews();
        setupRecyclerView();
        setupHttpClient();
        setupClickListeners();
        setupBottomNavigation();

        mainHandler = new Handler(Looper.getMainLooper());

        // Thêm tin nhắn chào mừng
        addWelcomeMessage();
    }


    private void initViews() {
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        progressBarSending = findViewById(R.id.progressBarSending);
        chipNavigationBar = findViewById(R.id.bottomNavBar);
        // Xử lý nút back
        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatMessageAdapter(chatMessages);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);
    }

    private void setupHttpClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private void setupClickListeners() {
        buttonSend.setOnClickListener(v -> sendMessage());
    }

    private void addWelcomeMessage() {
        ChatMessage welcomeMessage = new ChatMessage(
            "Xin chào! Tôi là AI Assistant. Tôi có thể giúp bạn trả lời các câu hỏi về phim, đặt vé, hoặc bất kỳ thắc mắc nào khác. Bạn muốn hỏi gì?",
            false,
            System.currentTimeMillis()
        );
        chatMessages.add(welcomeMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        scrollToBottom();
    }
    private void setupBottomNavigation() {
        // Đặt chat là item được chọn
        chipNavigationBar.setItemSelected(R.id.chat, true);

        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int id) {
                if (id == R.id.home) {
                    finish(); // Quay lại FragmentContainerViewActivity
                } else if (id == R.id.chat) {
                    // Đã ở chat rồi, không làm gì
//                } else if (id == R.id.cart) {
//                    // TODO: Chuyển đến cart
//                } else if (id == R.id.nav_profile) {
//                    // TODO: Chuyển đến profile
                }
            }
        });
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // Thêm tin nhắn của user
        ChatMessage userMessage = new ChatMessage(messageText, true, System.currentTimeMillis());
        chatMessages.add(userMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        scrollToBottom();

        // Clear input và hiển thị loading
        editTextMessage.setText("");
        showLoading(true);

        // Gửi request đến Gemini API
        sendToGeminiAPI(messageText);
    }

    private void sendToGeminiAPI(String message) {
        try {
            JSONObject requestJson = new JSONObject();
            JSONArray contentsArray = new JSONArray();
            JSONObject contentObject = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject partObject = new JSONObject();
            
            partObject.put("text", message);
            partsArray.put(partObject);
            contentObject.put("parts", partsArray);
            contentsArray.put(contentObject);
            requestJson.put("contents", contentsArray);

            RequestBody requestBody = RequestBody.create(
                requestJson.toString(),
                MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API call failed", e);
                    mainHandler.post(() -> {
                        showLoading(false);
                        addErrorMessage("Xin lỗi, có lỗi xảy ra khi kết nối với AI. Vui lòng thử lại.");
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d(TAG, "API Response Code: " + response.code());
                    Log.d(TAG, "API Response: " + responseBody);
                    
                    mainHandler.post(() -> {
                        showLoading(false);
                        try {
                            if (response.isSuccessful()) {
                                JSONObject jsonResponse = new JSONObject(responseBody);
                                JSONArray candidates = jsonResponse.getJSONArray("candidates");
                                if (candidates.length() > 0) {
                                    JSONObject candidate = candidates.getJSONObject(0);
                                    JSONObject content = candidate.getJSONObject("content");
                                    JSONArray parts = content.getJSONArray("parts");
                                    if (parts.length() > 0) {
                                        String aiResponse = parts.getJSONObject(0).getString("text");
                                        addAIMessage(aiResponse);
                                    } else {
                                        addErrorMessage("AI không thể tạo phản hồi.");
                                    }
                                } else {
                                    addErrorMessage("AI không thể tạo phản hồi.");
                                }
                            } else {
                                String errorMessage = "Lỗi từ server: " + response.code();
                                if (response.code() == 403) {
                                    errorMessage += "\nCó thể do API key không hợp lệ hoặc không có quyền truy cập.\nVui lòng kiểm tra lại API key trong Google AI Studio.";
                                } else if (response.code() == 429) {
                                    errorMessage += "\nĐã vượt quá giới hạn request. Vui lòng thử lại sau.";
                                }
                                addErrorMessage(errorMessage);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response", e);
                            addErrorMessage("Có lỗi xảy ra khi xử lý phản hồi từ AI: " + e.getMessage());
                        }
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error creating request", e);
            showLoading(false);
            addErrorMessage("Có lỗi xảy ra khi tạo yêu cầu.");
        }
    }

    private void addAIMessage(String message) {
        ChatMessage aiMessage = new ChatMessage(message, false, System.currentTimeMillis());
        chatMessages.add(aiMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        scrollToBottom();
    }

    private void addErrorMessage(String errorMessage) {
        ChatMessage errorMsg = new ChatMessage(errorMessage, false, System.currentTimeMillis());
        chatMessages.add(errorMsg);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        scrollToBottom();
    }

    private void showLoading(boolean show) {
        progressBarSending.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonSend.setEnabled(!show);
    }

    private void scrollToBottom() {
        if (chatMessages.size() > 0) {
            recyclerViewChat.smoothScrollToPosition(chatMessages.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
        }
    }
}

