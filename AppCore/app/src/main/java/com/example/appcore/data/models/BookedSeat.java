package com.example.appcore.data.models;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookedSeat implements Serializable {
    private String showtimeId;
    private String roomId;
    private char row;
    private int number;
    private String userId;
    private long bookingTime;
    
    // Constructor để tạo seat key
    public String getSeatKey() {
        return row + String.valueOf(number);
    }
    
    // Constructor từ seat và thông tin booking
    public BookedSeat(String showtimeId, String roomId, Seat seat, String userId) {
        this.showtimeId = showtimeId;
        this.roomId = roomId;
        this.row = seat.getRow();
        this.number = seat.getNumber();
        this.userId = userId;
        this.bookingTime = System.currentTimeMillis();
    }
} 