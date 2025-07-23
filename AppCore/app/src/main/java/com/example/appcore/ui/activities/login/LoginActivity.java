package com.example.appcore.ui.activities.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appcore.R;
import com.example.appcore.dao.AuthDAO;
import com.example.appcore.dao.UserDAO;
import com.example.appcore.ui.activities.forgotpassword.ForgotPasswordActivity;
import com.example.appcore.ui.activities.register.RegisterActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail,edtPassword;
    private Button btnLogin;
    private TextView txtDangki,txtQuenMatKhau;


    // chay banner login
    private int[] imageList = {
            R.drawable.img_movie1,
            R.drawable.img_movie2,
            R.drawable.img_movie3
    };

    private int currentIndex = 0;
    private Handler handler = new Handler();
    private Runnable imageSwitcher;
    private ImageView imageView;

    private AuthDAO authDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // bạn phải có layout tương ứng

        authDao = new AuthDAO();

        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnLogin = findViewById(R.id.btn_login);
        txtDangki = findViewById(R.id.btn_Dangki);
        txtQuenMatKhau = findViewById(R.id.txtQuenMatKhau);


        // click login button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if(!email.isEmpty() && !password.isEmpty()){
                    authDao.loginUser(LoginActivity.this, email,password);
                } else {
                    Toast.makeText(LoginActivity.this, "Vui long nhap day du email password",Toast.LENGTH_SHORT).show();
                }

            }
        });



        // chuyển qua layout Register
        txtDangki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intnent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intnent);
            }
        });

        // chuyển sang layout ForgotPassword
        txtQuenMatKhau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // chuyển layout banner login
        imageView = findViewById(R.id.img_banner); // id trong XML
        imageSwitcher = new Runnable() {
            @Override
            public void run() {
                imageView.setImageResource(imageList[currentIndex]);
                currentIndex = (currentIndex + 1) % imageList.length;
                handler.postDelayed(this, 3000); // đổi ảnh sau 3 giây
            }
        };

        handler.post(imageSwitcher);
    }
}
