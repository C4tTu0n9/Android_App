package com.example.appcore.ui.common.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appcore.R;

import java.util.List;

public class MoviePosterAdapter extends RecyclerView.Adapter<MoviePosterAdapter.MoviePosterViewHolder> {

    private List<Integer> moviePosterList; // Sử dụng Integer để chứa ID của drawable

    public MoviePosterAdapter(List<Integer> moviePosterList) {
        this.moviePosterList = moviePosterList;
    }

    @NonNull
    @Override
    public MoviePosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_poster, parent, false);
        return new MoviePosterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoviePosterViewHolder holder, int position) {
        holder.posterImage.setImageResource(moviePosterList.get(position));
    }

    @Override
    public int getItemCount() {
        return moviePosterList.size();
    }

    static class MoviePosterViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImage;

        public MoviePosterViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImage = itemView.findViewById(R.id.iv_poster);
        }
    }
}