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
import com.example.appcore.data.models.ShowTime;

import java.util.List;

public class TimeSelectionAdapter extends RecyclerView.Adapter<TimeSelectionAdapter.TimeViewHolder> {
    
    private Context context;
    private List<ShowTime> showTimeList;
    private int selectedPosition = -1;
    private OnTimeSelectedListener listener;
    
    public interface OnTimeSelectedListener {
        void onTimeSelected(ShowTime showTime, int position);
    }
    
    public TimeSelectionAdapter(Context context, List<ShowTime> showTimeList, OnTimeSelectedListener listener) {
        this.context = context;
        this.showTimeList = showTimeList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_time_selection, parent, false);
        return new TimeViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TimeViewHolder holder, int position) {
        ShowTime showTime = showTimeList.get(position);
        
        // Set data
        holder.tvTime.setText(showTime.getShowTime());
        holder.tvRoomName.setText(showTime.getRoomName());
        
        // Handle selection state
        boolean isSelected = position == selectedPosition;
        if (isSelected) {
            holder.cardTime.setCardBackgroundColor(ContextCompat.getColor(context, R.color.orange));
            holder.tvTime.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.tvRoomName.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            holder.cardTime.setCardBackgroundColor(ContextCompat.getColor(context, R.color.dark_grey));
            holder.tvTime.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.tvRoomName.setTextColor(ContextCompat.getColor(context, R.color.grey));
        }
        
        // Click listener
        holder.cardTime.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = position;
            
            // Notify changes for smooth animation
            if (previousPosition != -1) {
                notifyItemChanged(previousPosition);
            }
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onTimeSelected(showTime, position);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return showTimeList.size();
    }
    
    public void updateShowTimes(List<ShowTime> newShowTimes) {
        this.showTimeList = newShowTimes;
        this.selectedPosition = -1; // Reset selection
        notifyDataSetChanged();
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
    
    public static class TimeViewHolder extends RecyclerView.ViewHolder {
        CardView cardTime;
        TextView tvTime, tvRoomName;
        
        public TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTime = itemView.findViewById(R.id.cardTime);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
        }
    }
} 