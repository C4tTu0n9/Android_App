package com.example.appcore.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appcore.R;
import com.example.appcore.dao.AuthDAO;
import com.example.appcore.ui.activities.login.LoginActivity;
import com.example.appcore.ui.activities.register.RegisterActivity;
import com.google.android.material.textfield.TextInputEditText;

public class ChangePasswordActivity extends AppCompatActivity {
    private AuthDAO authDAO;

    private TextView txtQuayLai;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_changepassword);

        authDAO = new AuthDAO();


        TextInputEditText edtOldPassword = findViewById(R.id.edt_old_password);
        TextInputEditText edtNewPassword = findViewById(R.id.edt_new_password);
        TextInputEditText edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        Button btnChangePassword = findViewById(R.id.btn_change_password);


        // Xử lý sự kiện nhấn nút "Đổi mật khẩu"
        btnChangePassword.setOnClickListener(v -> {
            String oldPassword = edtOldPassword.getText().toString().trim();
            String newPassword = edtNewPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            authDAO.changePassword(this, oldPassword, newPassword, confirmPassword);
        });


        txtQuayLai = findViewById(R.id.txt_back);

        txtQuayLai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intnent = new Intent(ChangePasswordActivity.this, ProfileActivity.class);
                startActivity(intnent);
            }
        });
    


    }
}