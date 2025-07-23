package com.example.appcore.data.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistory implements Serializable {
    private String movieName;
    private String showDate;
    private String showTime;
    private long timestamp;
    private int totalPrice;
    private String roomName;
    private List<String> selectedSeats;
    private String poster;
}
