package com.example.appcore.dao;

import androidx.annotation.NonNull;

import com.example.appcore.data.models.Movie;
import com.example.appcore.data.remote.FirebaseDataSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MovieDAO {
    private final DatabaseReference databaseReference;

    public MovieDAO() {
        databaseReference = FirebaseDataSource.getInstance().getReference("Movies");
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    // Đổi tên interface cho rõ ràng hơn
    public interface MovieDataCallback {
        void onDataFetched(List<Movie> movies);
        void onError(DatabaseError error);
    }

    /**
     * Lấy tất cả các bộ phim và lắng nghe sự thay đổi.
     * Sắp xếp theo tên phim (movieName).
     * @param callback Callback để nhận dữ liệu hoặc lỗi.
     * @return ValueEventListener để Activity có thể gỡ bỏ khi không cần thiết.
     */
    public ValueEventListener getAllMovies(MovieDataCallback callback) {
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Movie> movies = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Movie movie = snapshot.getValue(Movie.class);
                    if (movie != null) {
                        movie.setMovieId(snapshot.getKey());
                        movies.add(movie);
                    }
                }
                callback.onDataFetched(movies);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError);
            }
        };
        databaseReference.orderByChild("movieName").addValueEventListener(eventListener);
        return eventListener;
    }

    public void addMovie(Movie movie, ActionCallback callback) {
        // 1. Tạo một tham chiếu mới với ID duy nhất bằng push() tại `databaseReference`
        // Thao tác này chưa gửi dữ liệu gì lên server.
        DatabaseReference newMovieRef = databaseReference.push();

        // 2. Lấy key (ID) duy nhất vừa được tạo ra từ tham chiếu trên
        String newMovieId = newMovieRef.getKey();

        // 3. Gán ID này vào chính đối tượng movie của bạn
        // Điều này rất quan trọng để sau này bạn có thể dễ dàng biết được ID của movie object.
        movie.setMovieId(newMovieId);

        // 4. Lưu toàn bộ đối tượng movie (bây giờ đã có ID) vào vị trí tham chiếu mới
        newMovieRef.setValue(movie)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    /**
     * Cập nhật thông tin của một bộ phim đã có bằng cách ghi đè toàn bộ object.
     * @param movie Đối tượng Movie chứa thông tin mới (phải có movieId).
     * @param callback Callback để thông báo kết quả.
     */
    public void updateMovie(Movie movie, ActionCallback callback) {
        if (movie.getMovieId() == null || movie.getMovieId().isEmpty()) {
            // Đảm bảo có ID để biết cần cập nhật bản ghi nào
            callback.onFailure(new IllegalArgumentException("Movie ID không được rỗng khi cập nhật."));
            return;
        }

        // Dùng setValue để ghi đè toàn bộ dữ liệu tại nút đó
        databaseReference.child(movie.getMovieId()).setValue(movie)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    // Bạn có thể xóa hoặc giữ lại hàm update bằng Map cũ nếu muốn dùng ở nơi khác
    /*
    public void updateMovie(String movieId, Map<String, Object> movieUpdates, ActionCallback callback) {
        databaseReference.child(movieId).updateChildren(movieUpdates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }
    */

    public void deleteMovie(String movieId, ActionCallback callback) {
        databaseReference.child(movieId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public void removeListener(ValueEventListener listener) {
        if (listener != null) {
            databaseReference.removeEventListener(listener);
        }
    }
}