package com.example.appcore.ui.onboarding;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.appcore.R;
import com.example.appcore.ui.common.adapters.MoviePosterAdapter;
import com.example.appcore.ui.home.FragmentContainerViewActivity;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {
    private ViewPager2 viewPagerMovies;
    private MoviePosterAdapter adapter;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        viewPagerMovies = findViewById(R.id.view_pager_movies);

        // Chuẩn bị dữ liệu mẫu (bạn sẽ thay thế bằng dữ liệu từ API)
        // Hãy chắc chắn bạn đã thêm các ảnh này vào thư mục res/drawable
        List<Integer> moviePosters = new ArrayList<>();
        moviePosters.add(R.drawable.human); // Thay poster_furiosa bằng tên file ảnh của bạn
        moviePosters.add(R.drawable.the_dark_knight_rises);
        moviePosters.add(R.drawable.the_ides_of_march); // Thay poster_furiosa bằng tên file ảnh của bạn

        // Khởi tạo Adapter và gán cho ViewPager2
        adapter = new MoviePosterAdapter(moviePosters);
        viewPagerMovies.setAdapter(adapter);

        // Thiết lập các thuộc tính để xem được các item bên cạnh
        viewPagerMovies.setOffscreenPageLimit(3);
        viewPagerMovies.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);

        // Áp dụng hiệu ứng thu nhỏ cho các trang ở xa
        viewPagerMovies.setPageTransformer(new CarouselPageTransformer());

        // Thiết lập tự động trượt
        setupAutoSlider();

        // 1. Tìm nút "Get Started" bằng ID của nó
        // ID "btn_get_started" đã được định nghĩa trong file activity_onboarding.xml
        Button btnGetStarted = findViewById(R.id.btn_get_started);

        // 2. Thiết lập sự kiện lắng nghe khi nút được nhấn
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khi nút được nhấn, chúng ta sẽ thực hiện hành động chuyển màn hình

                // Tạo một Intent để chỉ định màn hình muốn chuyển đến (FragmentContainerViewActivity)
                Intent intent = new Intent(OnboardingActivity.this, FragmentContainerViewActivity.class);

                // Thực hiện chuyển màn hình
                startActivity(intent);

                // 3. Kết thúc OnboardingActivity
                // Gọi finish() để xóa Activity này khỏi lịch sử (back stack).
                // Điều này đảm bảo người dùng không thể nhấn nút Back từ Homepage để quay lại đây.
                finish();
            }
        });

    }

    private void setupAutoSlider() {
        sliderRunnable = () -> {
            int currentItem = viewPagerMovies.getCurrentItem();
            int nextItem = currentItem + 1;
            if (nextItem >= adapter.getItemCount()) {
                nextItem = 0; // Quay lại item đầu tiên
            }
            viewPagerMovies.setCurrentItem(nextItem, true); // true để có hiệu ứng trượt mượt
        };

        // Lắng nghe sự thay đổi trang của người dùng để reset timer
        viewPagerMovies.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Xóa và đặt lại timer mỗi khi người dùng vuốt
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000); // 3 giây
            }
        });
    }

    // Class nội bộ để tạo hiệu ứng carousel
    private static class CarouselPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        @Override
        public void transformPage(@NonNull View page, float position) {
            int pageWidth = page.getWidth();
            int pageHeight = page.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // Trang này đã ở rất xa bên trái.
                page.setAlpha(0f);
            } else if (position <= 1) { // [-1,1]
                // Thay đổi scale mặc định để tạo hiệu ứng co lại
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    page.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    page.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale trang xuống (giữa MIN_SCALE và 1)
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);

                // Làm mờ trang khi nó ở xa
                page.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            } else { // (1,+Infinity]
                // Trang này đã ở rất xa bên phải.
                page.setAlpha(0f);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Dừng auto-slider khi activity không còn hiển thị để tiết kiệm tài nguyên
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Bắt đầu lại auto-slider khi activity được tiếp tục
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }
}
    