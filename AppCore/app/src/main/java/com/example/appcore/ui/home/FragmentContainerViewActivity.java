package com.example.appcore.ui.home;
import android.content.Intent;
import android.os.Bundle;
import com.example.appcore.ui.chatbox.ChatboxActivity;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.appcore.R;
import com.example.appcore.ui.activities.profile.ProfileFragment;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class FragmentContainerViewActivity extends AppCompatActivity {

    ChipNavigationBar chipNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container_view); // Sử dụng layout đã chỉnh sửa ở Bước 2

        chipNavigationBar = findViewById(R.id.bottomNavBar);

        // Load Fragment mặc định khi mở app lần đầu
        if (savedInstanceState == null) {
            chipNavigationBar.setItemSelected(R.id.home, true);
            loadFragment(new HomeFragment());
        }

        // Lắng nghe sự kiện khi người dùng chọn một mục
        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int id) {
                Fragment selectedFragment = null;

                if (id == R.id.home) { // Thay 'home' bằng id thực tế trong menu của bạn
                    selectedFragment = new HomeFragment();
                } else if (id == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();

                }
                else if (id == R.id.chat) {
                    // Khởi chạy ChatboxActivity
                    Intent intent = new Intent(FragmentContainerViewActivity.this, ChatboxActivity.class);
                    startActivity(intent);
//                } else if (id == R.id.cart) { // Thay 'cart' bằng id thực tế
//                    selectedFragment = new CartFragment();
//                } else if (id == R.id.nav_profile) { // Thay 'profile' bằng id thực tế
//                    selectedFragment = new ProfileFragment();
//                }
            }
                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                }
            }
        });
    }

    // Hàm để thay đổi Fragment trong container
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}