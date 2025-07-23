package com.example.appcore.data.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat implements Serializable {
    private char row;
    private int number;
    private String status;
    private int price;
}
