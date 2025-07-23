package com.example.appcore.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appcore.R;
import com.example.appcore.data.models.Movie;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MovieAdminAdapter extends RecyclerView.Adapter<MovieAdminAdapter.MovieViewHolder> {

    private final Context context;
    private List<Movie> movieList;
    private final OnMovieActionListener actionListener; // Thêm listener

    // Interface để xử lý các sự kiện click từ Adapter ra Activity
    public interface OnMovieActionListener {
        void onUpdateClicked(Movie movie);
        void onDeleteClicked(Movie movie);
    }

    // Cập nhật constructor để nhận listener
    public MovieAdminAdapter(Context context, OnMovieActionListener listener) {
        this.context = context;
        this.actionListener = listener;
        this.movieList = new ArrayList<>();
    }

    public void setMovies(List<Movie> movies) {
        this.movieList = movies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_admin, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        if (movie == null) return;

        holder.textMovieTitle.setText(String.format("Tên phim: %s", movie.getMovieName()));
        holder.textDirector.setText(String.format("Đạo diễn: %s", movie.getDirector()));
        holder.textViewMovieTime.setText(String.format("Thời lượng: %s phút", movie.getDurationInMinutes()));

        Glide.with(context)
                .load(movie.getImage())
                .into(holder.imagePoster);

        // Gọi phương thức của interface khi click
        holder.buttonUpdate.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onUpdateClicked(movie);
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteClicked(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList != null ? movieList.size() : 0;
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imagePoster;
        TextView textMovieTitle, textViewMovieTime, textDirector;
        ImageButton buttonUpdate, buttonDelete;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePoster = itemView.findViewById(R.id.imageViewPoster);
            textMovieTitle = itemView.findViewById(R.id.textViewMovieTitle);
            textViewMovieTime = itemView.findViewById(R.id.textViewMovieTime);
            textDirector = itemView.findViewById(R.id.textViewDirector);
            buttonUpdate = itemView.findViewById(R.id.buttonUpdate);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}