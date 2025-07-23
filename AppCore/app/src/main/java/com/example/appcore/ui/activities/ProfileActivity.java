package com.example.appcore.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appcore.R;
import com.example.appcore.TransactionHistoryActivity;
import com.example.appcore.dao.UserDAO;
import com.example.appcore.ui.activities.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private TextView txtTen, txtEmail, txtSDT, txtDiaChi;
    private UserDAO userDAO;
    private String userId;
    private boolean isFinishing = false;
    private LinearLayout btnUpdatePro, btnDeleteuser,btnChangePassword, btnTransaction;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        txtTen = findViewById(R.id.txt_ten_TCN);
        txtEmail = findViewById(R.id.txt_email_TCN);
        txtSDT = findViewById(R.id.txt_SDT_TCN);
        txtDiaChi = findViewById(R.id.txt_Dia_Chi);
        btnDeleteuser = findViewById(R.id.btn_delete_user);
        btnTransaction = findViewById(R.id.btn_transaction);


        userDAO = new UserDAO();
        // Lấy userId từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");
        Log.d("userId", "userId từ SharedPreferences: " + userId);
        if (userId.isEmpty()) {
            Log.e("userId", "userId không hợp lệ");
            Toast.makeText(getApplicationContext(), "userId không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        // Tải dữ liệu ban đầu
        loadUserData();

        btnChangePassword = findViewById(R.id.btn_change_password);
        btnChangePassword.setOnClickListener(v -> {
            Log.d(TAG, "Chuyển đến ChangePasswordActivity");
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        btnUpdatePro = findViewById(R.id.btn_updateprofile);
        btnUpdatePro.setOnClickListener(v -> {
            Log.d(TAG, "Chuyển đến UpdateProfileActivity");
            Intent intent = new Intent(ProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        });

        btnTransaction.setOnClickListener(v -> {
            Log.d(TAG, "Chuyển đến TransactionHistoryActivity");
            Intent intent = new Intent(ProfileActivity.this, TransactionHistoryActivity.class);
            startActivity(intent);
        });

        btnDeleteuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("Thông báo")
                        .setMessage("Bạn có chắc chắn muốn xoa tai khoan khong?")
                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteUserInAuth();
                                userDAO.deleteUser(userId);
//                                finish();
                                dialog.dismiss();
                                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                            }
                        })
                        .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();

            }
        });

    }

    private void deleteUserInAuth() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("DeleteUser", "Tài khoản đã được xoá thành công.");
                            // Chuyển hướng về màn hình đăng nhập nếu cần
                        } else {
                            Log.e("DeleteUser", "Xoá tài khoản thất bại: " + task.getException());
                            // Có thể cần re-authenticate nếu lỗi là do phiên đăng nhập đã cũ
                        }
                    });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Làm mới dữ liệu người dùng");
        loadUserData(); // Làm mới dữ liệu khi quay lại từ UpdateUserActivity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isFinishing = true;
        Log.d(TAG, "onDestroy: Activity bị hủy");
    }

    private void loadUserData() {
        // Lấy dữ liệu từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        String fullName = sharedPreferences.getString("fullName", "");
        String email = sharedPreferences.getString("email", "");
        String phone = sharedPreferences.getString("phone", "");
        String address = sharedPreferences.getString("address", "");

        int role = sharedPreferences.getInt("role", -1);

        Log.d(TAG, "Dữ liệu từ SharedPreferences - fullName: " + fullName + ", email: " + email + ", phone: " + phone);

        // Hiển thị thông tin
        txtTen.setText(fullName);
        txtEmail.setText(email);
        txtSDT.setText(phone);
        txtDiaChi.setText(address);


        // Tùy chọn: Lấy dữ liệu mới nhất từ Firebase nếu cần
        userDAO.fetchUserById(userId, user -> {
            if (isFinishing || isDestroyed()) {
                Log.w(TAG, "loadUserData: Activity đã bị hủy, bỏ qua callback");
                return;
            }
            if (user != null) {
                Log.d(TAG, "loadUserData: Tải dữ liệu từ Firebase thành công - User: " + user.getUserName());
                // Cập nhật SharedPreferences với dữ liệu mới nhất
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("fullName", user.getFullName());
                editor.putString("email", user.getEmail());
                editor.putString("phone", user.getPhone());
                editor.putString("address", user.getAddress());

                editor.apply();

                // Cập nhật giao diện
                txtTen.setText(user.getFullName() != null ? user.getFullName() : "");
                txtEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                txtSDT.setText(user.getPhone() != null ? user.getPhone() : "");
                txtDiaChi.setText(user.getAddress() != null ? user.getAddress() : "");

            } else {
                Log.e(TAG, "loadUserData: Không tìm thấy người dùng trên Firebase");
            }
        });
    }

}

