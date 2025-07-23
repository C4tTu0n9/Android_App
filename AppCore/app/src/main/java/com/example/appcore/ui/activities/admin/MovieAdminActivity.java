package com.example.appcore.ui.activities.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appcore.R;
import com.example.appcore.dao.MovieDAO;
import com.example.appcore.data.models.Movie;
import com.example.appcore.ui.adapter.MovieAdminAdapter;
import com.example.appcore.utils.AddMovieFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.List;


public class MovieAdminActivity extends BaseActivity implements MovieAdminAdapter.OnMovieActionListener {

    private static final String TAG = "MovieAdminActivity";
    private RecyclerView recyclerViewMovies;
    private MovieAdminAdapter movieAdminAdapter;
    private MovieDAO movieDAO;

    // Lưu lại listener để có thể gỡ bỏ khi Activity bị hủy
    private ValueEventListener movieValueEventListener;
    private FloatingActionButton fabAddMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Quan trọng: Gọi super.onCreate trước

        // --- SỬA ĐỔI QUAN TRỌNG ---
        // Không gọi setContentView() ở đây nữa.
        // Thay vào đó, lấy container từ layout của BaseActivity và chèn layout của màn hình này vào.
        // Giả sử trong `activity_base.xml` có một FrameLayout với id là `content_frame`.
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        LayoutInflater.from(this).inflate(R.layout.activity_admin_movie, contentFrame, true);
        // --- KẾT THÚC SỬA ĐỔI ---

        // Ánh xạ FAB từ layout activity_admin_movie.xml
        // Đổi ID trong XML từ `fabAddCategory` thành `fabAddMovie` để dễ hiểu hơn
        fabAddMovie = findViewById(R.id.fabAddMovie);

        // Khởi tạo các thành phần sau khi đã inflate layout
        movieDAO = new MovieDAO();
        recyclerViewMovies = findViewById(R.id.recyclerViewMovies);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thêm Phim Mới");
        }


        setupRecyclerView();
        loadMovies();

        // Gán sự kiện click cho FAB
        fabAddMovie.setOnClickListener(v -> {
            AddMovieFragment addMovieFragment = new AddMovieFragment();
            addMovieFragment.show(getSupportFragmentManager(), addMovieFragment.getTag());
        });
    }

    private void setupRecyclerView() {
        recyclerViewMovies.setLayoutManager(new LinearLayoutManager(this));
        // Truyền 'this' vì activity này đã implement OnMovieActionListener
        movieAdminAdapter = new MovieAdminAdapter(this, this);
        recyclerViewMovies.setAdapter(movieAdminAdapter);
    }

    private void loadMovies() {
        // Sửa: Sử dụng đúng interface `MovieDataCallback`
        movieValueEventListener = movieDAO.getAllMovies(new MovieDAO.MovieDataCallback() {
            @Override
            public void onDataFetched(List<Movie> movies) {
                movieAdminAdapter.setMovies(movies);
                Toast.makeText(MovieAdminActivity.this, "Tải thành công " + movies.size() + " bộ phim", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(DatabaseError error) {
                Log.e(TAG, "Lỗi khi tải dữ liệu phim: ", error.toException());
                Toast.makeText(MovieAdminActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Triển khai các phương thức từ interface OnMovieActionListener
    @Override
    public void onUpdateClicked(Movie movie) {
        AddMovieFragment updateFragment = AddMovieFragment.newInstance(movie);
        updateFragment.show(getSupportFragmentManager(), "UPDATE_MOVIE_FRAGMENT");
    }

    @Override
    public void onDeleteClicked(Movie movie) {
        // Hiển thị dialog xác nhận trước khi xóa
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa phim '" + movie.getMovieName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteMovieFromDatabase(movie);
                })
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteMovieFromDatabase(Movie movie) {
        movieDAO.deleteMovie(movie.getMovieId(), new MovieDAO.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(MovieAdminActivity.this, "Đã xóa phim: " + movie.getMovieName(), Toast.LENGTH_SHORT).show();
                // Dữ liệu sẽ tự động cập nhật do đang lắng nghe sự thay đổi trên Realtime Database
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MovieAdminActivity.this, "Lỗi khi xóa phim!", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi khi xóa phim: " + movie.getMovieId(), e);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Quan trọng: Gỡ bỏ listener để tránh rò rỉ bộ nhớ
        if (movieDAO != null && movieValueEventListener != null) {
            movieDAO.removeListener(movieValueEventListener);
        }
    }
}