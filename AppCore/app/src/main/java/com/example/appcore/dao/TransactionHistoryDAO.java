package com.example.appcore.dao;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.appcore.data.models.TransactionHistory;
import com.example.appcore.utils.QRCodeGenerator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryDAO {
    private static final String TAG = "TransactionHistoryDAO";
    private DatabaseReference databaseReference;

    public TransactionHistoryDAO() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("transaction_history");
    }

    /**
     * Interface callback để trả về danh sách transaction history
     */
    public interface TransactionHistoryCallback {
        void onSuccess(List<TransactionHistory> transactions);
        void onFailure(String error);
    }

    /**
     * Interface callback để trả về QR code bitmap
     */
    public interface QRCodeCallback {
        void onSuccess(Bitmap qrBitmap);
        void onFailure(String error);
    }

    /**
     * Lấy danh sách transaction history theo userId
     * @param userId ID người dùng
     * @param callback Callback trả về kết quả
     */
    public void getTransactionHistoryByUserId(String userId, TransactionHistoryCallback callback) {
        databaseReference.orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<TransactionHistory> transactions = new ArrayList<>();
                        
                        for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                            TransactionHistory transaction = transactionSnapshot.getValue(TransactionHistory.class);
                            if (transaction != null) {
                                transactions.add(transaction);
                            }
                        }
                        
                        // Sắp xếp theo timestamp giảm dần (mới nhất trước)
                        transactions.sort((t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                        
                        callback.onSuccess(transactions);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error getting transaction history: " + error.getMessage());
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    /**
     * Thêm transaction history mới
     * @param transaction Transaction history object
     * @param userId ID người dùng
     * @param invoiceId ID hóa đơn
     */
    public void addTransactionHistory(TransactionHistory transaction, String userId, String invoiceId) {
        String transactionId = databaseReference.push().getKey();
        if (transactionId != null) {
            // Thêm thông tin bổ sung
            transaction.setTimestamp(System.currentTimeMillis());
            
            databaseReference.child(transactionId).setValue(transaction)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Transaction history added successfully: " + transactionId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding transaction history", e);
                    });
        }
    }

    /**
     * Tạo QR code cho transaction history
     * @param transaction Transaction history object
     * @param userId ID người dùng
     * @param invoiceId ID hóa đơn
     * @param callback Callback trả về QR code bitmap
     */
    public void generateQRCodeForTransaction(TransactionHistory transaction, String userId, 
                                           String invoiceId, QRCodeCallback callback) {
        try {
            // Tạo QR code với thông tin từ transaction
            Bitmap qrBitmap = QRCodeGenerator.generateMovieTicketQR(
                    transaction.getMovieName(),
                    transaction.getShowDate(),
                    transaction.getShowTime(),
                    transaction.getRoomName(),
                    transaction.getSelectedSeats(),
                    invoiceId,
                    userId
            );

            if (qrBitmap != null) {
                callback.onSuccess(qrBitmap);
            } else {
                callback.onFailure("Không thể tạo QR code");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error generating QR code for transaction", e);
            callback.onFailure("Lỗi tạo QR code: " + e.getMessage());
        }
    }

    /**
     * Lấy transaction history theo ID
     * @param transactionId ID transaction
     * @param callback Callback trả về kết quả
     */
    public void getTransactionById(String transactionId, TransactionHistoryCallback callback) {
        databaseReference.child(transactionId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TransactionHistory transaction = snapshot.getValue(TransactionHistory.class);
                List<TransactionHistory> transactions = new ArrayList<>();
                if (transaction != null) {
                    transactions.add(transaction);
                }
                callback.onSuccess(transactions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error getting transaction by ID: " + error.getMessage());
                callback.onFailure(error.getMessage());
            }
        });
    }

    /**
     * Cập nhật trạng thái transaction (ví dụ: đã sử dụng)
     * @param transactionId ID transaction
     * @param status Trạng thái mới
     */
    public void updateTransactionStatus(String transactionId, String status) {
        databaseReference.child(transactionId).child("status").setValue(status)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Transaction status updated: " + transactionId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating transaction status", e);
                });
    }

    /**
     * Lấy tất cả transaction history (cho admin)
     * @param callback Callback trả về kết quả
     */
    public void getAllTransactionHistory(TransactionHistoryCallback callback) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<TransactionHistory> transactions = new ArrayList<>();
                
                for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                    TransactionHistory transaction = transactionSnapshot.getValue(TransactionHistory.class);
                    if (transaction != null) {
                        transactions.add(transaction);
                    }
                }
                
                // Sắp xếp theo timestamp giảm dần
                transactions.sort((t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                
                callback.onSuccess(transactions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error getting all transaction history: " + error.getMessage());
                callback.onFailure(error.getMessage());
            }
        });
    }

    /**
     * Lấy doanh thu theo khoảng thời gian
     * @param startTime Thời gian bắt đầu (timestamp)
     * @param endTime Thời gian kết thúc (timestamp)
     * @param callback Callback trả về kết quả
     */
    public void getRevenueByTimeRange(long startTime, long endTime, TransactionHistoryCallback callback) {
        databaseReference.orderByChild("timestamp")
                .startAt(startTime)
                .endAt(endTime)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<TransactionHistory> transactions = new ArrayList<>();
                        
                        for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                            TransactionHistory transaction = transactionSnapshot.getValue(TransactionHistory.class);
                            if (transaction != null) {
                                transactions.add(transaction);
                            }
                        }
                        
                        callback.onSuccess(transactions);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error getting revenue by time range: " + error.getMessage());
                        callback.onFailure(error.getMessage());
                    }
                });
    }
}

