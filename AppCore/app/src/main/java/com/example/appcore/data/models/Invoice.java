package com.example.appcore.data.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice implements Serializable{
    private String invoiceId;
    private int quantity;
    private int totalAmount;
    private int paymentMethod;
    private int status;
    private String time;
    private String userId;
    private String showtimeId;
}
