package com.example.appcore.ui.activities.admin;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.appcore.R;
import com.example.appcore.ui.activities.login.LoginActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

// Lớp này là abstract vì nó không cần phải được hiển thị trực tiếp
public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tất cả các activity con sẽ sử dụng layout cơ sở này
        setContentView(R.layout.activity_base);

        // --- Cấu hình Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // --- Cấu hình Navigation Drawer ---
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    // Xử lý sự kiện click trên menu
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_quan_ly_phim) {
            startActivity(new Intent(this, MovieAdminActivity.class));
        } else if (itemId == R.id.nav_quan_ly_the_loai) {
            startActivity(new Intent(this, CategoryActivity.class));
            Toast.makeText(this, "Mở màn hình Quản lý Thể loại", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_quan_ly_phong) {
            startActivity(new Intent(this, AdminRoomActivity.class));
            Toast.makeText(this, "Mở màn hình Quản lý Phòng", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_dang_xuat) {
            logoutUser();
        } else if (itemId == R.id.nav_quan_ly_suat_chieu) {
            startActivity(new Intent(this, AdminShowtimeActivity.class));
            Toast.makeText(this, "Mở màn hình Quản lý Suất Chiếu", Toast.LENGTH_SHORT).show();
        }
//them
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * HÀM MỚI: Xử lý logic đăng xuất người dùng
     */
    private void logoutUser() {
        // 1. Đăng xuất khỏi Firebase Authentication
        FirebaseAuth.getInstance().signOut();

        // 2. Xóa dữ liệu người dùng đã lưu trong SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Xóa tất cả dữ liệu
        editor.apply();

        // 3. Quay về màn hình Login và xóa hết các Activity cũ
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(BaseActivity.this, LoginActivity.class); // Thay LoginActivity.class bằng tên Activity đăng nhập của bạn
        // Cờ này đảm bảo người dùng không thể nhấn back để quay lại màn hình admin
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Đóng Activity hiện tại
    }

    // Xử lý nút Back
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}