package com.example.appcore.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appcore.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateSelectionAdapter extends RecyclerView.Adapter<DateSelectionAdapter.DateViewHolder> {
    
    private Context context;
    private List<Date> dateList;
    private int selectedPosition = -1;
    private OnDateSelectedListener listener;
    
    public interface OnDateSelectedListener {
        void onDateSelected(Date date, int position);
    }
    
    public DateSelectionAdapter(Context context, List<Date> dateList, OnDateSelectedListener listener) {
        this.context = context;
        this.dateList = dateList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_date_selection, parent, false);
        return new DateViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        Date date = dateList.get(position);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        // Format date components
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", new Locale("vi", "VN"));
        
        String dayOfWeek = dayFormat.format(date);
        String dayNumber = dateFormat.format(date);
        String month = monthFormat.format(date);
        
        // Set data
        holder.tvDayOfWeek.setText(dayOfWeek);
        holder.tvDate.setText(dayNumber);
        holder.tvMonth.setText(month);
        
        // Handle selection state
        boolean isSelected = position == selectedPosition;
        if (isSelected) {
            holder.cardDate.setCardBackgroundColor(ContextCompat.getColor(context, R.color.orange));
            holder.tvDate.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.tvDayOfWeek.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.tvMonth.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            holder.cardDate.setCardBackgroundColor(ContextCompat.getColor(context, R.color.dark_grey));
            holder.tvDate.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.tvDayOfWeek.setTextColor(ContextCompat.getColor(context, R.color.grey));
            holder.tvMonth.setTextColor(ContextCompat.getColor(context, R.color.grey));
        }
        
        // Click listener
        holder.cardDate.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = position;
            
            // Notify changes for smooth animation
            if (previousPosition != -1) {
                notifyItemChanged(previousPosition);
            }
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onDateSelected(date, position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return dateList.size();
    }
    
    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition);
        }
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition);
        }
    }
    
    public static class DateViewHolder extends RecyclerView.ViewHolder {
        CardView cardDate;
        TextView tvDayOfWeek, tvDate, tvMonth;
        
        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            cardDate = itemView.findViewById(R.id.cardDate);
            tvDayOfWeek = itemView.findViewById(R.id.tvDayOfWeek);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMonth = itemView.findViewById(R.id.tvMonth);
        }
    }
} 