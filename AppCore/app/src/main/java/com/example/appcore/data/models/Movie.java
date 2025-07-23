package com.example.appcore.data.models;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie implements Serializable {
    private String movieId;
    private String movieName;
    private String director;
    private String description;
    private String image;
    private String categoryId;
    private double price;
    private int revenue;
    private List<Actor> cast;
    private String status;
    private int durationInMinutes;
}
