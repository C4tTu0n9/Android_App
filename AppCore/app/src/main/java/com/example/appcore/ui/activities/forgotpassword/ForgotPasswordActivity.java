package com.example.appcore.ui.activities.forgotpassword;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appcore.R;
import com.example.appcore.dao.AuthDAO;
import com.example.appcore.ui.activities.login.LoginActivity;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputEditText edtEmail;
    private Button btnSendResetEmail;
    private TextView txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        edtEmail = findViewById(R.id.edt_email);
        txtLogin = findViewById(R.id.txtLogin);
        btnSendResetEmail = findViewById(R.id.btn_send_reset_email);


        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class );
                startActivity(intent);
            }
        });

        btnSendResetEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString().trim();

                if (!email.isEmpty()) {
                    AuthDAO authDAO = new AuthDAO();
                    authDAO.sendPasswordResetEmail(ForgotPasswordActivity.this, email);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Vui lòng điền địa chỉ email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
