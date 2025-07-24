package com.example.appcore.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appcore.R;
import com.example.appcore.data.models.Seat;

import java.util.ArrayList;
import java.util.List;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder> {
    
    private Context context;
    private List<Seat> seatList;
    private List<String> bookedSeats; // Ghế đã được đặt bởi người khác
    private List<String> selectedSeats; // Ghế người dùng hiện tại chọn
    private OnSeatSelectedListener listener;
    
    // Seat status constants
    public static final String STATUS_AVAILABLE = "available";
    public static final String STATUS_SELECTED = "selected";
    public static final String STATUS_BOOKED = "booked";
    
    public interface OnSeatSelectedListener {
        void onSeatSelected(Seat seat, boolean isSelected);
    }
    
    public SeatAdapter(Context context, List<Seat> seatList, OnSeatSelectedListener listener) {
        this.context = context;
        this.seatList = seatList;
        this.selectedSeats = new ArrayList<>();
        this.bookedSeats = new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_seat, parent, false);
        return new SeatViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        Seat seat = seatList.get(position);
        String seatKey = seat.getRow() + String.valueOf(seat.getNumber());
        
        // Set seat text
        holder.tvSeat.setText(seatKey);
        
        // Determine seat status and set background
        if (bookedSeats.contains(seatKey)) {
            // Seat is booked by someone else
            holder.tvSeat.setBackgroundResource(R.drawable.ic_seat_unavailable);
            holder.tvSeat.setEnabled(false);
        } else if (selectedSeats.contains(seatKey)) {
            // Seat is selected by current user
            holder.tvSeat.setBackgroundResource(R.drawable.ic_seat_selected);
            holder.tvSeat.setEnabled(true);
        } else {
            // Seat is available
            holder.tvSeat.setBackgroundResource(R.drawable.ic_seat_available);
            holder.tvSeat.setEnabled(true);
        }
        
        // Click listener - only for available and selected seats
        holder.tvSeat.setOnClickListener(v -> {
            if (!bookedSeats.contains(seatKey)) {
                boolean isCurrentlySelected = selectedSeats.contains(seatKey);
                
                if (isCurrentlySelected) {
                    // Deselect seat
                    selectedSeats.remove(seatKey);
                    seat.setStatus(STATUS_AVAILABLE);
                } else {
                    // Select seat
                    selectedSeats.add(seatKey);
                    seat.setStatus(STATUS_SELECTED);
                }
                
                // Notify adapter to update UI
                notifyItemChanged(position);
                
                // Notify listener
                if (listener != null) {
                    listener.onSeatSelected(seat, !isCurrentlySelected);
                }
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return seatList.size();
    }
    
    public void updateBookedSeats(List<String> bookedSeats) {
        this.bookedSeats = bookedSeats;
        notifyDataSetChanged();
    }
    
    public List<String> getSelectedSeats() {
        return new ArrayList<>(selectedSeats);
    }
    
    public void clearSelectedSeats() {
        selectedSeats.clear();
        // Update seat status in the list
        for (Seat seat : seatList) {
            if (seat.getStatus().equals(STATUS_SELECTED)) {
                seat.setStatus(STATUS_AVAILABLE);
            }
        }
        notifyDataSetChanged();
    }
    
    public int getSelectedSeatCount() {
        return selectedSeats.size();
    }
    
    public static class SeatViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeat;
        
        public SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSeat = itemView.findViewById(R.id.tvSeat);
        }
    }
} 