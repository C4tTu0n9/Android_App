package com.example.appcore.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.appcore.R;
import com.example.appcore.data.models.Movie;
import com.example.appcore.ui.adapter.MovieListTopUpcomingAdapter;
import com.example.appcore.ui.common.adapters.GenericSliderAdapter;
import com.example.appcore.ui.movie_details.MovieDetailActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    // --- Hằng số cho Firebase ---
    private static final String FIREBASE_NODE_MOVIES = "Movies";
    private static final String FIREBASE_FIELD_STATUS = "status";
    private static final String STATUS_NOW_SHOWING = "now_showing";
    private static final String STATUS_UPCOMING = "upcoming";
    private static final String TAG = "HomeFragment"; // Đổi tên TAG cho phù hợp

    // --- Views ---
    private ViewPager2 viewPagerBannerMovie;
    private ProgressBar progressBarBannerMovie, progressBarTopMovies, progressBarUpcomingMovies;
    private RecyclerView recyclerViewTopMovies, recyclerViewUpcomingMovies;

    private TextView textViewUserName, textViewUserEmail;

    // --- Adapters ---
    private GenericSliderAdapter<Movie> bannerMovieAdapter;
    private MovieListTopUpcomingAdapter topMoviesAdapter;
    private MovieListTopUpcomingAdapter upcomingMoviesAdapter;

    // --- Data Lists ---
    private List<Movie> bannerMoviesList;
    private List<Movie> topMoviesList;
    private List<Movie> upcomingMoviesList;

    // --- Firebase & Listeners ---
    private DatabaseReference moviesRef;
    private ValueEventListener bannerMoviesListener, topMoviesListener, upcomingMoviesListener;
    private Query topMoviesQuery, upcomingMoviesQuery;

    // --- Banner Slider Handler ---
    private final Handler slideHandler = new Handler(Looper.getMainLooper());
    private Runnable slideRunnable;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Bước 1: Nạp layout XML vào và trả về một đối tượng View.
        // Đây là phương thức thay thế cho setContentView của Activity.
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Bước 2: Sau khi view đã được tạo, tất cả logic sẽ được viết ở đây.
        // Phương thức này thay thế cho phần logic trong onCreate của Activity cũ.

        initDataLists();
        // Truyền 'view' vào để có thể tìm các view con bên trong nó
        initViews(view);
        setupBannerMovieViewPager();
        setupAllRecyclerViews();
        loadAndDisplayUserInfo();
        moviesRef = FirebaseDatabase.getInstance().getReference(FIREBASE_NODE_MOVIES);
        fetchAllMovies();
    }

    /**
     * Khởi tạo các danh sách chứa dữ liệu phim.
     */
    private void initDataLists() {
        bannerMoviesList = new ArrayList<>();
        topMoviesList = new ArrayList<>();
        upcomingMoviesList = new ArrayList<>();
    }

    /**
     * Ánh xạ các view từ layout XML.
     */
    private void initViews(View view) {
        // Tất cả findViewById phải được gọi từ 'view' của Fragment
        viewPagerBannerMovie = view.findViewById(R.id.viewPager2);
        progressBarBannerMovie = view.findViewById(R.id.progressBar2);
        recyclerViewTopMovies = view.findViewById(R.id.recyclerViewTopMovies);
        progressBarTopMovies = view.findViewById(R.id.progressBarTopMovie);
        recyclerViewUpcomingMovies = view.findViewById(R.id.recyclerViewUpcoming);
        progressBarUpcomingMovies = view.findViewById(R.id.progressBarUpcoming);
        // Ánh xạ TextViews cho username và email
        textViewUserName = view.findViewById(R.id.textView2);
        textViewUserEmail = view.findViewById(R.id.textView3);
    }

//    Hàm đọc dữ liệu từ SharedPreferences và hiển thị lên UI
    private void loadAndDisplayUserInfo() {
        if (getContext() == null) return; // Đảm bảo context không null

        // Truy cập vào file SharedPreferences có tên "login_prefs"
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE);

        // Đọc dữ liệu đã lưu. Nếu không tìm thấy, sẽ dùng giá trị mặc định (ví dụ: "User", "")
        String fullName = sharedPreferences.getString("fullName", "User");
        String email = sharedPreferences.getString("email", "");

        // Gán dữ liệu đọc được vào TextViews
        textViewUserName.setText("Hello " + fullName);
        textViewUserEmail.setText(email);
    }
    /**
     * Cài đặt cho ViewPager hiển thị banner phim.
     */
    private void setupBannerMovieViewPager() {
        // Sử dụng getContext() để lấy Context thay vì 'this'
        bannerMovieAdapter = new GenericSliderAdapter<>(
                getContext(),
                bannerMoviesList,
                R.layout.viewholder_slider,
                R.id.imageSlide,
                Movie::getImage
        );

        bannerMovieAdapter.setOnItemClickListener((movie, position) -> {
            Log.d(TAG, "Clicked on banner movie: " + movie.getMovieName());
            navigateToMovieDetails(movie);
        });

        viewPagerBannerMovie.setAdapter(bannerMovieAdapter);
        viewPagerBannerMovie.setClipToPadding(false);
        viewPagerBannerMovie.setClipChildren(false);
        viewPagerBannerMovie.setOffscreenPageLimit(3);
        viewPagerBannerMovie.setPageTransformer(new MarginPageTransformer((int) (16 * getResources().getDisplayMetrics().density)));

        setupBannerAutoScroll();
    }

    /**
     * Cài đặt cho tất cả RecyclerViews trong màn hình.
     */
    private void setupAllRecyclerViews() {
        // Khởi tạo Adapter cho "Top Movies"
        topMoviesAdapter = new MovieListTopUpcomingAdapter(topMoviesList, (movie, position) -> {
            Log.d(TAG, "Clicked on Top Movie: " + movie.getMovieName());
            navigateToMovieDetails(movie);
        });
        // Khởi tạo Adapter cho "Upcoming Movies"
        upcomingMoviesAdapter = new MovieListTopUpcomingAdapter(upcomingMoviesList, (movie, position) -> {
            Log.d(TAG, "Clicked on Upcoming Movie: " + movie.getMovieName());
            navigateToMovieDetails(movie);
        });
        // Sử dụng hàm chung để cài đặt
        setupRecyclerView(recyclerViewTopMovies, topMoviesAdapter);
        setupRecyclerView(recyclerViewUpcomingMovies, upcomingMoviesAdapter);
    }

    /**
     * Hàm chung để cài đặt một RecyclerView với Horizontal Layout Manager.
     */
    private void setupRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter<?> adapter) {
        // Sử dụng getContext() để lấy Context thay vì 'this'
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Bắt đầu quá trình tải dữ liệu cho tất cả các section.
     */
    private void fetchAllMovies() {
        fetchBannerMovies();
        fetchMoviesByStatus(STATUS_NOW_SHOWING);
        fetchMoviesByStatus(STATUS_UPCOMING);
    }

    private void fetchBannerMovies() {
        progressBarBannerMovie.setVisibility(View.VISIBLE);
        if (bannerMoviesListener == null) {
            bannerMoviesListener = createGenericMovieListener(bannerMoviesList, bannerMovieAdapter, progressBarBannerMovie, "Banner Movies");
            moviesRef.addValueEventListener(bannerMoviesListener);
        }
    }

    private void fetchMoviesByStatus(final String status) {
        if (status.equals(STATUS_NOW_SHOWING)) {
            progressBarTopMovies.setVisibility(View.VISIBLE);
            if (topMoviesListener == null) {
                topMoviesQuery = moviesRef.orderByChild(FIREBASE_FIELD_STATUS).equalTo(STATUS_NOW_SHOWING);
                topMoviesListener = createGenericMovieListener(topMoviesList, topMoviesAdapter, progressBarTopMovies, "Top Movies");
                topMoviesQuery.addValueEventListener(topMoviesListener);
            }
        } else if (status.equals(STATUS_UPCOMING)) {
            progressBarUpcomingMovies.setVisibility(View.VISIBLE);
            if (upcomingMoviesListener == null) {
                upcomingMoviesQuery = moviesRef.orderByChild(FIREBASE_FIELD_STATUS).equalTo(STATUS_UPCOMING);
                upcomingMoviesListener = createGenericMovieListener(upcomingMoviesList, upcomingMoviesAdapter, progressBarUpcomingMovies, "Upcoming Movies");
                upcomingMoviesQuery.addValueEventListener(upcomingMoviesListener);
            }
        }
    }

    /**
     * Hàm tái sử dụng để tạo một ValueEventListener chung cho các danh sách phim.
     * @param movieList Danh sách để thêm dữ liệu vào
     * @param adapter Adapter để thông báo thay đổi
     * @param progressBar ProgressBar để ẩn/hiện
     * @param logTag Tag để ghi log
     * @return Một instance của ValueEventListener
     */
    private ValueEventListener createGenericMovieListener(final List<Movie> movieList, final RecyclerView.Adapter<?> adapter, final ProgressBar progressBar, final String logTag) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    movieList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Movie movie = dataSnapshot.getValue(Movie.class);
                        if (movie != null && movie.getImage() != null && !movie.getImage().isEmpty()) {
                            String movieId = dataSnapshot.getKey();
                            movie.setMovieId(movieId);
                            movieList.add(movie);
                        }
                    }
                    Log.d(TAG, "Loaded " + movieList.size() + " items for " + logTag);
                    adapter.notifyDataSetChanged();
                    if (adapter instanceof GenericSliderAdapter) {
                        restartAutoScroll();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing data for " + logTag, e);
                } finally {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase onCancelled for " + logTag + ": " + error.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        };
    }

    /**
     * Cài đặt logic tự động trượt cho banner.
     */
    private void setupBannerAutoScroll() {
        slideRunnable = () -> {
            if (bannerMovieAdapter != null) {
                int currentItem = viewPagerBannerMovie.getCurrentItem();
                int totalItems = bannerMovieAdapter.getItemCount();
                if (totalItems > 1) {
                    viewPagerBannerMovie.setCurrentItem((currentItem + 1) % totalItems, true);
                }
            }
        };

        viewPagerBannerMovie.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                restartAutoScroll();
            }
        });
    }

    private void restartAutoScroll() {
        slideHandler.removeCallbacks(slideRunnable);
        if (bannerMovieAdapter != null && bannerMovieAdapter.getItemCount() > 1) {
            slideHandler.postDelayed(slideRunnable, 3000);
        }
    }

    private void navigateToMovieDetails(Movie movie) {
        // Sử dụng getContext() để lấy Context thay vì 'HomePageActivity.this'
        Intent intent = new Intent(getContext(), MovieDetailActivity.class);
        intent.putExtra("MOVIE_ID", movie.getMovieId());
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        slideHandler.removeCallbacks(slideRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        restartAutoScroll();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // QUAN TRỌNG: Gỡ bỏ listener ở onDestroyView để tránh rò rỉ bộ nhớ
        // khi view của fragment bị hủy.
        if (moviesRef != null && bannerMoviesListener != null) {
            moviesRef.removeEventListener(bannerMoviesListener);
        }
        if (topMoviesQuery != null && topMoviesListener != null) {
            topMoviesQuery.removeEventListener(topMoviesListener);
        }
        if (upcomingMoviesQuery != null && upcomingMoviesListener != null) {
            upcomingMoviesQuery.removeEventListener(upcomingMoviesListener);
        }
        slideHandler.removeCallbacksAndMessages(null); // Dọn dẹp sạch sẽ handler
    }
}