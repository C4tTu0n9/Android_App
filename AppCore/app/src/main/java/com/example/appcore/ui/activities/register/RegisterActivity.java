package com.example.appcore.ui.activities.register;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appcore.R;
import com.example.appcore.dao.AuthDAO;
import com.example.appcore.dao.UserDAO;
import com.example.appcore.data.models.User;

import com.example.appcore.ui.activities.forgotpassword.ForgotPasswordActivity;
import com.example.appcore.ui.activities.login.LoginActivity;

import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity  {

    private int[] imageList = {
            R.drawable.img_movie1,
            R.drawable.img_movie2,
            R.drawable.img_movie3
    };

    private int currentIndex = 0;
    private Handler handler = new Handler();
    private Runnable imageSwitcher;
    private ImageView imageView;

    private TextInputEditText edtEmail, edtPassword, edtRepassword, edtFullName, edtPhone, edtAddess;
    private Button btnRegister;
    private TextView txtLogin;
    private CheckBox chkTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // bạn phải có layout tương ứng
        imageView = findViewById(R.id.img_banner); // id trong XML

        // img
        imageSwitcher = new Runnable() {
            @Override
            public void run() {

                imageView.setImageResource(imageList[currentIndex]);
                currentIndex = (currentIndex + 1) % imageList.length;
                handler.postDelayed(this, 3000); // đổi ảnh sau 3 giây
            }
        };

        handler.post(imageSwitcher);

        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        edtRepassword = findViewById(R.id.edt_repassword);
        edtFullName = findViewById(R.id.edt_name);
        edtPhone = findViewById(R.id.edt_phone);
        txtLogin = findViewById(R.id.txtLogin);
        btnRegister = findViewById(R.id.btn_register);
        edtAddess = findViewById(R.id.edt_address);
        chkTerms = findViewById(R.id.chkTerms);
        btnRegister = findViewById(R.id.btn_register);


        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class );
                startActivity(intent);
            }
        });



        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                String repassword = edtRepassword.getText().toString().trim();
                String name = edtFullName.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                String address = edtAddess.getText().toString().trim();
                boolean termsChecked = chkTerms.isChecked();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || repassword.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(repassword)) {
                    Toast.makeText(RegisterActivity.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!termsChecked) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng đồng ý với các điều khoản sử dụng", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("testtestste","email =" +email);
                Log.d("testtestste","password =" +password);
                User user = new User();
                UserDAO userDAO = new UserDAO();
                AuthDAO authDao = new AuthDAO();
                authDao.registerUser(
                        RegisterActivity.this,
                        email,
                        password,
                        name,
                        phone,
                        address,
                        user
                );

            }
        });


    }
}
