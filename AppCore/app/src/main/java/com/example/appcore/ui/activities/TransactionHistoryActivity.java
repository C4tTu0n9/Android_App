package com.example.appcore;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appcore.dao.TransactionHistoryDAO;
import com.example.appcore.data.models.TransactionHistory;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {
    private static final String TAG = "TransactionHistoryActivity";
    
    private ImageView btnBack;
    private RecyclerView recyclerViewTransactions;
    private TransactionHistoryAdapter adapter;
    private TransactionHistoryDAO transactionDAO;
    private String userId;
    private List<TransactionHistory> transactionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        initViews();
        initData();
        setupClickListeners();
        loadTransactionHistory();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        recyclerViewTransactions = findViewById(R.id.recycler_transactions);
        
        // Setup RecyclerView
        transactionList = new ArrayList<>();
        adapter = new TransactionHistoryAdapter(this, transactionList, this::showQRDialog);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(adapter);
    }

    private void initData() {
        transactionDAO = new TransactionHistoryDAO();
        
        // Lấy userId từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");
        
        if (userId.isEmpty()) {
            Log.e(TAG, "userId không hợp lệ");
            Toast.makeText(this, "Không thể tải lịch sử giao dịch", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupClickListeners() {
        // Back button click listener
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close current activity and return to previous screen
            }
        });
    }

    private void loadTransactionHistory() {
        transactionDAO.getTransactionHistoryByUserId(userId, new TransactionHistoryDAO.TransactionHistoryCallback() {
            @Override
            public void onSuccess(List<TransactionHistory> transactions) {
                runOnUiThread(() -> {
                    transactionList.clear();
                    transactionList.addAll(transactions);
                    adapter.notifyDataSetChanged();
                    
                    Log.d(TAG, "Loaded " + transactions.size() + " transactions");
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Error loading transactions: " + error);
                    Toast.makeText(TransactionHistoryActivity.this, 
                                 "Lỗi tải lịch sử giao dịch: " + error, 
                                 Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showQRDialog(TransactionHistory transaction) {
        // Create custom dialog to show QR code in full size
        Dialog qrDialog = new Dialog(this);
        qrDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        qrDialog.setContentView(R.layout.dialog_qr_code);
        
        // Set dialog properties
        Window window = qrDialog.getWindow();
        if (window != null) {
            window.setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                (int) (getResources().getDisplayMetrics().heightPixels * 0.7)
            );
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Find views in dialog
        ImageView dialogQrImage = qrDialog.findViewById(R.id.dialog_qr_image);
        TextView dialogCloseBtn = qrDialog.findViewById(R.id.dialog_close_btn);
        TextView movieTitle = qrDialog.findViewById(R.id.movie_title);
        TextView movieDetails = qrDialog.findViewById(R.id.movie_details);
        TextView seatInfo = qrDialog.findViewById(R.id.seat_info);
        TextView showtimeInfo = qrDialog.findViewById(R.id.showtime_info);

        // Set movie information
        movieTitle.setText(transaction.getMovieName());
        movieDetails.setText(transaction.getRoomName());
        seatInfo.setText("Ghế: " + String.join(", ", transaction.getSelectedSeats()));
        showtimeInfo.setText(transaction.getShowTime() + " | " + transaction.getShowDate());

        // Generate and set QR code dynamically
        transactionDAO.generateQRCodeForTransaction(transaction, userId, "INV_" + transaction.getTimestamp(), 
            new TransactionHistoryDAO.QRCodeCallback() {
                @Override
                public void onSuccess(Bitmap qrBitmap) {
                    runOnUiThread(() -> {
                        dialogQrImage.setImageBitmap(qrBitmap);
                    });
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Error generating QR code: " + error);
                        Toast.makeText(TransactionHistoryActivity.this, 
                                     "Lỗi tạo QR code: " + error, 
                                     Toast.LENGTH_SHORT).show();
                        // Fallback to default QR image
                        dialogQrImage.setImageResource(R.drawable.qr_movie_ticket);
                    });
                }
            });

        // Close button click listener
        dialogCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrDialog.dismiss();
            }
        });

        // Show dialog
        qrDialog.show();
    }
}

