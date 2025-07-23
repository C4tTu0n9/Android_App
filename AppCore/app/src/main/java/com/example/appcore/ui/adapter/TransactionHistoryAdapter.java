package com.example.appcore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appcore.data.models.TransactionHistory;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.TransactionViewHolder> {
    
    private Context context;
    private List<TransactionHistory> transactionList;
    private OnQRClickListener qrClickListener;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    public interface OnQRClickListener {
        void onQRClick(TransactionHistory transaction);
    }

    public TransactionHistoryAdapter(Context context, List<TransactionHistory> transactionList, OnQRClickListener qrClickListener) {
        this.context = context;
        this.transactionList = transactionList;
        this.qrClickListener = qrClickListener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction_history, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionHistory transaction = transactionList.get(position);
        
        // Set movie information
        holder.movieTitle.setText(transaction.getMovieName());
        holder.roomName.setText(transaction.getRoomName());
        holder.showTime.setText(transaction.getShowTime());
        holder.showDate.setText(transaction.getShowDate());
        
        // Set seats
        if (transaction.getSelectedSeats() != null && !transaction.getSelectedSeats().isEmpty()) {
            holder.seatInfo.setText("Ghế: " + String.join(", ", transaction.getSelectedSeats()));
        } else {
            holder.seatInfo.setText("Ghế: N/A");
        }
        
        // Set price
        holder.totalPrice.setText("-" + formatCurrency(transaction.getTotalPrice()));
        
        // Set transaction time
        String transactionTime = dateFormat.format(new Date(transaction.getTimestamp()));
        holder.transactionTime.setText(transactionTime);
        
        // Set status based on timestamp (simple logic)
        long currentTime = System.currentTimeMillis();
        long transactionTimestamp = transaction.getTimestamp();
        long daysDiff = (currentTime - transactionTimestamp) / (24 * 60 * 60 * 1000);
        
        if (daysDiff > 1) {
            holder.status.setText("Đã sử dụng");
            holder.status.setBackgroundResource(R.drawable.status_used_background);
            holder.status.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        } else {
            holder.status.setText("Thành công");
            holder.status.setBackgroundResource(R.drawable.status_success_background);
            holder.status.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        }
        
        // QR click listener
        holder.btnViewQr.setOnClickListener(v -> {
            if (qrClickListener != null) {
                qrClickListener.onQRClick(transaction);
            }
        });
        
        holder.qrCodeImage.setOnClickListener(v -> {
            if (qrClickListener != null) {
                qrClickListener.onQRClick(transaction);
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    private String formatCurrency(int amount) {
        return String.format(Locale.getDefault(), "%,d đ", amount);
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView movieTitle, roomName, showTime, showDate, seatInfo, totalPrice, transactionTime, status, btnViewQr;
        ImageView qrCodeImage, movieIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            
            movieTitle = itemView.findViewById(R.id.movie_title);
            roomName = itemView.findViewById(R.id.room_name);
            showTime = itemView.findViewById(R.id.show_time);
            showDate = itemView.findViewById(R.id.show_date);
            seatInfo = itemView.findViewById(R.id.seat_info);
            totalPrice = itemView.findViewById(R.id.total_price);
            transactionTime = itemView.findViewById(R.id.transaction_time);
            status = itemView.findViewById(R.id.status);
            btnViewQr = itemView.findViewById(R.id.btn_view_qr);
            qrCodeImage = itemView.findViewById(R.id.qr_code_image);
            movieIcon = itemView.findViewById(R.id.movie_icon);
        }
    }
}

