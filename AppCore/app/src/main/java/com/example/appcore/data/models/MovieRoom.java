package com.example.appcore.data.models;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieRoom implements Serializable {
    private String roomId;
    private String roomName;
    private int roomType;
    private int seatCount;
}
