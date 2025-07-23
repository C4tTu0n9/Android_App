package com.example.appcore.data.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowTime implements Serializable {
    private String showtimeId;
    private String poster;
    private String movieId;
    private String roomId;
    private long showDateTime;
    private String movieName;
    private String roomName;



    @Exclude
    public String getShowDate() {
        Date date = new Date(showDateTime);
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    @Exclude
    public String getShowTime() {
        Date date = new Date(showDateTime);
        return new SimpleDateFormat("HH:mm").format(date);
    }
}
