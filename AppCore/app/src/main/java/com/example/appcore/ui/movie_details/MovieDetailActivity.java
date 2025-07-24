package com.example.appcore.ui.movie_details;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.bumptech.glide.Glide;
import com.example.appcore.R;
import com.example.appcore.data.models.Movie;
import com.example.appcore.utils.DateUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class MovieDetailActivity extends AppCompatActivity {
    // khai báo các thành phần UI chính
    private ImageView moviePosterImageView, backButton;
    private TextView movieTitleTextView, movieYearDurationTextView, movieTimeTextView;
    private TextView summaryTitleTextView, summaryContentTextView, castListTextView;
    private AppCompatButton buyTicketButton;
    

    private static final String TAG = "MovieDetailActivity";
    private String currentMovieId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        // 1. Ánh xạ các View từ layout XML
        initViews();
        // 2. Gán sự kiện click cho các nút
        initListeners();
        // nhận id từ intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("MOVIE_ID")) {
            currentMovieId = intent.getStringExtra("MOVIE_ID");
            loadMovieDetailsFromFirebase(currentMovieId);
            loadShowTimesFromFirebase(currentMovieId);
        }else {
            finish();
        }
    }

    /**
     * Gán các sự kiện click cho các View tương tác được.
     */
    private void initListeners() {
        backButton.setOnClickListener(v -> finish()); // Nhấn nút back để quay lại màn hình trước

        buyTicketButton.setOnClickListener(v -> {
            // Chuyển đến màn hình chọn ghế
            Intent intent = new Intent(MovieDetailActivity.this, com.example.appcore.ui.activities.SeatListActivity.class);
            intent.putExtra("MOVIE_ID", currentMovieId);
            startActivity(intent);
        });
    }

    /**
     * Ánh xạ các biến trong code với các View trong file XML.
     */
    private void initViews() {
        moviePosterImageView = findViewById(R.id.moviePosterImageView);
        backButton = findViewById(R.id.backButton);
        movieTitleTextView = findViewById(R.id.movieTitleTextView);
        movieYearDurationTextView = findViewById(R.id.movieYearDurationTextView);
        movieTimeTextView = findViewById(R.id.movieTimeTextView);
        summaryTitleTextView = findViewById(R.id.summaryTitleTextView);
        summaryContentTextView = findViewById(R.id.summaryContentTextView);
        castListTextView = findViewById(R.id.castListTextView);
        buyTicketButton = findViewById(R.id.buyTicketButton);
    }

    /**
     * Tải dữ liệu chi tiết của phim từ Firebase Realtime Database.
     * @param currentMovieId ID của phim cần tải.
     */
    private void loadMovieDetailsFromFirebase(String currentMovieId) {
        DatabaseReference movieRef = FirebaseDatabase.getInstance().getReference("Movies").child(currentMovieId);
        // Sử dụng addListenerForSingleValueEvent để chỉ tải dữ liệu một lần.
        movieRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        // Chuyển đổi dữ liệu từ Firebase thành đối tượng Movie
                        Movie movie = snapshot.getValue(Movie.class);
                        if (movie != null) {
                            // Gán ID cho movie để đảm bảo movieId không null
                            movie.setMovieId(currentMovieId);
                            // 5. Đổ dữ liệu lên giao diện
                            populateUi(movie);
                        } else {
                            Toast.makeText(MovieDetailActivity.this, "Lỗi đọc dữ liệu phim.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MovieDetailActivity.this, "Lỗi xử lý dữ liệu phim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MovieDetailActivity.this, "Không tìm thấy thông tin phim.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // ### HÀM LẤY NGÀY CHIẾU (CHỈ HIỂN THỊ NGÀY) ###
    private void loadShowTimesFromFirebase(String movieId) {
        DatabaseReference showtimesRef = FirebaseDatabase.getInstance().getReference("ShowTimes");
        // Tạo một query để tìm tất cả các suất chiếu có movieId trùng khớp
        Query showtimeQuery = showtimesRef.orderByChild("movieId").equalTo(movieId);

        showtimeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long earliestShowtime = Long.MAX_VALUE;
                    boolean foundShowtime = false;

                    // Lặp qua tất cả các suất chiếu tìm được
                    for (DataSnapshot showtimeSnapshot : snapshot.getChildren()) {
                        Long showDateTime = showtimeSnapshot.child("showDateTime").getValue(Long.class);
                        if (showDateTime != null && showDateTime > System.currentTimeMillis()) {
                            if (showDateTime < earliestShowtime) {
                                earliestShowtime = showDateTime;
                                foundShowtime = true;
                            }
                        }
                    }

                    if (foundShowtime) {
                        String formattedDate = DateUtils.formatTimestamp(earliestShowtime, "dd/MM/yyyy");
                        movieYearDurationTextView.setText(formattedDate);
                    } else {
                        movieYearDurationTextView.setText("Hiện chưa có lịch chiếu");
                    }
                } else {
                    movieYearDurationTextView.setText("Chưa có lịch chiếu");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                movieYearDurationTextView.setText("Lỗi tải lịch chiếu");
            }
        });
    }

    /**
     * Đổ dữ liệu từ đối tượng Movie lên các thành phần của giao diện.
     * @param movie Đối tượng Movie chứa thông tin chi tiết.
     */
    private void populateUi(Movie movie) {
        // --- Cập nhật các TextView ---
        movieTimeTextView.setText(movie.getDurationInMinutes() + " phút");
        movieTitleTextView.setText(movie.getMovieName());
        summaryContentTextView.setText(movie.getDescription());
        // --- Hiển thị danh sách diễn viên ---
        try {
            if (movie.getCast() != null && !movie.getCast().isEmpty()) {
                StringBuilder castBuilder = new StringBuilder();
                for (int i = 0; i < movie.getCast().size(); i++) {
                    if (i > 0) castBuilder.append(", ");
                    // Xử lý trường hợp Actor hoặc actorName có thể null
                    if (movie.getCast().get(i) != null && movie.getCast().get(i).getActorName() != null) {
                        castBuilder.append(movie.getCast().get(i).getActorName());
                    } else {
                        castBuilder.append("Diễn viên ẩn danh");
                    }
                }
                castListTextView.setText(castBuilder.toString());
            } else {
                castListTextView.setText("Không có thông tin diễn viên");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing cast data", e);
            castListTextView.setText("Không thể hiển thị thông tin diễn viên");
        }
        // --- Hiển thị ảnh Poster ---
        Glide.with(this)
                .load(movie.getImage())
                .placeholder(R.drawable.ic_launcher_background) // Ảnh hiển thị trong lúc tải
                .error(R.drawable.ic_launcher_background) // Ảnh hiển thị khi lỗi
                .into(moviePosterImageView);
    }

}