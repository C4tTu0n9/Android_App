package com.example.appcore.data.models;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class History implements Serializable {
    private String historyId;
    private Date releaseDate;
    private String theater;
    private String showTime;
    private String ticketCount;
    private String selectedSeats;
    private String paymentMethod;
    private String totalAmount;
    private String movieId;
}
