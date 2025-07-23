package com.example.appcore.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appcore.R;
import com.example.appcore.dao.UserDAO;
import com.example.appcore.data.models.User;
import com.example.appcore.data.remote.FirebaseDataSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdateProfileActivity extends AppCompatActivity {

    private static final String TAG = "UpdateUserActivity";
    private EditText editTextName, editTextEmail, editTextPhone, editTextAddress;
    private Button btnUpdate, btnCancel; // Ánh xạ đúng với layout
    private UserDAO userDAO;
    private String userId;

    private User current_user;
    private boolean isFinishing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_profile);

        userDAO = new UserDAO();
        userId = getuserID();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Cập nhật thông tin");
        }

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddress = findViewById(R.id.editTextAddress);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_Name = editTextName.getText().toString();
                String new_email = editTextEmail.getText().toString();
                String new_Phone = editTextPhone.getText().toString();
                String new_Address = editTextAddress.getText().toString();

                current_user.setEmail(new_email);
                current_user.setFullName(new_Name);
                current_user.setPhone(new_Phone);
                current_user.setAddress(new_Address);

                userDAO.updateUser(current_user, new UserDAO.OnUserOperationListener() {
                    @Override
                    public void onSuccess() {
                        finish();
                        Toast.makeText(UpdateProfileActivity.this,"Thanh cong!",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(UpdateProfileActivity.this,"That bai!",Toast.LENGTH_SHORT).show();
                        loadUserData();
                    }
                });

                Log.d("testuseruserusu"," user = "+current_user);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private String getuserID() {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return id;
    }

    private void loadUserData() {
        userDAO.fetchUserById(userId, user -> {
            current_user = user;
            if (isFinishing || isDestroyed()) {
                return;
            }
            if (user != null) {
                editTextName.setText(user.getFullName() != null ? user.getFullName() : "");
                editTextEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                editTextPhone.setText(user.getPhone() != null ? user.getPhone() : "");
                editTextAddress.setText(user.getAddress()!= null ? user.getAddress() : "" );
            } else {
                Toast.makeText(getApplicationContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}