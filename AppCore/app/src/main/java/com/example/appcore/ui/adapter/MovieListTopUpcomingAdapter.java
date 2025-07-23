package com.example.appcore.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appcore.R; // Giả sử R file nằm ở đây
import com.example.appcore.data.models.Movie;

import java.util.List;

public class MovieListTopUpcomingAdapter extends RecyclerView.Adapter<MovieListTopUpcomingAdapter.ViewHolder> {

    private final List<Movie> moviesList;
    private final OnMovieClickListener clickListener; // Biến để lưu trữ listener


    public MovieListTopUpcomingAdapter(List<Movie> moviesList, OnMovieClickListener clickListener) {
        this.moviesList = moviesList;
        this.clickListener = clickListener;
    }

    // Thêm interface này vào trong file MovieListTopUpcomingAdapter.java
    public interface OnMovieClickListener {
        void onMovieClick(Movie movie, int position);
    }

    /**
     * Lớp ViewHolder: Nơi lưu giữ các tham chiếu đến View của một item.
     * Việc tìm kiếm View bằng findViewById() chỉ xảy ra MỘT LẦN ở đây.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Khai báo các View có trong layout item_film.xml
        public final ImageView moviePoster;
        public final TextView movieTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ View, thao tác tốn kém này chỉ chạy 1 lần khi ViewHolder được tạo
            moviePoster = itemView.findViewById(R.id.movieView);
            movieTitle = itemView.findViewById(R.id.movieName);
        }

        // Thêm một hàm để bind listener
        public void bind(final Movie movie, final int position, final OnMovieClickListener listener) {
            itemView.setOnClickListener(v -> listener.onMovieClick(movie, position));
        }
    }

    /**
     * Được gọi khi RecyclerView cần một ViewHolder mới để hiển thị một item.
     * Chúng ta tạo View từ layout XML và trả về một ViewHolder mới chứa View đó.
     */
    @NonNull
    @Override
    public MovieListTopUpcomingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_film, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = moviesList.get(position);
        // Gán dữ liệu cho View
        holder.movieTitle.setText(movie.getMovieName());
        Glide.with(holder.itemView.getContext()) // Lấy context từ itemView
                .load(movie.getImage())
                .into(holder.moviePoster);

        // Gán sự kiện click cho item
        holder.bind(movie, position, clickListener);
    }

    /**
     * Trả về tổng số item trong danh sách.
     */
    @Override
    public int getItemCount() {
        return moviesList != null ? moviesList.size() : 0;
    }
}